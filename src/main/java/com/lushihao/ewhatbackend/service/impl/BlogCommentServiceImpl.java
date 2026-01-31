package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.druid.support.spring.stat.annotation.Stat;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.constant.StatusConstant;
import com.lushihao.ewhatbackend.context.BaseContext;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import com.lushihao.ewhatbackend.mapper.UserMapper;
import com.lushihao.ewhatbackend.model.dto.BlogCommentDTO;
import com.lushihao.ewhatbackend.model.entity.Blog;
import com.lushihao.ewhatbackend.model.entity.BlogComment;
import com.lushihao.ewhatbackend.model.entity.User;
import com.lushihao.ewhatbackend.model.vo.BlogCommentVO;
import com.lushihao.ewhatbackend.model.vo.BlogVO;
import com.lushihao.ewhatbackend.model.vo.UserVO;
import com.lushihao.ewhatbackend.service.BlogCommentService;
import com.lushihao.ewhatbackend.mapper.BlogCommentMapper;
import com.lushihao.ewhatbackend.service.BlogService;
import com.lushihao.ewhatbackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lushihao.ewhatbackend.constant.RedisConstants.BLOG_COMMENT_LIKED_KEY;

/**
 * @author lushihao
 * @description 针对表【tb_blog_comments(博文评论)】的数据库操作Service实现
 * @createDate 2025-11-05 22:13:49
 */
@Service
public class BlogCommentServiceImpl extends ServiceImpl<BlogCommentMapper, BlogComment>
        implements BlogCommentService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private BlogService blogService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发表评论
     *
     * @param blogCommentDTO 前端传入评论DTO
     * @return 保存的评论id
     */
    @Override
    public Long saveBlogComment(BlogCommentDTO blogCommentDTO) {
        // 1.获取当前登录的用户
        Long userId = BaseContext.getCurrentId();
        Long blogId = blogCommentDTO.getBlogId();
        // 2.判断当前博文是否存在
        Blog blog = blogService.getById(blogId);
        ThrowUtils.throwIf(blog == null, ErrorCode.NOT_FOUND_ERROR, "帖子不存在");
        // 2.补充相关值
        BlogComment blogComment = BeanUtil.copyProperties(blogCommentDTO, BlogComment.class);
        blogComment.setUserId(userId);
        // 2.1初始化点赞数为0
        blogComment.setLiked(0);
        // 3.保存评论
        boolean isSuccess = this.save(blogComment);
        ThrowUtils.throwIf(!isSuccess, ErrorCode.OPERATION_ERROR, "发表评论失败，请稍后重试!");
        blogService.update().setSql("comments = comments + 1").eq("id", blogComment.getBlogId()).update();
        // 3.返回博文id
        return blogComment.getId();
    }

    @Override
    public Boolean deleteBlogComment(Long id) {
        // 1.获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        // 2.判断评论是否存在
        BlogComment blogComment = this.getById(id);
        ThrowUtils.throwIf(blogComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 3.权限校验，只能删除自己的评论
        ThrowUtils.throwIf(!blogComment.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "只能删除自己的评论");
        // 4. 检查是否已被删除
        ThrowUtils.throwIf(blogComment.getStatus().equals(StatusConstant.DELETE), ErrorCode.OPERATION_ERROR, "评论已被删除");
        // 5. 软删除:更新status和content
        BlogComment updateComment = BlogComment.builder().id(id).status(StatusConstant.DELETE).content("该评论已经被删除！").build();
        boolean isSuccess = this.updateById(updateComment);
        ThrowUtils.throwIf(!isSuccess, ErrorCode.OPERATION_ERROR, "删除评论失败，请稍后重试!");
        blogService.update().setSql("comments = comments - 1").eq("id", blogComment.getBlogId()).update();
        return true;
    }

    @Override
    public Page<BlogCommentVO> queryCommentByBlogIdWithPage(Long id, PageRequest pageRequest) {
        // 1.参数校验
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "博文id异常！");
        // 2.构造分页对象
        Page<BlogComment> page = new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize());
        // 3.构造查询条件 只查询一级评论 并且仅保留启用状态的
        QueryWrapper<BlogComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("blog_id", id).eq("parent_id", 0).eq("status", StatusConstant.ENABLE);
        // 4. 处理排序
        if (pageRequest.getSortField() != null && !pageRequest.getSortField().isEmpty()) {
            boolean isAsc = "ascend".equalsIgnoreCase(pageRequest.getSortOrder());
            queryWrapper.orderBy(true, isAsc, pageRequest.getSortField());
        } else {
            // 默认按创建时间降序（最新的在前）
            queryWrapper.orderByDesc("create_time");
        }
        // 5.执行分页
        Page<BlogComment> parentCommentPage = this.page(page, queryWrapper);
        if (parentCommentPage == null) {
            return new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize(), 0);
        }
        // 6.获取所有的一级评论id
        List<Long> parentCommentIdlist = parentCommentPage.getRecords().stream().map(BlogComment::getId).toList();
        // 7.查询这些以及评论对应的二级评论
        QueryWrapper<BlogComment> childrenWrapper = new QueryWrapper<>();
        childrenWrapper.in("parent_id", parentCommentIdlist).eq("status", StatusConstant.ENABLE).orderByDesc("create_time");
        List<BlogComment> allChildren = this.list(childrenWrapper);
        // 8.按parent_id分组  key是parentId value是对应的BlogComment
        Map<Long, List<BlogComment>> childrenMap = allChildren.stream().collect(Collectors.groupingBy(BlogComment::getParentId));
        // 9. 批量查询所有用户信息（一级评论和二级评论的用户）
        List<Long> allUserIds = new ArrayList<>();
        parentCommentPage.getRecords().forEach(comment -> allUserIds.add(comment.getUserId()));
        // 遍历每个子评论
        allChildren.forEach(comment -> {
            allUserIds.add(comment.getUserId());
            //
            if (comment.getAnswerId() != null && comment.getAnswerId() != 0) {
                BlogComment blogComment = this.getById(comment.getAnswerId());
                if (blogComment != null) {
                    allUserIds.add(blogComment.getUserId());
                }
            }
        });
        List<User> users = userMapper.selectBatchIds(allUserIds.stream().distinct().collect(Collectors.toList()));
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        // 11. 构建一级评论VO（包含子评论）
        List<BlogCommentVO> voList = parentCommentPage.getRecords().stream()
                .map(parentComment -> buildCommentVO(parentComment, userMap, childrenMap))
                .collect(Collectors.toList());
        // 12. 构建分页结果
        Page<BlogCommentVO> resultPage = new Page<>(parentCommentPage.getCurrent(),
                parentCommentPage.getSize(), parentCommentPage.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }

    @Override
    public Boolean likeBlogComment(Long id) {
        // 1.获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        BlogComment blogComment = this.getById(id);
        ThrowUtils.throwIf(blogComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 2.判断当前用户是否已经点赞
        // 利用ZSet存储点赞 其中key是 blogComment:liked:blogCommentId value是userId score是点赞的时间
        String key = BLOG_COMMENT_LIKED_KEY + id;
        // 2.1 获取Score
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        // 未点赞
        if (score == null) {
            boolean isSuccess = this.update().setSql("liked = liked + 1").eq("id", id).update();
            // 数据库更新成功 更新redis
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 已点赞 取消点赞
            boolean isSuccess = this.update().setSql("liked  = liked -1").eq("id", id).update();
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
        return true;
    }

    /**
     * 当前用户是否点赞评论
     *
     * @param commentId 评论id
     * @return 是否点赞
     */
    private Boolean isLike(Long commentId) {
        Long userId = BaseContext.getCurrentId();
        String key = BLOG_COMMENT_LIKED_KEY + commentId;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        return score != null;
    }

    private BlogCommentVO buildCommentVO(BlogComment comment, Map<Long, User> userMap, Map<Long, List<BlogComment>> childrenMap) {
        // 1. 构建基础VO
        Boolean isLiked = isLike(comment.getId());
        BlogCommentVO vo = BlogCommentVO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .liked(comment.getLiked())
                .parentId(comment.getParentId())
                .answerId(comment.getAnswerId())
                .createTime(comment.getCreateTime())
                .isLike(isLiked)
                .build();
        // 2. 设置用户信息
        User user = userMap.get(comment.getUserId());
        if (user != null) {
            UserVO userVO = UserVO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .avatar(user.getAvatar())
                    .sex(user.getSex())
                    .build();
            vo.setUser(userVO);
        }

// 3. 如果是二级评论，设置回复的用户信息
        if (comment.getAnswerId() != null && comment.getAnswerId() != 0) {
            // 查找被回复的评论
            BlogComment answerComment = this.getById(comment.getAnswerId());
            if (answerComment != null) {
                User answerUser = userMap.get(answerComment.getUserId());
                if (answerUser != null) {
                    UserVO answerUserVO = UserVO.builder()
                            .id(answerUser.getId())
                            .name(answerUser.getName())
                            .avatar(answerUser.getAvatar())
                            .sex(answerUser.getSex())
                            .build();
                    vo.setAnswerUser(answerUserVO);
                }
            }
        }

        // 4. 设置子评论（如果是一级评论）
        if (comment.getParentId() == 0 && childrenMap != null) {
            List<BlogComment> children = childrenMap.get(comment.getId());
            if (children != null && !children.isEmpty()) {
                List<BlogCommentVO> childrenVO = new ArrayList<>();
                for (BlogComment child : children) {
                    BlogCommentVO childVO = buildCommentVO(child, userMap, null);
                    childrenVO.add(childVO);
                }
                vo.setChildren(childrenVO);
            } else {
                vo.setChildren(new ArrayList<>());
            }
        }

        return vo;
    }
}





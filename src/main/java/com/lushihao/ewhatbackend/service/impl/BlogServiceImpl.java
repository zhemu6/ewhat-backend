package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.constant.StatusConstant;
import com.lushihao.ewhatbackend.context.BaseContext;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import com.lushihao.ewhatbackend.mapper.UserMapper;
import com.lushihao.ewhatbackend.model.dto.BlogDTO;
import com.lushihao.ewhatbackend.model.entity.Blog;
import com.lushihao.ewhatbackend.model.entity.Canteen;
import com.lushihao.ewhatbackend.model.entity.Follow;
import com.lushihao.ewhatbackend.model.entity.User;
import com.lushihao.ewhatbackend.model.vo.BlogVO;
import com.lushihao.ewhatbackend.model.vo.ScrollResult;
import com.lushihao.ewhatbackend.model.vo.UserVO;
import com.lushihao.ewhatbackend.service.BlogService;
import com.lushihao.ewhatbackend.mapper.BlogMapper;
import com.lushihao.ewhatbackend.service.FollowService;
import com.lushihao.ewhatbackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lushihao.ewhatbackend.constant.RedisConstants.BLOG_LIKED_KEY;
import static com.lushihao.ewhatbackend.constant.RedisConstants.FEED_KEY;

/**
 * @author lushihao
 * @description 针对表【tb_blog(博文信息)】的数据库操作Service实现
 * @createDate 2025-11-05 21:40:53
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {
    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private FollowService followService;

    @Override
    public Long saveBlog(BlogDTO blogDTO) {
        // 1.获取登录用户
        Long userId = BaseContext.getCurrentId();
        Blog blog = BeanUtil.copyProperties(blogDTO, Blog.class);
        blog.setUserId(userId);
        // 2.保存博文
        boolean isSuccess = this.save(blog);
        ThrowUtils.throwIf(!isSuccess, ErrorCode.OPERATION_ERROR, "保存博文失败，请稍后重试~");
        // 3.推送博文到当前作者的粉丝
        // 3.1 查询当前博文的粉丝
        List<Follow> follows = followService.query().eq("follow_user_id", userId).list();
        // 3.2 推送博文
        for (Follow follow : follows) {
            // 遍历每个粉丝
            Long fansKey = follow.getId();
            // 粉丝的收件箱
            String key = FEED_KEY + fansKey;
            // 推送 利用ZSet实现 其中key是粉丝的收件箱 value是博文id score是系统时间戳
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());

        }
        // 3.返回博文ID
        return blog.getId();
    }

    @Override
    public Boolean likeBlog(Long id) {
        // 1.获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        // 2.判断当前用户是否已经点赞
        // 利用ZSet存储点赞 其中key是 blog:liked:blogId value是userId score是点赞的时间
        String key = BLOG_LIKED_KEY + id;
        // 2.1 获取Score
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        // 未点赞
        if (score == null) {
            // 更新blog中的点赞数 + 1
            boolean isSuccess = this.update().setSql("liked = liked + 1")
                    .eq("id", id).update();
            // 数据库更新成功 才更新redis中缓存
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        }
        // 已点赞 那就执行取消点赞
        boolean isSuccess = this.update().setSql("liked = liked - 1")
                .eq("id", id).update();
        if (isSuccess) {
            stringRedisTemplate.opsForZSet().remove(key, userId.toString());
        }
        return true;
    }

    @Override
    public Page<BlogVO> queryMyBlogByPage(PageRequest pageRequest) {
        Long userId = BaseContext.getCurrentId();
        // 构建分页对象
        Page<Blog> page = new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize());
        // 构建查询条件
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (pageRequest.getSortField() != null && !pageRequest.getSortField().isEmpty()) {
            boolean isAsc = "ascend".equalsIgnoreCase(pageRequest.getSortOrder());
            queryWrapper.orderBy(true, isAsc, pageRequest.getSortField());
        }
        Page<Blog> blogPage = this.page(page, queryWrapper);

        // 执行分页查询
        return (Page<BlogVO>) blogPage.convert(blog -> {
            BlogVO vo = new BlogVO();
            BeanUtil.copyProperties(blog, vo);
            return vo;
        });
    }

    @Override
    public Page<Blog> queryBlogByPage(PageRequest pageRequest) {
        // 构建分页对象
        Page<Blog> page = new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize());
        // 构建查询条件
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        if (pageRequest.getSortField() != null && !pageRequest.getSortField().isEmpty()) {
            boolean isAsc = "ascend".equalsIgnoreCase(pageRequest.getSortOrder());
            queryWrapper.orderBy(true, isAsc, pageRequest.getSortField());
        }
        return this.page(page, queryWrapper);
    }

    @Override
    public boolean updateBlogStatus(Long id, Integer status) {
        // 参数校验
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "博客ID不合法");
        ThrowUtils.throwIf(status == null || (status != 0 && status != 1),
                ErrorCode.PARAMS_ERROR, "状态值只能是0或1");

        // 检查博客是否存在
        Blog blog = this.getById(id);
        ThrowUtils.throwIf(blog == null, ErrorCode.NOT_FOUND_ERROR, "博客不存在");

        // 更新状态
        Blog updateBlog = new Blog();
        updateBlog.setId(id);
        updateBlog.setStatus(status);

        return this.updateById(updateBlog);
    }

    /**
     * 查询当前用户关注用户的博客
     *
     * @param max    上次查询的最小时间戳
     * @param offset 偏移量
     * @return ScrollResult
     */
    @Override
    public ScrollResult queryBlogOfFollow(Long max, Integer offset) {
        // 1.获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        // 2.查询当前登录用户的收件箱 构建Redis key，格式如：feed:123
        String key = FEED_KEY + userId;
        // 按照时间戳倒叙排列 新的在前 旧的在后 reverseRangeByScoreWithScores参数说明：
        // key: Redis的ZSet键
        // min: 0 最小分数
        // max: 传入的max值，作为本次查询的最大分数（时间戳）
        // offset: 偏移量，用于处理相同分数的元素
        // count: 3 每次查询3条记录
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 3);
        if(typedTuples == null||typedTuples.isEmpty()){
            return null;
        }
        // 存储博客ID
        ArrayList<Long> ids = new ArrayList<>(typedTuples.size());
        // 记录本次查询的最小时间戳（用于下一次查询）
        long minTime = 0;
        // 记录偏移量 是时间戳等于最小时间戳的个数
        int os = 1;
        // 3.解析数据获得ScrollResult各部分数据 tuple中有value和score 由于这里是按照从大到小排序的 所以遍历完 最后一个是最小的
        for(ZSetOperations.TypedTuple<String> tuple :typedTuples){
            // 3.1.获取id
            ids.add(Long.valueOf(tuple.getValue()));
            // 3.2 获取分数（时间戳）
            long time = tuple.getScore().longValue();
            if(minTime == time){
                os++;
            }else{
                minTime=time;
                // 重置计数器
                os=1;
            }
        }
        os = minTime == max ? os : os + offset;
        // 4.根据id查询博客
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        List<BlogVO> blogVOs = new ArrayList<>(blogs.size());
        // 4.1 填充具体信息
        for (Blog blog : blogs) {
            BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
            // 2.查询当前博文的作者相关信息
            queryBlogUser(blogVO);
            // 3.查询当前用户是否点赞
            isBlogLiked(blogVO, blog.getId());
            blogVOs.add(blogVO);
        }
        return ScrollResult.builder().list(blogVOs).offset(os).minTime(minTime).build();
    }

    @Override
    public Page<BlogVO> queryHotBlogVOByPage(PageRequest pageRequest) {
        pageRequest.setSortField("liked");
        pageRequest.setSortOrder("descend");
        // 构建分页对象
        Page<Blog> page = new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize());
        // 构建查询条件
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", StatusConstant.ENABLE);
        if (pageRequest.getSortField() != null && !pageRequest.getSortField().isEmpty()) {
            boolean isAsc = "ascend".equalsIgnoreCase(pageRequest.getSortOrder());
            queryWrapper.orderBy(true, isAsc, pageRequest.getSortField());
        }
        // 执行分页查询
        return (Page<BlogVO>) this.page(page, queryWrapper).convert(blog -> {
            BlogVO vo = new BlogVO();
            BeanUtil.copyProperties(blog, vo);
            return vo;
        });
    }

    /**
     * 查询博客的所有点赞的人
     * 利用zrange查询
     *
     * @param id blog的id
     * @return List<UserVO>
     */
    @Override
    public List<UserVO> queryBlogLikes(Long id) {
        String key = BLOG_LIKED_KEY + id;
        // 1.查询点赞最新的五个人的用户id
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        // 为空直接返回 无人点赞
        if (CollUtil.isEmpty(top5)) {
            return Collections.emptyList();
        }
        // 2. 将查询到的id 转换成Long
        List<Long> ids = top5.stream().map(Long::valueOf).toList();
        // 3. 根据ids查询用户信息
        List<User> users = userService.query().in("id", ids).list();
        // 构建一个map集合 其中key 是用户id value是用户实体
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<UserVO> userVOS = ids.stream()
                // 按原始顺序从Map中获取用户
                .map(userMap::get)
                // 过滤掉空用户
                .filter(Objects::nonNull)
                .map(user -> BeanUtil.copyProperties(user, UserVO.class))
                .toList();
        return userVOS;
    }

    @Override
    public Blog queryBlogById(Long id) {
        Blog blog = this.getById(id);
        ThrowUtils.throwIf(blog == null, ErrorCode.NOT_FOUND_ERROR, "请求帖子不存在！");
        return blog;
    }

    /**
     * 通过博文id查询博文具体信息
     *
     * @param id
     * @return
     */
    @Override
    public BlogVO queryById(Long id) {
        // 1.获取博文
        Blog blog = this.getById(id);
        ThrowUtils.throwIf(blog == null, ErrorCode.NOT_FOUND_ERROR, "帖子不存在，请稍后刷新页面!");
        ThrowUtils.throwIf(!blog.getStatus().equals(StatusConstant.ENABLE), ErrorCode.NOT_FOUND_ERROR, "帖子状态异常，无法查看!");
        BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
        // 2.查询当前博文的作者相关信息
        queryBlogUser(blogVO);
        // 3.查询当前用户是否点赞
        isBlogLiked(blogVO, id);
        return blogVO;
    }


    /**
     * 查询当前用户是否给该博文点赞
     *
     * @param blogVO
     */
    private void isBlogLiked(BlogVO blogVO, Long blogId) {
        Long userId = BaseContext.getCurrentId();
        // key的格式是 blog:liked:blogId
        String key = BLOG_LIKED_KEY + blogId;
        // 从redis中判断是否点赞 如果从ZSet中的key获得的score不为null 代表
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blogVO.setIsLike(score != null);
    }

    /**
     * 查询Blog的发布者相关信息
     *
     * @param blogVO
     */
    private void queryBlogUser(BlogVO blogVO) {
        User user = userService.getById(blogVO.getUserId());
        blogVO.setName(user.getName());
        blogVO.setIcon(user.getAvatar());
    }
}





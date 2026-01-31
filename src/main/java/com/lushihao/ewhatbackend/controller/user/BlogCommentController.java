package com.lushihao.ewhatbackend.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.dto.BlogCommentDTO;
import com.lushihao.ewhatbackend.model.vo.BlogCommentVO;
import com.lushihao.ewhatbackend.service.BlogCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序段-博客评论管理
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-11-09   18:10
 */
@Slf4j
@RequestMapping("/user/blog_comment")
@RestController("userBlogCommentController")
@RequiredArgsConstructor
public class BlogCommentController {

    private final BlogCommentService blogCommentService;

    /**
     * 发表评论
     *
     * @param blogCommentDTO 前端传入评论DTO
     * @return 保存的评论id
     */
    @PostMapping
    public BaseResponse<Long> saveBlogComment(@RequestBody BlogCommentDTO blogCommentDTO) {
        return ResultUtils.success(blogCommentService.saveBlogComment(blogCommentDTO));
    }

    /**
     * 删除自己评论
     *
     * @param id 评论id
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteBlogComment(@PathVariable("id") Long id) {
        return ResultUtils.success(blogCommentService.deleteBlogComment(id));
    }

    /**
     * 分页查询当前博文下所有评论
     *
     * @param id          博文id
     * @param pageRequest 分页请求参数
     * @return Page
     */
    @GetMapping("/blog/{blogId}")
    public BaseResponse<Page<BlogCommentVO>> queryCommentByBlogIdWithPage(@PathVariable("blogId") Long id, PageRequest pageRequest) {
        return ResultUtils.success(blogCommentService.queryCommentByBlogIdWithPage(id, pageRequest));
    }

    /**
     * 点赞评论
     *
     * @param id 评论id
     * @return 是否点赞成功
     */
    @PutMapping("/like/{id}")
    public BaseResponse<Boolean> likeBlogComment(@PathVariable("id") Long id) {
        return ResultUtils.success(blogCommentService.likeBlogComment(id));
    }


}

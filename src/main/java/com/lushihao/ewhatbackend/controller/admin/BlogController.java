package com.lushihao.ewhatbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import com.lushihao.ewhatbackend.model.dto.BlogStatusUpdateRequest;
import com.lushihao.ewhatbackend.model.entity.Blog;
import com.lushihao.ewhatbackend.service.BlogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-博文管理
 * @author: lushihao
 * @version: 1.0
 * create:   2025-11-06   10:25
 */
@Slf4j
@RestController("adminBlogController")
@RequestMapping("/admin/blog")
public class BlogController {
    @Resource
    private BlogService blogService;

    /**
     * 根据id查询博文
     * @param id 博文id
     * @return 博文
     */
    @GetMapping("/{id}")
    public BaseResponse<Blog> queryBlogById(@PathVariable("id") Long id){
        Blog blog =  blogService.queryBlogById(id);
        return ResultUtils.success(blog);
    }

    /**
     * 分页查询博客
     * @param pageRequest 分页请求
     * @return
     */
    @GetMapping("/page")
    public BaseResponse<Page<Blog>> queryBlogByPage(PageRequest pageRequest){
        Page<Blog> page = blogService.queryBlogByPage(pageRequest);
        return ResultUtils.success(page);
    }

    /**
     * 更新博客审核状态
     * @param request 包含博客ID和状态值
     * @return 操作结果
     */
    @PutMapping("/status")
    public BaseResponse<Boolean> updateBlogStatus(@RequestBody BlogStatusUpdateRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);

        boolean result = blogService.updateBlogStatus(request.getId(), request.getStatus());
        return ResultUtils.success(result);
    }












}

package com.lushihao.ewhatbackend.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.dto.BlogDTO;
import com.lushihao.ewhatbackend.model.vo.BlogVO;
import com.lushihao.ewhatbackend.model.vo.ScrollResult;
import com.lushihao.ewhatbackend.model.vo.UserVO;
import com.lushihao.ewhatbackend.service.BlogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端-博客管理
 * @author: lushihao
 * @version: 1.0
 * create:   2025-11-05   21:43
 */
@Slf4j
@RestController("userBlogController")
@RequestMapping("/user/blog")
public class BlogController {
    @Resource
    private BlogService blogService;

    /**
     * 发布博文
     * @param blogDTO 前端传入博文DTO
     * @return 博文的id
     */
    @PostMapping
    public BaseResponse<Long> saveBlog(@RequestBody BlogDTO blogDTO){
        return ResultUtils.success(blogService.saveBlog(blogDTO));
    }

    /**
     * 博文点赞功能
     * @param id 博文id
     * @return 是否点赞成功
     */
    @PutMapping("/like/{id}")
    public BaseResponse<Boolean> likeBlog(@PathVariable("id") Long id){
        return ResultUtils.success(blogService.likeBlog(id));
    }

    /**
     * 根据id查询博文
     * @param id 食堂id
     * @return VO 对象
     */
    @GetMapping("/{id}")
    public BaseResponse<BlogVO> queryById(@PathVariable("id") Long id){
        return ResultUtils.success(blogService.queryById(id));
    }

    /**
     * 分页查询自己的博客
     * @param pageRequest 分页请求
     * @return Page 博客
     */
    @GetMapping("/of/me")
    public BaseResponse<Page<BlogVO>> queryMyBlogByPage(PageRequest pageRequest){
        Page<BlogVO> page = blogService.queryMyBlogByPage(pageRequest);
        return ResultUtils.success(page);
    }

    /**
     * 分页查询热门帖子
     * @param pageRequest 分页请求
     * @return
     */
    @GetMapping("/hot")
    public BaseResponse<Page<BlogVO>> queryHotBlogByPage(PageRequest pageRequest){
        Page<BlogVO> page = blogService.queryHotBlogVOByPage(pageRequest);
        return ResultUtils.success(page);
    }

    /**
     * 查询博客的所有点赞的人
     * 利用zrange查询
     * @param id blog的id
     * @return  List<UserVO>
     */
    @GetMapping("/likes/{id}")
    public BaseResponse<List<UserVO>> queryBlogLikes(@PathVariable("id") Long id){
        return ResultUtils.success( blogService.queryBlogLikes(id));
    }

    /**
     * 查询当前用户关注用户的博客
     * @param max 上次查询的最小时间戳
     * @param offset 偏移量
     * @return ScrollResult
     */
    @GetMapping("/of/follow")
    public BaseResponse<ScrollResult> queryBlogOfFollow(@RequestParam("lastId") Long max,@RequestParam(value = "offset",defaultValue = "0") Integer offset){
        return ResultUtils.success(blogService.queryBlogOfFollow(max,offset));
    }


}

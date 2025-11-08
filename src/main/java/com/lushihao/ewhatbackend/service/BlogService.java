package com.lushihao.ewhatbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.model.dto.BlogDTO;
import com.lushihao.ewhatbackend.model.entity.Blog;
import com.lushihao.ewhatbackend.model.vo.BlogVO;
import com.lushihao.ewhatbackend.model.vo.ScrollResult;
import com.lushihao.ewhatbackend.model.vo.UserVO;

import java.util.List;

/**
* @author lushihao
* @description 针对表【tb_blog(博文信息)】的数据库操作Service
* @createDate 2025-11-05 21:40:53
*/
public interface BlogService extends IService<Blog> {

    BlogVO queryById(Long id);

    Long saveBlog(BlogDTO blogDTO);

    Boolean likeBlog(Long id);

    Page<BlogVO> queryMyBlogByPage(PageRequest pageRequest);

    Page<BlogVO> queryHotBlogVOByPage(PageRequest pageRequest);

    List<UserVO> queryBlogLikes(Long id);

    Blog queryBlogById(Long id);

    Page<Blog> queryBlogByPage(PageRequest pageRequest);

    boolean updateBlogStatus(Long id, Integer status);

    ScrollResult queryBlogOfFollow(Long max, Integer offset);
}

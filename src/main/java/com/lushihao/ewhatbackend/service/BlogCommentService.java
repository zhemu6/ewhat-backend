package com.lushihao.ewhatbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.model.dto.BlogCommentDTO;
import com.lushihao.ewhatbackend.model.entity.BlogComment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.ewhatbackend.model.vo.BlogCommentVO;
import com.lushihao.ewhatbackend.model.vo.BlogVO;

/**
* @author lushihao
* @description 针对表【tb_blog_comments(博文评论)】的数据库操作Service
* @createDate 2025-11-05 22:13:49
*/
public interface BlogCommentService extends IService<BlogComment> {

    Long saveBlogComment(BlogCommentDTO blogCommentDTO);

    Boolean deleteBlogComment(Long id);

    Page<BlogCommentVO> queryCommentByBlogIdWithPage(Long id, PageRequest pageRequest);

    Boolean likeBlogComment(Long id);
}

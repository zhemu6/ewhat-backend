package com.lushihao.ewhatbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.model.entity.BlogComments;
import com.lushihao.ewhatbackend.service.BlogCommentsService;
import com.lushihao.ewhatbackend.mapper.BlogCommentsMapper;
import org.springframework.stereotype.Service;

/**
* @author lushihao
* @description 针对表【tb_blog_comments(博文评论)】的数据库操作Service实现
* @createDate 2025-11-05 22:13:49
*/
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments>
    implements BlogCommentsService{

}





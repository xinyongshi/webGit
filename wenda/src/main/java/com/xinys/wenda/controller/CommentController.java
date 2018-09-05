package com.xinys.wenda.controller;

import com.xinys.wenda.model.Comment;
import com.xinys.wenda.model.EntityType;
import com.xinys.wenda.model.HostHolder;
import com.xinys.wenda.service.CommentService;
import com.xinys.wenda.service.QuestionService;
import com.xinys.wenda.service.SensitiveService;
import com.xinys.wenda.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;

/**
 * 评论Controller
 */
@Controller
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);


    @Autowired
    private SensitiveService sensitiveService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private QuestionService questionService;

    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})
    public String addComment(@RequestParam("questionId") int questionId,
                             @RequestParam("content") String content) {

        try{
             // 过滤掉脚本内容和过滤敏感词
            content = HtmlUtils.htmlEscape(content);
            content = sensitiveService.doFilter(content);
            Comment comment = new Comment();
            if (hostHolder.getUser() != null){
                comment.setUserId(hostHolder.getUser().getId());
            }else {
                comment.setUserId(WendaUtil.ANONYMOUS_USERID);// 匿名用户
            }
            comment.setContent(content); // 设置评论内容
            comment.setEntityId(questionId); //设置评论的问题id
            comment.setEntityType(EntityType.ENTITY_QUESTION); //设置问题内容
            comment.setCreatedDate(new Date()); // 设置日期
            comment.setStatus(0); // 1 表示删除评论不显示

            // 开始添加评论到数据库
            commentService.addComment(comment);

            // 更新题目里的评论数量
            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            questionService.updateCommentCount(comment.getEntityId(), count);
        }catch (Exception e) {
            logger.error("增加评论失败" + e.getMessage());
        }

        return "redirect:/question/" + String.valueOf(questionId);

    }
}

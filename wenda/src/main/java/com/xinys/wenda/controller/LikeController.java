package com.xinys.wenda.controller;

import com.xinys.wenda.async.EventModel;
import com.xinys.wenda.async.EventProducer;
import com.xinys.wenda.async.EventType;
import com.xinys.wenda.model.Comment;
import com.xinys.wenda.model.EntityType;
import com.xinys.wenda.model.HostHolder;
import com.xinys.wenda.service.CommentService;
import com.xinys.wenda.service.LikeService;
import com.xinys.wenda.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 赞踩操作
 */
@Controller
public class LikeController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 点赞操作
     * @param commentId
     * @return
     */
    @RequestMapping(value = {"/like"}, method = {RequestMethod.POST})
    @ResponseBody
    public String like(@RequestParam("commentId") int commentId) {
        System.out.println("liked");
        if (hostHolder.getUser() == null){
            return WendaUtil.getJSONString(999);// 未登录，重新登录
        }
        long likeCount = likeService.like(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT,commentId);
       // 点赞结束，站内信通知对方被赞了
        Comment comment = commentService.getCommentById(commentId);
        eventProducer.fireEvent(new EventModel(EventType.LIKE)
                .setEntityId(commentId)
                .setEntityType(EntityType.ENTITY_COMMENT)
                .setActorId(hostHolder.getUser().getId())
                .setEntityOwnerId(comment.getUserId())
                .setExt("questionId",String.valueOf(comment.getEntityId()))
        );
        return WendaUtil.getJSONString(0,String.valueOf(likeCount));
    }


    @RequestMapping(value = {"/dislike"}, method = {RequestMethod.POST})
    @ResponseBody
    public String dislike(@RequestParam("commentId") int commentId) {
        System.out.println("dislike");
        if (hostHolder.getUser() == null){
            return WendaUtil.getJSONString(999);// 未登录，重新登录
        }
        long likeCount = likeService.dislike(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT,commentId);
        return WendaUtil.getJSONString(0,String.valueOf(likeCount));
    }
}

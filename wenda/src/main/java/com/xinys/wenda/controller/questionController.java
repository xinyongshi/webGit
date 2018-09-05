package com.xinys.wenda.controller;

import com.xinys.wenda.dao.QuestionDAO;
import com.xinys.wenda.model.*;
import com.xinys.wenda.service.CommentService;
import com.xinys.wenda.service.LikeService;
import com.xinys.wenda.service.QuestionService;
import com.xinys.wenda.service.UserService;
import com.xinys.wenda.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.jws.WebParam;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 发布问题的操作
 */
@Controller
public class questionController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private QuestionService questionService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(value = "/question/add",method = RequestMethod.POST)
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,
                              @RequestParam("content") String content){
       try {
           Question question = new Question();
           question.setContent(content);
           question.setTitle(title);
           question.setCreatedDate(new Date());
           if (hostHolder.getUser() == null) {
//               return WendaUtil.getJSONString(999);
               return WendaUtil.getJSONString(WendaUtil.ANONYMOUS_USERID);
           } else {
               question.setUserId(hostHolder.getUser().getId());
           }
           if (questionService.addQuestion(question) > 0) {
               return WendaUtil.getJSONString(0); // 表示添加成功
           }
       }catch (Exception e){
           logger.error("添加问题失败："+e.getMessage());
       }
        return WendaUtil.getJSONString(1,"添加问题失败");
    }


    @RequestMapping(value = "/question/{qid}",method = RequestMethod.GET)
    public String selectQuestion(Model model, @PathVariable("qid") int qid){
             Question question = questionService.selectById(qid);
             model.addAttribute("question",question);
        List<Comment> commentList = commentService.getCommentsByEntity(qid, EntityType.ENTITY_QUESTION);
        List<ViewObject> vos = new ArrayList<>();
        for (Comment comment:commentList
             ) {
            ViewObject vo = new ViewObject();
            vo.set("comment",comment);
            if (hostHolder.getUser() == null){
                vo.set("liked",0);
            }else {
                vo.set("liked",likeService.getLikeStatus(hostHolder.getUser().getId(),EntityType.ENTITY_COMMENT,comment.getId()));
            }
            vo.set("likeCount",likeService.getLikeCount(EntityType.ENTITY_COMMENT,comment.getId()));
            vo.set("user",userService.getUser(comment.getUserId()));
            vos.add(vo);
        }
        model.addAttribute("comments", vos);
        return "detail";
    }


}
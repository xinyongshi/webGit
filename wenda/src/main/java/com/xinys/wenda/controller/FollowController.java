package com.xinys.wenda.controller;

import com.xinys.wenda.async.EventModel;
import com.xinys.wenda.async.EventProducer;
import com.xinys.wenda.async.EventType;
import com.xinys.wenda.model.*;
import com.xinys.wenda.service.CommentService;
import com.xinys.wenda.service.FollowService;
import com.xinys.wenda.service.QuestionService;
import com.xinys.wenda.service.UserService;
import com.xinys.wenda.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController {

    @Autowired
    FollowService followService;

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    EventProducer eventProducer;



    @RequestMapping(path = {"/followUser"},
            method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String followUser(@RequestParam("userId") int userId) {
        System.out.println("follow");
        if (hostHolder.getUser() == null){
            return WendaUtil.getJSONString(999);
        }

        boolean ret = followService.follow(hostHolder.getUser().getId(),
                EntityType.ENTITY_USER,userId);
        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setEntityOwnerId(userId)
                .setActorId(hostHolder.getUser().getId())
                .setEntityType(EntityType.ENTITY_USER)
                .setEntityId(userId)
        );
        // 如果关注成功，返回此用户的所有关注对象
        return WendaUtil.getJSONString(ret ? 0 : 1,
                String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(), EntityType.ENTITY_USER)));


    }


    @RequestMapping(path = {"/unfollowUser"},
            method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String unfollowUser(@RequestParam("userId") int userId) {

        System.out.println("unfollow");
        if (hostHolder.getUser() == null){
            return WendaUtil.getJSONString(999);
        }

        boolean ret = followService.unfollow(hostHolder.getUser().getId(),
                EntityType.ENTITY_USER,userId);
        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setEntityOwnerId(userId)
                .setActorId(hostHolder.getUser().getId())
                .setEntityType(EntityType.ENTITY_USER)
                .setEntityId(userId)
        );
        // 如果取消关注成功，返回此用户的所有关注对象
        return WendaUtil.getJSONString(ret ? 0 : 1,
                String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(), EntityType.ENTITY_USER)));


    }


    @RequestMapping(path = {"/followQuestion"},
            method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String followQuestion(@RequestParam("questionId") int questionId) {

        if (hostHolder.getUser() == null){
            return WendaUtil.getJSONString(999);
        }

        Question q = questionService.selectById(questionId);
        if (q == null){
            return WendaUtil.getJSONString(1,"问题不存在");
        }

        boolean ret = followService.follow(hostHolder.getUser().getId(),
                EntityType.ENTITY_QUESTION,questionId);
        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setEntityOwnerId(questionId)
                .setActorId(hostHolder.getUser().getId())
                .setEntityType(EntityType.ENTITY_USER)
                .setEntityId(questionId)
        );

        Map<String,Object> info = new HashMap<String,Object>();
        info.put("headUrl",hostHolder.getUser().getHeadUrl());
        info.put("name",hostHolder.getUser().getName());
        info.put("id",hostHolder.getUser().getId());
        info.put("count",followService.getFollowerCount(EntityType.ENTITY_QUESTION,questionId));

        // 如果关注成功，返回此用户的所有关注对象
        return WendaUtil.getJSONString(ret ? 0:1,info);
    }

    @RequestMapping(path = {"/unfollowQuestion"},
            method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String unfollowQuestion(@RequestParam("questionId") int questionId) {

        if (hostHolder.getUser() == null){
            return WendaUtil.getJSONString(999);
        }

        Question q = questionService.selectById(questionId);
        if (q == null){
            return WendaUtil.getJSONString(1,"问题不存在");
        }

        boolean ret = followService.unfollow(hostHolder.getUser().getId(),
                EntityType.ENTITY_QUESTION,questionId);
        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setEntityOwnerId(questionId)
                .setActorId(hostHolder.getUser().getId())
                .setEntityType(EntityType.ENTITY_USER)
                .setEntityId(questionId)
        );

        Map<String,Object> info = new HashMap<String,Object>();
        info.put("headUrl",hostHolder.getUser().getHeadUrl());
        info.put("name",hostHolder.getUser().getName());
        info.put("id",hostHolder.getUser().getId());
        info.put("count",followService.getFollowerCount(EntityType.ENTITY_QUESTION,questionId));

        // 如果关注成功，返回此用户的所有关注对象
        return WendaUtil.getJSONString(ret ? 0:1,info);
    }


    /**
     * 查看此用户的粉丝详情
     * @param model
     * @param userId
     * @return
     */
    @RequestMapping(path = {"/user/{uid}/followers"}, method = {RequestMethod.GET})
    public String followers(Model model, @PathVariable("uid") int userId) {
        List<Integer> followerIds = followService.getFollowers(EntityType.ENTITY_USER,userId,0,10);
        if (hostHolder.getUser() != null) {
            model.addAttribute("followers", getUsersInfo(hostHolder.getUser().getId(), followerIds));
        } else {
            model.addAttribute("followers", getUsersInfo(0, followerIds));
        }

        model.addAttribute("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        model.addAttribute("curUser", userService.getUser(userId));
        return "followers";
    }


    /**
     * 获取此用户的关注详情
     * @param model
     * @param userId
     * @return
     */
    @RequestMapping(path = {"/user/{uid}/followees"}, method = {RequestMethod.GET})
    public String followees(Model model, @PathVariable("uid") int userId) {
        List<Integer> followeeIds = followService.getFollowees(userId,EntityType.ENTITY_USER,0,10);
        if (hostHolder.getUser() != null) {
            model.addAttribute("followees", getUsersInfo(hostHolder.getUser().getId(), followeeIds));
        } else {
            model.addAttribute("followees", getUsersInfo(0, followeeIds));
        }

        model.addAttribute("followeeCount", followService.getFolloweeCount(userId,EntityType.ENTITY_USER));
        model.addAttribute("curUser", userService.getUser(userId));
        return "followees";
    }



    private List<ViewObject> getUsersInfo(int localUserId, List<Integer> userIds) {
        List<ViewObject> viewObjects = new ArrayList<ViewObject>();
        for (Integer uid : userIds) {
            User user = userService.getUser(uid);
            if (user == null) {
                  continue;
            }
            ViewObject vo = new ViewObject();
            vo.set("user",user);
            vo.set("commentCount",commentService.getUserCommentCount(uid));
            vo.set("followerCount",followService.getFollowerCount(EntityType.ENTITY_USER,uid));
            vo.set("followeeCount",followService.getFolloweeCount(uid,EntityType.ENTITY_USER));
            if (localUserId != 0){
                vo.set("followed",followService.isFollower(localUserId,EntityType.ENTITY_USER,uid));
            }else {
                vo.set("followed",false);
            }
            viewObjects.add(vo);
        }
        return viewObjects;
    }

}

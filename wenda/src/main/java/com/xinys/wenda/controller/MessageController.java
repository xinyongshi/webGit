package com.xinys.wenda.controller;

import com.xinys.wenda.model.HostHolder;
import com.xinys.wenda.model.Message;
import com.xinys.wenda.model.User;
import com.xinys.wenda.model.ViewObject;
import com.xinys.wenda.service.MessageService;
import com.xinys.wenda.service.UserService;
import com.xinys.wenda.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;


    /**
     * 所有与用户有关的私聊信息
     * @param model
     * @return
     */
    @RequestMapping(path = {"/msg/list"}, method = {RequestMethod.GET})
    public String conversationDetail(Model model) {
        try {
            int localUserId = hostHolder.getUser().getId(); // 获取当前登录用户
            List<ViewObject> conversations = new ArrayList<ViewObject>();
            List<Message> conversationList = messageService.getConversationList(localUserId, 0, 10);
            for (Message msg : conversationList) {
                ViewObject vo = new ViewObject();
                vo.set("conversation",msg);
                //
                int targetId = msg.getFromId() == localUserId ? msg.getToId() : msg.getFromId();
                User user = userService.getUser(targetId);
                vo.set("user",user);
                // 未读信息
                vo.set("unread", messageService.getConvesationUnreadCount(localUserId, msg.getConversationId()));
                conversations.add(vo);
            }
            model.addAttribute("conversations", conversations);
        }catch (Exception e) {
            logger.error("获取站内信列表失败" + e.getMessage());
        }
        return "letter";
    }


    /**
     * 用户私聊信息的具体交谈内容
     * @param model
     * @param conversationId
     * @return
     */
    @RequestMapping(value = {"/msg/detail"}, method = {RequestMethod.GET})
    public String conversationDetail(Model model,
                                     @RequestParam("conversationId") String conversationId) {
        try {
            List<Message> conversationList = messageService.getConversationDetail(conversationId, 0, 10);
            List<ViewObject> messages = new ArrayList<>();
            for (Message msg:conversationList) {

                ViewObject viewObject = new ViewObject();
                viewObject.set("message",msg);
                User user = userService.getUser(msg.getFromId());

                if (user == null){
                    continue;
                }
                if(messageService.updateRead(hostHolder.getUser().getId(),conversationId) > 0){
                    viewObject.set("headUrl", user.getHeadUrl());
                    viewObject.set("userId", user.getId());
                    messages.add(viewObject);
                }
            }
           model.addAttribute("messages",messages);
        } catch (Exception e){
           logger.error("获取详情消息失败" + e.getMessage());
        }

        return "letterDetail";

    }


    @RequestMapping(path = {"/msg/addMessage"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("toName") String toName,
                             @RequestParam("content") String content) {

        try {
            if (hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999, "未登录");
            }
            User toUser = userService.selectByName(toName);
            if (toUser == null) {
                return WendaUtil.getJSONString(1, "用户不存在");
            }

            Message msg = new Message();
            msg.setContent(content);
            msg.setFromId(hostHolder.getUser().getId());
            msg.setToId(toUser.getId());
            msg.setCreatedDate(new Date());
            String conversationId =hostHolder.getUser().getId()+"_"+toUser.getId();
            msg.setConversationId(conversationId);
            messageService.addMessage(msg);
            return WendaUtil.getJSONString(0);
        } catch (Exception e) {
            logger.error("增加站内信失败" + e.getMessage());
            return WendaUtil.getJSONString(1, "插入站内信失败");
        }
    }


    @RequestMapping(path = {"/msg/jsonAddMessage"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("fromId") int fromId,
                             @RequestParam("toId") int toId,
                             @RequestParam("content") String content) {

        try {
            Message msg = new Message();
            String conversationId = fromId+"_"+toId;
            msg.setContent(content);
            msg.setFromId(fromId);
            msg.setToId(toId);
            msg.setCreatedDate(new Date());
            msg.setConversationId(conversationId);
            messageService.addMessage(msg);
            return WendaUtil.getJSONString(msg.getId());
        }catch (Exception e) {
            logger.error("增加评论失败" + e.getMessage());
            return WendaUtil.getJSONString(1, "插入评论失败");
        }



    }
}

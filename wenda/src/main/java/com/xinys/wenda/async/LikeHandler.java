package com.xinys.wenda.async;

import com.xinys.wenda.model.Message;
import com.xinys.wenda.model.User;
import com.xinys.wenda.service.MessageService;
import com.xinys.wenda.service.UserService;
import com.xinys.wenda.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class LikeHandler implements EventHandler {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;


    @Override
    public void doHandle(EventModel eventModel) {
        Message message = new Message();
        int fromId = WendaUtil.SYSTEM_USERID;
        int toId = eventModel.getEntityOwnerId();
        message.setFromId(fromId);
        message.setToId(toId);
        message.setCreatedDate(new Date());
        User user = userService.getUser(eventModel.getActorId());
        String msgAdvice = "用户"+user.getName()+"赞了你的评论,http://127.0.0.1:8080/question/"
                +eventModel.getExt("questionId");
        message.setContent(msgAdvice);
        message.setConversationId(fromId + "_" + toId);
        messageService.addMessage(message);

    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);
    }
}

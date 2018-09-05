package com.xinys.wenda.async;

import com.alibaba.fastjson.JSON;
import com.xinys.wenda.util.JedisAdapter;
import com.xinys.wenda.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 取出redis队列中的事件
 */
@Service
public class EventConsumer implements InitializingBean,ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private Map<EventType,List<EventHandler>> config = new HashMap<EventType,List<EventHandler>>();

    private ApplicationContext applicationContext;

    @Autowired
    private JedisAdapter jedisAdapter;

    @Override
    public void afterPropertiesSet() throws Exception {

        // 表示返回实现EventHandler接口的实例，String为名字，EventHandler为实例对象
        Map<String,EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if (beans != null){
            for (Map.Entry<String,EventHandler> entry : beans.entrySet()){
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();
                for (EventType eventType : eventTypes){
                    // 得到具体的某一个事件，比如点赞事件
                    if(!config.containsKey(eventType)){
                        config.put(eventType,new ArrayList<EventHandler>());
                    }
                    // 把eventType与具体的eventHandler绑定一起
                    config.get(eventType).add(entry.getValue());
                }

            }
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    String key = RedisKeyUtil.getEventQueueKey();
                    List<String> events = jedisAdapter.brpop(0,key);
                    for (String event : events){
                        if (event.equals(key)){
                            continue;

                        }
                        EventModel eventModel = JSON.parseObject(event,EventModel.class);
                        if (!config.containsKey(eventModel.getType())){
                            logger.error("不能识别的事件");
                            continue;
                        }
                        for (EventHandler eventHandler : config.get(eventModel.getType())){
                            eventHandler.doHandle(eventModel);

                        }
                    }

                }
            }
        });
        thread.start();

    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

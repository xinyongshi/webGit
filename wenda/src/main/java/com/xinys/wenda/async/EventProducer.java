package com.xinys.wenda.async;

import com.alibaba.fastjson.JSON;
import com.xinys.wenda.util.JedisAdapter;
import com.xinys.wenda.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 事件发布到队列中
 */
@Service
public class EventProducer {


    @Autowired
    private JedisAdapter jedisAdapter;


    /**
     * 把事件加入到redis队列中
     * @param eventModel
     * @return true表示加入成功
     */
    public boolean fireEvent(EventModel eventModel){
        try {
            String json = JSON.toJSONString(eventModel);
            String key = RedisKeyUtil.getEventQueueKey();
            jedisAdapter.lpush(key,json);
            return true;
        }catch (Exception e){
            return false;
        }
    }



}

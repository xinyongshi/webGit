package com.xinys.wenda.service;

import com.xinys.wenda.util.JedisAdapter;
import com.xinys.wenda.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private JedisAdapter jedisAdapter;

    public long like(int userId,int entityType,int entityId){
        String likeKey = RedisKeyUtil.getLikeKey(entityType,entityId);
        jedisAdapter.sadd(likeKey,String.valueOf(userId));

        String dislikeKey = RedisKeyUtil.getDisLikeKey(entityType,entityId);
        jedisAdapter.srem(dislikeKey,String.valueOf(userId));

        return jedisAdapter.scard(likeKey);

    }

    public long dislike(int userId,int entityType,int entityId){
        String dislikeKey = RedisKeyUtil.getDisLikeKey(entityType,entityId);
        String likeKey = RedisKeyUtil.getLikeKey(entityType,entityId);
        jedisAdapter.sadd(dislikeKey,String.valueOf(userId));
        jedisAdapter.srem(likeKey,String.valueOf(userId));
        return jedisAdapter.scard(likeKey);

    }


    public long getLikeCount(int entityType,int entityId){
        String likeKey = RedisKeyUtil.getLikeKey(entityType,entityId);
        return jedisAdapter.scard(likeKey);
    }


    public int getLikeStatus(int userId,int entityType,int entityId){
        String likeKey = RedisKeyUtil.getLikeKey(entityType,entityId);
        if (jedisAdapter.sismember(likeKey,String.valueOf(userId))){
            return 1;
        }
        String dislikeKey = RedisKeyUtil.getDisLikeKey(entityType,entityId);
       return jedisAdapter.sismember(dislikeKey,String.valueOf(userId)) ? -1 : 0;
    }

}

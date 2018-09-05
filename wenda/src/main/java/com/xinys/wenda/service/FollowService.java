package com.xinys.wenda.service;

import com.xinys.wenda.util.JedisAdapter;
import com.xinys.wenda.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 通用的关注业务逻辑
 */
@Service
public class FollowService {

    @Autowired
    private JedisAdapter jedisAdapter;


    /**
     * 用户关注了某个实体，实体可以是用户、问题以及评论等
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean follow(int userId,int entityType,int entityId){
        // 获取粉丝的Key
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        // 获取关注对象的Key
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        Date date = new Date();
        Jedis jedis = jedisAdapter.getJedis();
        Transaction tx = jedisAdapter.multi(jedis);

        // userId成为entityId的粉丝
        tx.zadd(followerKey,date.getTime(),String.valueOf(userId));
        // entityId成为userId的关注对象
        tx.zadd(followeeKey,date.getTime(),String.valueOf(entityId));

        List<Object> result = jedisAdapter.exec(tx,jedis);
        return result.size() == 2 && (long)result.get(0) > 0 && (long)result.get(1) > 0;

    }


    /**
     * 取消关注实体
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean unfollow(int userId,int entityType,int entityId){
        // 获取粉丝的Key
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        // 获取关注对象的Key
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        Date date = new Date();
        Jedis jedis = jedisAdapter.getJedis();
        Transaction tx = jedisAdapter.multi(jedis);

        // userId取消成为entityId的粉丝
        tx.zrem(followerKey,String.valueOf(userId));
        // entityId取消成为userId的关注对象
        tx.zrem(followeeKey,String.valueOf(entityId));

        List<Object> result = jedisAdapter.exec(tx,jedis);
        return result.size() == 2 && (long)result.get(0) > 0 && (long)result.get(1) > 0;

    }


    /**
     * 获取entityId所有粉丝列表
     * @param count
     * @param entityType
     * @param entityId
     * @return
     */
    public List<Integer> getFollowers(int entityType, int entityId, int count){
        // 获取粉丝的Key
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return getIdsFromSet(jedisAdapter.zrevrange(followerKey, 0, count));
    }

    /**
     * entityId的分页粉丝列表
     * @param entityType
     * @param entityId
     * @param offset
     * @param count
     * @return
     */
    public List<Integer> getFollowers(int entityType, int entityId, int offset, int count){
        // 获取粉丝的Key
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return getIdsFromSet(jedisAdapter.zrevrange(followerKey, offset, count));
    }

    /**
     * 获取userId的所有关注对象列表
     * @param userId
     * @param entityType
     * @param count
     * @return
     */
    public List<Integer> getFollowees(int userId, int entityType, int count){
        // 获取粉丝的Key
        String followerKey = RedisKeyUtil.getFollowerKey(userId, entityType);
        return getIdsFromSet(jedisAdapter.zrevrange(followerKey, 0, count));
    }

    /**
     * 获取userId的分页关注对象列表
     * @param userId
     * @param entityType
     * @param offset
     * @param count
     * @return
     */
    public List<Integer> getFollowees(int userId, int entityType, int offset, int count) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return getIdsFromSet(jedisAdapter.zrevrange(followeeKey, offset, offset+count));
    }

    /**
     * 获取粉丝数量
     * @param entityType
     * @param entityId
     * @return
     */
    public long getFollowerCount(int entityType, int entityId) {
        // 此key的粉丝数量
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return jedisAdapter.zcard(followerKey);
    }

    /**
     * 获取关注数量
     * @param userId
     * @param entityType
     * @return
     */
    public long getFolloweeCount(int userId, int entityType) {
        // 此key的关注数量
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return jedisAdapter.zcard(followeeKey);
    }


    /**
     *
     * @param idset
     * @return
     */
    private List<Integer> getIdsFromSet(Set<String> idset) {
        List<Integer> ids = new ArrayList<>();// 创建List
        for (String str : idset) {
            // 把Set中的元素添加到List中
            ids.add(Integer.parseInt(str));
        }
        return ids;
    }

    /**
     *  判断用户是否关注了某个实体
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean isFollower(int userId, int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return jedisAdapter.zscore(followerKey, String.valueOf(userId)) != null;
    }
}

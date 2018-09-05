package com.xinys.wenda.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * redis操作
 */
@Service
public class JedisAdapter  implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(JedisAdapter.class);
    private JedisPool pool;


    @Override
    public void afterPropertiesSet() throws Exception {
        pool = new JedisPool("redis://localhost:6379/10");
    }

    public  long sadd(String key,String value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sadd(key,value);
        }catch (Exception e){
            logger.error("保存到redis发生异常："+e.getMessage());
            return 0;
        }finally {
            jedis.close();
        }
    }

    public long srem(String key,String value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.srem(key,value);
        }catch (Exception e){
            logger.error("保存到redis发生异常："+e.getMessage());
            return 0;
        }finally {
            jedis.close();
        }
    }


    public long scard(String key){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.scard(key);
        }catch (Exception e){
            logger.error("保存到redis发生异常："+e.getMessage());
            return 0;
        }finally {
            jedis.close();
        }
    }

    public boolean sismember(String key,String value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sismember(key,value);
        }catch (Exception e){
            logger.error("保存到redis发生异常："+e.getMessage());
            return false;
        }finally {
            jedis.close();
        }
    }

    public long lpush(String key,String value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
           return jedis.lpush(key,value);
        }catch (Exception e){
            logger.error("保存到redis发生异常："+e.getMessage());
            return 0;
        }finally {
            jedis.close();
        }
    }

    public List<String> brpop(int var1, String var2){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return  jedis.brpop(var1,var2);
        }catch (Exception e){
            logger.error("保存到redis发生异常："+e.getMessage());
            return null;
        }finally {
            jedis.close();
        }
    }

    public Jedis getJedis() {
        return pool.getResource();
    }

    public Transaction multi(Jedis jedis){
       try {
           return jedis.multi();
       }catch (Exception e){
           logger.error("redis开启事务发生异常："+e.getMessage());
       }
       return null;
    }

    public List<Object> exec(Transaction tx,Jedis jedis){
        try{
            return tx.exec();
        }catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
            tx.discard();
        } finally {
            if (tx != null) {
                try {
                    tx.close();
                } catch (IOException ioe) {
                    // ..
                    logger.error("redis关闭事务发生异常："+ioe.getMessage());
                }
            }

            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }



    public long zadd(String key, double score, String value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zadd(key,score,value);
        }catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public long zrem(String key, String value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zrem(key,value);
        }catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

public Set<String> zrevrange(String key, int start, int end){
    Jedis jedis = null;
    try {
        jedis = pool.getResource();
        return jedis.zrevrange(key,start,end);
    }catch (Exception e) {
        logger.error("发生异常" + e.getMessage());
    } finally {
        if (jedis != null) {
            jedis.close();
        }
    }
    return null;
}

    public Set<String> zrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zrange(key, start, end);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public long zcard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zcard(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public Double zscore(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zscore(key, member);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }



}

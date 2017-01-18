package com.serli.oracle.of.bacon.repository;

import java.util.List;
import redis.clients.jedis.Jedis;

public class RedisRepository {
    private Jedis jedis;
    private int index=0;

    public RedisRepository(){
        jedis = new Jedis("localhost");
        System.out.println("Connection to server successful");

    }

    public List<String> getLastTenSearches() {
        List<String>  lastTenSearches =
                jedis.lrange("last-ten-searches",0,index);
        return lastTenSearches;
    }

    public void addSearch(String actorName) {
        jedis.lpush("last-ten-searches", actorName);
        if(index<9){
            index++;
        }
    }
}

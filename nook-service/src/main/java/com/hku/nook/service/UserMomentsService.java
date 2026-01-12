package com.hku.nook.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hku.nook.dao.UserMomentsDao;
import com.hku.nook.domain.UserMoment;
import com.hku.nook.domain.constant.UserMomentsConstant;
import com.hku.nook.service.util.RocketMQUtil;

@Service
public class UserMomentsService {

    @Autowired
    private UserMomentsDao userMomentsDao;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void addUserMoments(UserMoment userMoment) throws Exception {
        userMoment.setCreateTime(new Date());
        userMomentsDao.addUserMoments(userMoment);
        DefaultMQProducer producer = (DefaultMQProducer) applicationContext.getBean("momentsProducer");
        Message msg = new Message(
            UserMomentsConstant.TOPIC_MOMENTS, 
            JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8)
        );
        RocketMQUtil.syncSendMsg(producer, msg);
    }

    public List<UserMoment> getUserSubscribedMoments(Long userId) {
        String key = "subscribed-" + userId;
        String momentsStr = redisTemplate.opsForValue().get(key);
        return JSONArray.parseArray(momentsStr, UserMoment.class);   
    }
}

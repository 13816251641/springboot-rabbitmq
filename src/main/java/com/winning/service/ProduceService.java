package com.winning.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winning.config.MyRabbitMqConfig;
import com.winning.entity.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ProduceService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /*橙联项目发送消息的方式*/
    public void sendDirect() {
        String uuid = UUID.randomUUID().toString();
        CorrelationData cd = new CorrelationData(uuid);
        Person person = new Person();
        person.setName("lujieni");
        person.setSex("男");
        person.setAge(27);
        /*将对象转为json形式的字符串*/
        String content = JSON.toJSONString(person);
        try {
            rabbitTemplate.convertAndSend(MyRabbitMqConfig.BUSINESS_EXCHANGE_NAME, MyRabbitMqConfig.BUSINESS_ROUTING_KEY, content, cd);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /*
        完美发送消息的方式(包含了ConfirmCallback和ReturnCallback需要的回调的参数)
     */
    public void perfectSendDirect() {
        String uuid = UUID.randomUUID().toString();
        CorrelationData cd = new CorrelationData(uuid);
        try {
            Map<String, Object> content = new HashMap<>();
            content.put("a", "a");
            content.put("content", "content");
            ObjectMapper om = new ObjectMapper();
            /*将对象转为json形式的字符串*/
            String s = om.writeValueAsString(content);
            Message message = MessageBuilder.withBody(s.getBytes()).
                    setContentType(MessageProperties.CONTENT_TYPE_JSON).
                    setCorrelationId(uuid).build();
            rabbitTemplate.convertAndSend(MyRabbitMqConfig.BUSINESS_EXCHANGE_NAME, MyRabbitMqConfig.BUSINESS_ROUTING_KEY, message, cd);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

package com.winning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    /*确保消息能发送到rabbitmq中,如果没有可以从回调中知道*/
    public void sendDirect() {
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
            rabbitTemplate.convertAndSend("my.direct.exchange", "my.direct.queue1", message, cd);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        /*
            这样发送会导致回调事件correlationData参数为空
            Map<String, Object> content = new HashMap<>();
            content.put("a", "a");
            content.put("content", "content");
            rabbitTemplate.convertAndSend("my.direct.exchange","my.direct.queue",content);
        */
    }
}

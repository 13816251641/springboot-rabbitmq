package com.winning.service;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Package: com.winning.service
 * @ClassName: Consumer2Service
 * @Author: lujieni
 * @Description: 测试橙联代码
 * @Date: 2020-12-21 11:33
 * @Version: 1.0
 */
@Service
@Slf4j
public class Consumer2Service {


    @RabbitListener(queues = "deductBusinessQueue")
    public void receiveOne(Message message, Channel channel){
        try {
            /*
               如果ack方式为manual的话就必须手工应答,
               true表示回退到queue中,false表示如果没有
               绑定死信队列,消息丢失
             */
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
}
package com.winning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.winning.dao.StudentMapper;
import com.winning.entity.EventNotifierInputDTO;
import com.winning.entity.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
    消费消息服务
 */
@Service
@Slf4j
public class ConsumerService {
    @Autowired
    private StudentMapper studentMapper;


    @RabbitListener(queues = "winning.dcg.event.collector.queue")
    public void consume(Message message,Channel channel) throws Exception{
        log.info("consume123321");
        EventNotifierInputDTO dto = new ObjectMapper().readValue(message.getBody(), EventNotifierInputDTO.class);
        int i = 5/0;
    }



    /*
        receiveOne和receiveTwo都是监听一个消息队列并且默认情况下如果不配置prefetch
        参数消息会灌满第一个消费者后才会灌满第二个消费者,默认第一个消费者会灌满
        Integer.MAX条数据
     */
    /* @RabbitListener(queues = "myqueue") */
    public void receiveOne(Message message,Channel channel){
        try {
            log.info("receiveOne");
            TimeUnit.SECONDS.sleep(5);
            byte[] body = message.getBody();
            String s = new String(body);
            log.info("receiveOne收到消息:" + s);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            /* true退回到queue中,false如果没有绑定死信队列,消息丢失 */
            //channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    /*
       receiveOne和receiveTwo都是监听一个消息队列并且默认情况下如果不配置prefetch
       参数消息会灌满第一个消费者后才会灌满第二个消费者,默认第一个消费者会灌满
       Integer.MAX条数据
    */
    /*@RabbitListener(queues = "myqueue")*/
    public void receiveTwo(Message message,Channel channel){
        try {
            log.info("receiveTwo");
            TimeUnit.SECONDS.sleep(5);
            byte[] body = message.getBody();
            String s = new String(body);
            log.info("receiveTwo收到消息:" + s);
            //channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            /* true退回到queue中,false如果没有绑定死信队列,消息丢失 */
            //channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    /*
       模拟处理死信队列中的消息
     */
    //@RabbitListener(queues = "queue-dead")
    public void queueDead(Message message, Channel channel) throws IOException {
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }



    /*
       从消息中解析出entity
     */
    //@RabbitListener(queues = "myqueue")
    public void readMsg(Message message, Channel channel) throws IOException {
        try {
            log.info("readMsg");
            Map map = new ObjectMapper().readValue(message.getBody(), Map.class);
            String content = (String) map.get("content");
            log.info("收到消息:" + content);
        }catch (Exception e){
            /* 拒绝消息并回退queue */
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }


    /* @RabbitListener(queues = "queue-sqlserver") */
    public void consumer(Message message, Channel channel){
        try {
            String name=new String(message.getBody());
            //TODO 消息重复校验
            Student student = new Student();
            student.setName(name);
            studentMapper.insertWithoutId(student);
            /* false:不批量 */
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            log.error(e.getMessage());
                /* 不入队,当设置了死信队列会到死信队列,如果没有设置就抛弃消息 */
            try {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
            } catch (IOException ex) {
                log.error(e.getMessage());
            }
        }
    }
}

package com.winning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.winning.dao.StudentMapper;
import com.winning.entity.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ConsumerService {
    private Logger logger = LoggerFactory.getLogger(ConsumerService.class);
    @Autowired
    private StudentMapper studentMapper;


    /*
        receiveOne和receiveTwo都是监听一个消息队列并且默认情况下如果不配置prefetch
        参数消息会灌满第一个消费者后才会灌满第二个消费者,默认第一个消费者会灌满
        Integer.MAX条数据
     */
    //@RabbitListener(queues = "myqueue")
    public void receiveOne(Message message,Channel channel){
        try {
            System.out.println("receiveOne");
            TimeUnit.SECONDS.sleep(5);
            byte[] body = message.getBody();
            String s = new String(body);
            System.out.println("receiveOne收到消息:" + s);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            //channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);//true退回到queue中,false如果没有绑定死信队列,消息丢失
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    /*
       receiveOne和receiveTwo都是监听一个消息队列并且默认情况下如果不配置prefetch
       参数消息会灌满第一个消费者后才会灌满第二个消费者,默认第一个消费者会灌满
       Integer.MAX条数据
    */
    //@RabbitListener(queues = "myqueue")
    public void receiveTwo(Message message,Channel channel){
        try {
            System.out.println("receiveTwo");
            TimeUnit.SECONDS.sleep(5);
            byte[] body = message.getBody();
            String s = new String(body);
            System.out.println("receiveTwo收到消息:" + s);
            //channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            //channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);//true退回到queue中,false如果没有绑定死信队列,消息丢失
        }catch (Exception e){
            System.out.println(e.getMessage());
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
            System.out.println("readMsg");
            byte[] body = message.getBody();
            ObjectMapper om = new ObjectMapper();
            Map map = om.readValue(body, Map.class);
            String content = (String) map.get("content");
            System.out.println("收到消息:" + content);
        }catch (Exception e){
            //channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);拒绝消息并回退queue
        }
    }


    //@RabbitListener(queues = "queue-sqlserver")
    public void consumer(Message message, Channel channel){
        try {
            String name=new String(message.getBody());
            if(!checkIsUnique(name)){ //防止重复消费
                logger.info(name+"重复,放入死信队列处理");
                /*
                   不回退queue,如果有设置死信队列,就去死信
                 */
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
                return;//reject完后一定要return,否则余下代码仍然会执行
            }
            Student student = new Student();
            student.setName(name);
            studentMapper.insertWithoutId(student);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);//false:不批量
        }catch (Exception e){
            logger.error(e.getMessage());
            try {
                //不入队,当设置了死信队列会到死信队列,如果没有设置就抛弃消息
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
            }catch (Exception e2){
                logger.error(e2.getMessage());
            }
        }
    }

    /**
     * 验证某一字段是否唯一
     * @return
     */
    private boolean checkIsUnique(String property) {
      /*  Long result = redisTemplate.opsForSet().add("unique", property);
        if(result!=null && result.equals(1L)){  //插入成功,唯一
            return true;
        }*/
        return false;
    }
}

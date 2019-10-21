package com.winning;

import com.winning.service.ProduceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试消息的发送接收&&交换机的创建&&队列的创建&&绑定
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootRabbitmqApplicationTests {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    AmqpAdmin amqpAdmin;

    /**
     * 测试创建广播类型交换机&队列&绑定关系 OK
     */
    @Test
    public void testCreateFanoutExchangeAndQueueAndBinding(){
        /* 创建广播交换机 DirectExchange默认持久化 */
        amqpAdmin.declareExchange(new FanoutExchange("my.fanout.exchange"));
        /* 创建queue */
        amqpAdmin.declareQueue(new Queue("my.fanout.queue",true));
        /* 创建绑定关系 */
        amqpAdmin.declareBinding(new Binding("my.fanout.queue",Binding.DestinationType.QUEUE,"my.fanout.exchange","hello",null));
    }


    /**
     * 测试创建直连交换机&队列&绑定关系 OK
     */
    @Test
    public void testCreateDirectExchangeAndQueueAndBinding(){
        /* 创建直连交换机 DirectExchange默认持久化 */
        amqpAdmin.declareExchange(new DirectExchange("my.direct.exchange"));
        /* 创建queue */
        amqpAdmin.declareQueue(new Queue("my.direct.queue",true));
        /* 创建绑定关系 */
        amqpAdmin.declareBinding(new Binding("my.direct.queue",Binding.DestinationType.QUEUE,"my.direct.exchange","my.direct.queue",null));
    }



    /**
     * 测试发送消息到直连交换机中 OK
     */
    @Test
    public void testSendMessageToDirectExchange() {
        /*
           rabbitTemplate.send(exchange,routeKey,message);
           利用此api发送消息需要自己定义消息体内容和消息头
         */
        /*
           rabbitTemplate.convertAndSend(exchange,routeKey,object);
           object默认当成消息体,只需要传入要发送的对象,自动序列化发送给rabbitmq;
         */
        Map<String,Object> map = new HashMap<>();
        map.put("msg","这是springboot发送的消息哦");
        map.put("age",32);
        /*对象被默认序列化以后发送出去*/
        rabbitTemplate.convertAndSend("my.direct.exchange","my.direct.queue",map);
    }

    /**
       测试接收数据
     */
    @Test
    public void testReceiveMessage(){
        Object object = rabbitTemplate.receiveAndConvert("my.direct.queue");
        /*class java.util.HashMap*/
        System.out.println(object.getClass());
        System.out.println(object);
    }

    /**
     * 测试发送消息到广播交换机中 OK
     */
    @Test
    public void testSendMessageToFanoutExchange(){
        Map<String,Object> map = new HashMap<>();
        map.put("msg","这是springboot发送的消息");
        map.put("age",32);
        rabbitTemplate.convertAndSend("my.fanout.exchange","bye",map);
    }

}

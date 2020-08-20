package com.winning;

import com.winning.entity.Person;
import com.winning.service.ProduceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        amqpAdmin.declareBinding(new Binding("my.fanout.queue",Binding.DestinationType.QUEUE,"my.fanout.exchange","bye",null));
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
        amqpAdmin.declareBinding(new Binding("my.direct.queue",Binding.DestinationType.QUEUE,"my.direct.exchange","hello",null));
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
           object默认当成消息体,只需要传入要发送的对象,自动序列化发送给rabbitmq,
           因为rabbitTemplate已经配置好了自定义序列化方式!!!
           但是如果不配置CorrelationData那么当发送到exchange失败时,无法获知是哪一条消息失败;
           如果object不用message包装,那么当消息从交换机到队列失败的话无法从message中获得id
        */
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        Person person = new Person().setAge(29).setName("陆宝宝").setSex("男");
        rabbitTemplate.convertAndSend("my.direct.exchange","hello",person,correlationData);
    }

    /**
     * 测试发送消息到广播交换机中 OK
     */
    @Test
    public void testSendMessageToFanoutExchange(){
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        Map<String,Object> map = new HashMap<>();
        map.put("msg","这是springboot发送的消息");
        map.put("age",32);
        rabbitTemplate.convertAndSend("my.fanout.exchange","ggg",map,correlationData);
    }

}

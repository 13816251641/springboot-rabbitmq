package com.winning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.rabbitmq.client.Channel;
import com.winning.entity.EventNotifierInputDTO;
import com.winning.entity.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
    消费消息服务
 */
@Slf4j
public class ConsumerService implements InitializingBean {

    private Jackson2JsonMessageConverter jackson2JsonMessageConverter;

    @Autowired
    private Queue createDeadQueue;

    @Autowired
    private ConnectionFactory connectionFactory;

    @RabbitListener(queues = "my.direct.queue")
    public void consumeWithAuto(Channel channel) throws Exception{
        System.out.println("wrong");
        //int i = 5/0;
        //throw new MessageConversionException("a");
    }


    /**
     * 自动进行应答
     * 配置xml中配置了ack为auto,并开启了retry设置,
     * retry最大次数为3,当出现异常的时候会重试3次
     * 后才会最终往外抛出异常,这里重试都是消费端的
     * 事情和rabbitmq服务器没有关系
     * @param message
     * @param channel
     * @throws Exception
     */
    @RabbitListener(queues = "winning.dcg.event.collector.queue")
    public void consumeWithAutoAckAndRetry(Message message,Channel channel) throws Exception{
        log.info("调用了consume!!!");
        EventNotifierInputDTO dto = new ObjectMapper().readValue(message.getBody(), EventNotifierInputDTO.class);
        int i = 5/0;
    }


    /*
        receiveOne和receiveTwo都是监听一个消息队列并且默认情况下如果不配置prefetch
        参数消息会灌满第一个消费者后才会灌满第二个消费者,默认第一个消费者会灌满
        Integer.MAX条数据
     */
    @RabbitListener(queues = "myqueue")
    public void receiveOne(Message message,Channel channel){
        try {
            TimeUnit.SECONDS.sleep(5);
            String s = new String(message.getBody());
            log.info("receiveOne收到消息:" + s);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            /*
               如果ack方式为manual的话就必须手工应答,
               true表示回退到queue中,false表示如果没有
               绑定死信队列,消息丢失
             */
            /*channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);*/
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    /*
       receiveOne和receiveTwo都是监听一个消息队列并且默认情况下如果不配置prefetch
       参数消息会灌满第一个消费者后才会灌满第二个消费者,默认第一个消费者会灌满
       Integer.MAX条数据
    */
    @RabbitListener(queues = "myqueue")
    public void receiveTwo(Message message,Channel channel){
        try {
            TimeUnit.SECONDS.sleep(5);
            String s = new String(message.getBody());
            log.info("receiveTwo收到消息:" + s);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            /*
               如果ack方式为manual的话就必须手工应答,
               true表示回退到queue中,false表示如果没有
               绑定死信队列,消息丢失
            */
            /*channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);*/
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    /*
       从消息中解析出entity
     */
    @RabbitListener(queues = "myqueue")
    public void readMsg(Message message, Channel channel) throws IOException {
        try {
            log.info("readMsg");
            Map map = new ObjectMapper().readValue(message.getBody(), Map.class);
            String content = (String) map.get("content");
            log.info("收到消息:{}",content);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            /* 拒绝消息并回退queue */
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @RabbitListener(queues = "my.direct.queue")
    public void test(Message message,Channel channel) throws IOException{
        /* 用Person存的不能以String进行反序列化,必须强转为Person */
        //System.out.println("a");
        //Person person = (Person) jackson2JsonMessageConverter.fromMessage(message);
        //log.info(person.toString());
        //throw new MessageConversionException("a");
        channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
        //int j = 5 / 0;
    }

    @RabbitListener(queues = "my.direct.queue")
    public void test2(Message message,Channel channel) throws IOException{
        /* 用Person存的不能以String进行反序列化 */
        //System.out.println("b");
        //Person person = (Person) jackson2JsonMessageConverter.fromMessage(message);
        //log.info(person.toString());
        //throw new MessageConversionException("a");
        //channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        //int j = 5 / 0;
    }

    /**
     * 橙联使用的消费mq的方式
     * 如果createBusinessQueue为null,不会报错但消息同时也不会消费
     * @return
     */
    //@Bean
    public SimpleMessageListenerContainer acceptAutoGenerateEventTriggerConfigListenerContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(createDeadQueue);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setPrefetchCount(10);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            try {
                Person person = (Person) jackson2JsonMessageConverter.fromMessage(message);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                log.info("正常消息，手动ACK");
            } catch (Exception e) {
                log.error("Receive Message Error", e);
                //拒收消息，mq将消息写入死信队列
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
                log.error("消息消费异常，手动ACK");
            }
        });
        return container;
    }




    @Override
    public void afterPropertiesSet() throws Exception {
        this.jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
    }





}

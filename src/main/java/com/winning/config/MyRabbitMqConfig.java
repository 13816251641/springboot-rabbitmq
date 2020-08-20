package com.winning.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyRabbitMqConfig {
    private static final String BUSINESS_EXCHANGE_NAME="my.direct.exchange";

    private static final String BUSINESS_QUEUE_NAME="my.direct.queue";

    private static final String BUSINESS_ROUTING_KEY="hello";

    private static final String DEAD_EXCHANGE_NAME="my.direct.dead.exchange";

    private static final String DEAD_ROUTING_KEY="dead";

    private static final String DEAD_QUEUE_NAME="my.direct.dead.queue";

    /**
     * 我发现以下创建元数据(交换机,队列,绑定关系)的代码只有在代码中调用RabbitTemplate后
     * 才会去RabbitMq中创建元数据,项目启动时是不会去创建的!!!
     * 创建业务交换机(Direct)
     */
    @Bean
    public Exchange createBusinessExchange(){
        return new DirectExchange(BUSINESS_EXCHANGE_NAME);
    }

    /**
     * 创建死信交换机(Direct)
     * @return
     */
    @Bean
    public Exchange createDeadLetterExchange() {
        return new DirectExchange(DEAD_EXCHANGE_NAME);
    }

    /**
     * 创建死信队列并设置该队列是持久化的
     * @return
     */
    @Bean
    public Queue createDeadQueue() {
        return new Queue(DEAD_QUEUE_NAME,true);
    }

    /**
     * 将死信队列信息配置在业务队列上
     * @return
     *
     * RabbitMQ默认有一个exchange，叫default exchange，它用一个空字符串表示!!!，它是direct exchange类型，
     * "x-dead-letter-exchange", ""
     * 任何发往这个exchange的消息都会被路由到routing key的名字对应的队列上，如果没有对应的队列，则消息会被丢弃
     *
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.rabbitmq.queueMap.autoGenerateEvent", name = "enabled")
    public Queue createBusinessQueue() {
        Map<String, Object> args = new HashMap<>(2);
        /*声明死信交换机*/
        //args.put("x-dead-letter-exchange",DEAD_EXCHANGE_NAME);
        args.put("x-dead-letter-exchange","");
        /*声明死信路由键*/
        //args.put("x-dead-letter-routing-key", DEAD_ROUTING_KEY);
        args.put("x-dead-letter-routing-key", "my.direct.dead.queue");
        return QueueBuilder.durable(BUSINESS_QUEUE_NAME).withArguments(args).build();
    }


    /**
     * 将业务队列绑定到业务交换机上，并设置消息分发的路由键
     *
     * @return
     */
    @Bean
    public Binding bindBusinessExchangeAndQueue() {
        /*链式写法: 用指定的路由键将队列绑定到交换机 */
        return new Binding(BUSINESS_QUEUE_NAME, Binding.DestinationType.QUEUE, BUSINESS_EXCHANGE_NAME, BUSINESS_ROUTING_KEY, null);
    }

    /**
     * 将死信队列绑定到死信交换机上
     *
     * @return
     */
    @Bean
    public Binding bindDeadExchangeAndDeadnQueue() {
        /*链式写法: 用指定的路由键将队列绑定到交换机*/
        return new Binding(DEAD_QUEUE_NAME, Binding.DestinationType.QUEUE, DEAD_EXCHANGE_NAME, DEAD_ROUTING_KEY, null);
    }



    @Bean
    /* 加了@ConditionalOnSingleCandidate这个注解会导致自己定义的配置类无法注入 no reason */
    /*@ConditionalOnSingleCandidate(ConnectionFactory.class)*/
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, RabbitProperties properties, ObjectProvider<RabbitRetryTemplateCustomizer> retryTemplateCustomizers) {
        PropertyMapper map = PropertyMapper.get();
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        MessageConverter messageConverter = new Jackson2JsonMessageConverter();
        /* 使用json序列化方式替换jdk序列化 */
        rabbitTemplate.setMessageConverter(messageConverter);

        /*
         * 若使用confirm-callback或return-callback，
         * 必须要配置publisherConfirms或publisherReturns为true
         * 每个rabbitTemplate只能有一个confirm-callback和return-callback
         */
        rabbitTemplate.setConfirmCallback(createExchangeCheckCallback());

        /*
         * 使用return-callback时必须设置mandatory为true，
         * 或者在配置中设置mandatory-expression的值为true，
         * 可针对每次请求的消息去确定'mandatory'的boolean值，
         * 只能在提供'return-callback'时使用,和事务互斥
         */
        rabbitTemplate.setReturnCallback(createQueueCheckCallback());
        rabbitTemplate.setMandatory(true);

        /*
          利用lambda表达式从配置文件中取得默认值并赋值到rabbitTemplate中
         */
        RabbitProperties.Template template = properties.getTemplate();
        template.getClass();
        map.from(template::getReceiveTimeout).whenNonNull().as(Duration::toMillis).to(rabbitTemplate::setReceiveTimeout);
        template.getClass();
        map.from(template::getReplyTimeout).whenNonNull().as(Duration::toMillis).to(rabbitTemplate::setReplyTimeout);
        template.getClass();
        map.from(template::getExchange).to(rabbitTemplate::setExchange);
        template.getClass();
        map.from(template::getRoutingKey).to(rabbitTemplate::setRoutingKey);
        template.getClass();
        map.from(template::getQueue).whenNonNull().to(rabbitTemplate::setQueue);
        return rabbitTemplate;
    }

    /**
     * 关于 ExchangeCheckCallback 和 QueueCheckCallback 的回调说明：
     * 1.如果消息没有到exchange,则confirm回调,ack=false
     * 2.如果消息到达exchange,则confirm回调,ack=true
     * 3.exchange到queue成功,则不回调QueueCheckCallback,只会回调ExchangeCheckCallback
     * 4.exchange到queue失败,则回调QueueCheckCallback(需设置mandatory=true,否则不回调,消息就丢了)
     */

    @Bean
    public ExchangeCheckCallback createExchangeCheckCallback(){
        return new ExchangeCheckCallback();
    }

    @Bean
    public QueueCheckCallback createQueueCheckCallback(){
        return new QueueCheckCallback();
    }

}

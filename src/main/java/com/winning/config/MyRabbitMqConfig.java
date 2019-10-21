package com.winning.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class MyRabbitMqConfig {
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

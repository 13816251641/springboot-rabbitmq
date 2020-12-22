package com.winning.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
public class ExchangeCheckCallback implements RabbitTemplate.ConfirmCallback{

    /**
     * rabbitTemplate.convertAndSend("abc", MyRabbitMqConfig.BUSINESS_ROUTING_KEY, message);这样会导致回调
     * ConfirmCallback的时候correlationData为null
     *
     *
     * 当消息发送到交换机（exchange）时，该方法被回调.
     * 1.如果消息没有到exchange(可能exchange的名字写错了),则 ack=false
     * 2.如果消息到达exchange(消息已经写入日志(已落盘)),则 ack=true
     * @param correlationData
     * @param ack
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        log.info("ExchangeCheckCallback,回调id:"+correlationData.getId());
        if(ack){
            /* 当ack为true的时候,代表消息已经写入日志并已落入磁盘 */
             log.info("消息发送到exchange成功");
            /*TODO 修改消息表的消息状态为成功*/
        }else{
            /*
               到exchange失败后,queue肯定就失败,所以不会回调QueueCheckCallback
             */
            log.info("消息发送到exchange失败");
            /*TODO 消息发送到exchange失败,修改消息表的消息状态为失败*/
        }
    }
}

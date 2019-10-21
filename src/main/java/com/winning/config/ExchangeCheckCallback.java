package com.winning.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
public class ExchangeCheckCallback implements RabbitTemplate.ConfirmCallback{

    /**
     * 当消息发送到交换机（exchange）时，该方法被调用.
     * 1.如果消息没有到exchange,则 ack=false
     * 2.如果消息到达exchange,则 ack=true
     * @param correlationData
     * @param ack
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        log.info("ExchangeCheckCallback,回调id:"+correlationData);
        if(ack){
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

package com.winning.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
public class QueueCheckCallback implements RabbitTemplate.ReturnCallback {
    /**
     * 当消息从交换机到队列失败时,该方法被调用。（若成功，则不调用）
     * 需要注意的是：该方法调用后，MsgSendConfirmCallBack中的confirm方法也会被调用,且ack = true
     * @param message
     * @param replyCode
     * @param replyText
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        /*
         * 需要注意的是：该方法被回调代表从交换机到队列失败
         * ExchangeCheckCallback中的confirm方法也会被调用,且ack = true
         * 发message的时候要message中要设置correlationId才可以获得到
         */
        String correlationId = message.getMessageProperties().getCorrelationId();
        log.info("QueueCheckCallback [消息从交换机到队列失败]  message："+correlationId);
        //TODO  消息从交换机到队列失败，修改消息表的消息状态为失败
    }
}

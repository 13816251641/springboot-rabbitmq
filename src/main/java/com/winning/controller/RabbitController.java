package com.winning.controller;

import com.winning.config.MyRabbitMqConfig;
import com.winning.service.ProduceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/rabbit")
@Slf4j
public class RabbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProduceService produceService;


    /**
        将消息发送给my.direct.exchange交换机,
        如果没有创建交换机的话要先创建
     */
    @GetMapping("/send")
    public void send(){
        produceService.sendDirect();
        log.info("success");
    }

    /*
        批量发送到direct类型的交换机,之后因为对应队列
        设置了消息过期时间和死信队列,最后消息发送到死信
        队列中去
     */
    @GetMapping("/multisenddead")
    public void multisendDead(){
        String message = "this is dead test!";
        for(int i=0;i<20;i++){
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rabbitTemplate.convertAndSend("exchange-normal","queue-normal",message);
        }
    }
}

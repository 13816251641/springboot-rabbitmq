package com.winning.controller;

import com.winning.entity.MyDTO;
import com.winning.service.ProduceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rabbit")
@Slf4j
public class RabbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProduceService produceService;

    /**
     * 从前端接收dto
     * @param myDTO
     */
    @PostMapping("/test_mydto")
    public void testMyDTO(@RequestBody MyDTO myDTO){
        log.info(myDTO.toString());
    }

    /**
        将消息发送给my.direct.exchange交换机,
        如果没有创建交换机的话要先创建
     */
    @GetMapping("/send")
    @ResponseBody
    public void send(){
        produceService.sendDirect();
        log.info("success");
    }
}

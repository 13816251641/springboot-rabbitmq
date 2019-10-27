package com.winning.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class EventNotifierInputDTO {
    /*业务Id*/
    @NotNull(message = "actionId为空")
    private Long bsId;
    @NotNull(message = "hospitalSOID为空")
    private Long hospitalSOID;
    /*消息id,关联日志表*/
    @NotNull(message="messageId为空")
    private Long messageId;
    /*事件id*/
    @NotNull(message = "eventId为空")
    private Long eventId;

}

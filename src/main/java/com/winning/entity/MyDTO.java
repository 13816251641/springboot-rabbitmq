package com.winning.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class MyDTO {
    private LocalDate localDate;
    private Date birth;
}

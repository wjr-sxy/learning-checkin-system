package com.example.learningcheckin.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TrendPointDTO {
    private String date;
    private Integer value;
    private BigDecimal rate;
}

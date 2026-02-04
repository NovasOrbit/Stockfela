package com.application.stockfela.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateGroupRequest
{
    private String name;
        private String description;
        private BigDecimal monthlyContribution;
        private Integer cycleMonths;
        private Long createdBy;
}

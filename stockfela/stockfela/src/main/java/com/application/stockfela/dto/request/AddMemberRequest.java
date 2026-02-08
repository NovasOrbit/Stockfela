package com.application.stockfela.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AddMemberRequest {
    private Long userId;
    private Integer payoutOrder;

}

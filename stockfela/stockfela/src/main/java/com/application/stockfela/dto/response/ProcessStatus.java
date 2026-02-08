package com.application.stockfela.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessStatus {
    @NotNull
    private String code;

    @NotNull
    private String message;

}

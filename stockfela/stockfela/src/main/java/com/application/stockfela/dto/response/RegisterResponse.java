package com.application.stockfela.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {

    private ProcessStatus status;
    private Long id;
    private String username;
    private String email;
    private String fullName;

}

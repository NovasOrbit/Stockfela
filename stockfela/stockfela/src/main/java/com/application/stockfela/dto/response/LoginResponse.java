package com.application.stockfela.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String id;
    private String username;
    private String email;
    private String fullName;

}

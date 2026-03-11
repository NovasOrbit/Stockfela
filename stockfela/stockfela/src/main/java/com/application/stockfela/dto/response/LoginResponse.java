package com.application.stockfela.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {


    private String id;
    private String username;
    private String email;
    private String fullName;
    private String jwtToken;
    private List<String> roles;


    public LoginResponse(String jwtToken, String username,List<String> roles) {
        this.jwtToken=jwtToken;
        this.username=username;
        this.roles=roles;

    }
}

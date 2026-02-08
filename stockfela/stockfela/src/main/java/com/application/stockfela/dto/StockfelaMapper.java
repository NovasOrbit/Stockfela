package com.application.stockfela.dto;

import com.application.stockfela.dto.request.LoginRequest;
import com.application.stockfela.dto.request.RegisterRequest;
import com.application.stockfela.dto.response.LoginResponse;
import com.application.stockfela.dto.response.ProcessStatus;
import com.application.stockfela.dto.response.RegisterResponse;
import com.application.stockfela.entity.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StockfelaMapper {

    public static RegisterResponse mapToRegisterResponse(User savedUser){


        var processStatus = ProcessStatus.builder()
                .code(ProcessStatusCodes.SUCCESS.getCode())
                .message(ProcessStatusCodes.SUCCESS.getMessage())
                .build();


        return RegisterResponse.builder()
                .status(processStatus)
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getUsername())
                .build();
    }

//    public static LoginResponse mapToLoginResponse(LoginRequest loginRequest){
//
//    }


}

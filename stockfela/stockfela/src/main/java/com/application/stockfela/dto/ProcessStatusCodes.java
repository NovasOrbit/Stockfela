package com.application.stockfela.dto;

public enum ProcessStatusCodes {
    SUCCESS("200", "Success"),

    REGISTRATION_BADbREQUEST("400", "Registration Bad request"),
    REGISTRATION_ALREADY_EXIST("409", "Already exist"),
    LOGIN_DATA_INPUT_VALIDATION_ERROR("400", "Login input data error");


    private final String code;
    private final String message;

    ProcessStatusCodes(String code, String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

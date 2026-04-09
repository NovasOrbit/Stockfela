package com.application.stockfela.dto;

import com.application.stockfela.dto.response.ProcessStatus;
import com.application.stockfela.dto.response.RegisterResponse;
import com.application.stockfela.entity.User;
import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;

/**
 * Stateless mapping utilities for converting domain entities to response DTOs.
 *
 * <p>{@link UtilityClass} (Lombok) makes the class {@code final}, adds a
 * private constructor that throws {@link UnsupportedOperationException},
 * and removes the need for writing boilerplate static-only utility scaffolding.
 *
 * <p>Add a new {@code mapTo*()} static method here whenever a controller
 * needs a new entity-to-DTO conversion.
 */
@UtilityClass
public class StockfelaMapper {

    /**
     * Map a persisted {@link User} entity to a {@link RegisterResponse} DTO.
     *
     * <p>Only safe, non-sensitive fields are included in the response —
     * the password hash is intentionally excluded.
     *
     * @param savedUser the user entity returned by
     *                  {@link com.application.stockfela.repository.UserRepository#save}
     * @return a {@link RegisterResponse} containing the new user's public profile
     *         and a {@link ProcessStatus#SUCCESS} status object
     */
    public static RegisterResponse mapToRegisterResponse(User savedUser) {

        ProcessStatus processStatus = ProcessStatus.builder()
                .code(ProcessStatusCodes.SUCCESS.getCode())
                .message(ProcessStatusCodes.SUCCESS.getMessage())
                .build();

        return RegisterResponse.builder()
                .status(processStatus)
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .build();
    }
}

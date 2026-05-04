package com.clientservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InitSetupRequest {
    @NotBlank(message = "密码不能为空")
    private String password;
}

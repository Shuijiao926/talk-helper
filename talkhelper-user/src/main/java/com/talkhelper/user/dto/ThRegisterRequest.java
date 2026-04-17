package com.talkhelper.user.dto;

import lombok.Data;

@Data
public class ThRegisterRequest {

    private String username;
    private String password;
    private String email;
}

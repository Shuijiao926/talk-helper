package com.talkhelper.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThLoginResponse {

    private String token;
    private String userId;
    private String username;
    private long expiresIn;
}

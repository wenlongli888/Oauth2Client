package org.lwl.oauth2client.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class Token implements Serializable {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String scope;
    private Integer expiresIn;
}

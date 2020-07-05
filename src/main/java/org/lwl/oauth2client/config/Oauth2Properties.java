package org.lwl.oauth2client.config;

import lombok.Data;

/**
 * Oauth2Client config
 *  @author Lwl
 *  @version 1.0
 */
@Data
public class Oauth2Properties {
    private String serverIp;
    private String redirectUri;
    private String clientId;
    private String clientSecret;
    private String scope;
    private Boolean https = true;
}

package org.lwl.oauth2client.dto;

import lombok.*;
import java.io.Serializable;

/**
 *  http client return message
 *  @author Lwl
 *  @version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Message implements Serializable {
    /**
     * 状态码
     */
    int statusCode;
    /**
     * 消息体
     */
    String content;
}

package com.ladeit.pojo.ao;

import lombok.Data;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname MessageStateAO
 * @Date 2020/3/14 13:12
 */
@Data
public class MessageStateAO {

    private String id;

    private String messageId;

    private String userId;

    private Boolean readFlag;

}

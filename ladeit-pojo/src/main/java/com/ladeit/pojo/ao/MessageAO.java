package com.ladeit.pojo.ao;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname MessageAO
 * @Date 2020/3/14 14:17
 */
@Data
public class MessageAO {
    /**
     * id
     */
    private String id;

    /**
     * title
     */
    private String title;

    /**
     * target_id
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * type
     */
    private String type;

    /**
     * target_id
     */
    private String targetId;

    /**
     * level
     */
    private String level;

    /**name
     * service_id
     */
    private String serviceId;

    private String serviceName;

    private String serviceGroupId;

    private String serviceGroupName;

    /**
     * operuser_id
     */
    private String operuserId;

    private String operuserName;
}

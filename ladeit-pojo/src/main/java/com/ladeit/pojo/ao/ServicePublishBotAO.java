package com.ladeit.pojo.ao;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ServicePublishBot
 * @Date 2020/1/18 10:10
 */
@Data
public class ServicePublishBotAO {
    /**
     * 主键 primary key
     */
    private String id;

    /**
     * service_group_id
     */
    private String serviceGroupId;

    /**
     * service_id
     */
    private String serviceId;

    /**
     * oper_type
     */
    private String operType;

    /**
     * examine_type
     */
    private String examineType;

    /**
     * publish_type
     */
    private String publishType;
}

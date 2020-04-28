package com.ladeit.pojo.doo;

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
@Table(name = "service_publish_bot")
@Entity
public class ServicePublishBot {
    /**
     * 主键 primary key
     */
    @Id
    private String id;

    /**
     * service_group_id
     */
    @Column(name = "service_group_id")
    private String serviceGroupId;

    /**
     * service_id
     */
    @Column(name = "service_id")
    private String serviceId;

    /**
     * oper_type
     */
    @Column(name = "oper_type")
    private String operType;

    /**
     * examine_type
     */
    @Column(name = "examine_type")
    private String examineType;

    /**
     * publish_type
     */
    @Column(name = "publish_type")
    private String publishType;
}

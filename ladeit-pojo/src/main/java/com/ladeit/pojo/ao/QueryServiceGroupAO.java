package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname QueryServiceGroupAO
 * @Date 2019/11/7 8:18
 */
@Data
public class QueryServiceGroupAO {
    /**
     * 主键 primary key
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * env_id
     */
    private String envId;

    /**
     * cluster_id
     */
    private String clusterId;

    /**
     * gateway
     */
    private String gateway;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * modify_at
     */
    private Date modifyAt;

    /**
     * modify_by
     */
    private String modifyBy;

    /**
     * isdel
     */
    private Boolean isdel;

    /**
     * envName
     */
    private String envName;

    /**
     * clusterName
     */
    private String clusterName;

    private String accessLevel;

    private String inviteCode;


    private List<QueryServiceAO> servicelist;
}

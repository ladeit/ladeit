package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ChannelServiceGroup
 * @Date 2019/12/21 14:38
 */
@Data
@Table(name = "channel_service_group")
@Entity
public class ChannelServiceGroup {
    /**
     * id
     */
    @Id
    private String id;

    /**
     * channel_id
     */
    @Column(name = "channel_id")
    private String channelId;

    /**
     * servicegroup_id
     */
    @Column(name = "servicegroup_id")
    private String servicegroupId;

    /**
     * channel_name
     */
    @Column(name = "channel_name")
    private String channelName;

    /**
     * servicegroup_name
     */
    @Column(name = "servicegroup_name")
    private String servicegroupName;

    /**
     * create_at
     */
    @Column(name = "create_at")
    private Date createAt;


}

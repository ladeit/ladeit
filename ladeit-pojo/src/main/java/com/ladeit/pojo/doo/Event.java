package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "events")
public class Event {
    @Id
    private String id;
    @Column(name = "event_uid")
    private String eventUid;
    @Column(name = "resource_uid")
    private String resourceUid;
    @Column(name = "reason")
    private String reason;
    @Column(name = "type")
    private String type;
    @Column(name = "note")
    private String note;
    @Column(name = "kind")
    private String kind;
    @Column(name = "name")
    private String name;
    @Column(name = "namespace")
    private String namespace;
    @Column(name = "time")
    private Date time;
    @Transient
    private Date startTime;
    @Transient
    private Date endTime;
    @Column(name = "cluster_id")
    private String clusterId;
    @Transient
    private int pageSize;
    @Transient
    private int pageNum;

}

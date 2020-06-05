package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class EventAO {
    private String id;
    private String uid;
    private String reason;
    private String type;
    private String note;
    private String kind;
    private String name;
    private String namespace;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone="GMT+8")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone="GMT+8")
    private Date endTime;
    private Date time;
    private String clusterId;
    private int pageSize;
    private int pageNum;
}

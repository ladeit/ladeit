package com.ladeit.pojo.dto;

import com.ladeit.pojo.ao.TopologyAO;
import lombok.Data;

import java.util.Date;

@Data
public class CandidateDto {

    private String candidateId;
    private String imageId;
    private String releaseId;
    private String serviceId;
    private String uid;
    private String time;
    private String version;
    private TopologyAO topologyAO;
    // 水平扩展的数量
    private Integer [] scaleCount;
    private int type;
    // 是否是自动发版
    private Boolean auto;
}

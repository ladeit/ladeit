package com.ladeit.pojo.ao;

import lombok.Data;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname BotQueryServiceAO
 * @Date 2019/12/25 15:41
 */
@Data
public class BotQueryImageAO {

    private Boolean flag;

    private String message;

    private String serviceName;

    private String serviceId;

    private String serviceGroupName;

    private List<ImageAO> imageAOs;

}

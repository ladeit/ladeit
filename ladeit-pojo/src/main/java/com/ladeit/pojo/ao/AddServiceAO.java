package com.ladeit.pojo.ao;

import lombok.Data;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname AddServiceAO
 * @Date 2019/11/6 16:04
 */
@Data
public class AddServiceAO {

    private String token;

    private String serviceName;

    private String image;

    private String version;

    private String refs;

    private String commitHash;

    private String comments;

}

package com.ladeit.pojo.ao;

import lombok.Data;

import java.util.Map;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ResultAO
 * @Date 2019/12/26 13:42
 */
@Data
public class ResultAO {

    private Boolean flag;

    private String message;

    private Map<String,Object> info;

}

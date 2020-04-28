package com.ladeit.pojo.ao;

import lombok.Data;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname QueryResourceAO
 * @Date 2020/3/20 9:52
 */
@Data
public class QueryResourceAO {

    private String name;

    private List<QueryResourceAO> children;

}

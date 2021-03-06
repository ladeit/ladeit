package com.ladeit.pojo.ao;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname SeriveGroupUserAO
 * @Date 2019/11/25 13:17
 */
@Data
public class ServiceGroupUserAO {
    /**
     * id
     */
    private String id;

    /**
     * user_id
     */
    private String userId;

    /**
     * admin/regular
     */
    private String accessLevel;

    private String username;

    private Date createAt;

    private List<ServiceUserAO> serviceUsers;

}

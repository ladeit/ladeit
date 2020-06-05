package com.ladeit.pojo.ao;

import com.ladeit.pojo.ao.topology.StringMatch;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @program: ladeit
 * @description: Service
 * @author: liuzp
 * @create: 2019/11/11
 * @version: 1.0.0
 */
@Data
public class 	QueryServiceAO {
	/**
	 * 主键 primary key
	 */
	private String id;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 冗余
	 */
	private String serviceGroupId;

	private String clusterId;

	/**
	 * env_id
	 */
	private String envId;

	/**
	 * match json
	 */
	private List<List<List<StringMatch>>> match;

	/**
	 * gateway
	 */
	private List<String> gateway;
	/**
	 * 匹配ip或者dns
	 */
	private String mapping;

	/**
	 * 状态 0 正常运行 1 金丝雀发布中 2 蓝绿发布中 3 abtest发布中 4 滚动发布中
	 */
	private String status;

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
	 * image_version
	 */
	private String imageVersion;

	private String imageId;

	private String roleNum;

	private ReleaseAO release;

	private String token;

	private String clustername;

	private String envname;

	private Integer imagenum;

	private Date releaseAt;

	private String serviceType;

	private ServicePublishBotAO servicePublishBot;

	private List<ImageAO> imageAOS;

	private List<MessageAO> messageAOS;

	private Map<String,Long> podStatus;

}

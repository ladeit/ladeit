package com.ladeit.pojo.ao;

import lombok.Data;

import java.util.Date;

/**
 * @program: ladeit
 * @description: Service
 * @author: MddandPyy
 * @create: 2019/12/10
 * @version: 1.0.0
 */
@Data
public class ServiceDeployAO {

	//运行状态
	private String runStatus;

	//服务id
	private String serviceId;

	//运行时长（当前服役的release）
	private Date duration;

	//当前运行的镜像
	private String imageVersion;

	//服务的状态
	private String status;

	//服务首次发布时间，用于前端计算服务的总运行时长
	private Date releaseAt;

	//发布次数
	private int releaseNum;

	//镜像数量
	private int imageNum;

	private String imageId;


}

package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.ImageAO;
import com.ladeit.pojo.ao.QueryImageAO;
import com.ladeit.pojo.doo.Image;

import java.util.List;

/**
 * @program: ladeit
 * @description: ImageService
 * @author: falcomlife
 * @create: 2019/11/07
 * @version: 1.0.0
 */
public interface ImageService {
	/**
	 * 通过id得到image
	 *
	 * @param id
	 * @return com.ladeit.pojo.doo.Image
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	Image getImageById(String id);


	/**
	 * 查询镜像详情（有权限校验）
	 * @param serviceId, imageId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.QueryServiceAO>>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	ExecuteResult<QueryImageAO> getImageById(String serviceId, String imageId);

	/**
	 * 根据releaseId得到使用中的image
	 *
	 * @param releaseId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Image>
	 * @author falcomlife
	 * @date 19-11-11
	 * @version 1.0.0
	 */
	ExecuteResult<Image> getImageByReleaseId(String releaseId);

	/**
	* 根据service获取所有的image
	* @author falcomlife
	* @date 20-4-29
	* @version 1.0.0
	* @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.doo.Image>>
	* @param id
	*/
	ExecuteResult<List<Image>> getImageByServiceId(String id);
}

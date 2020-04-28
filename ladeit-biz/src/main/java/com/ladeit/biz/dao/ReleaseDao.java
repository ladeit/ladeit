package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Release;

import java.util.Date;
import java.util.List;

/**
 * @program: ladeit
 * @description: ReleaseDao
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
public interface ReleaseDao {

	/**
	 * 新建release
	 *
	 * @param release
	 * @return void
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	void insert(Release release);

	/**
	* 根据serviceid得到服役中的release
	* @author falcomlife
	* @date 19-11-11
	* @version 1.0.0
	* @return com.ladeit.pojo.doo.Release
	* @param serviceId
	*/
	Release getInUseReleaseByServiceId(String serviceId);

	/**
	 * 根据releaseID查询
	 * @param releaseId
	 * @return com.ladeit.pojo.doo.Release
	 * @date 2019/12/7
	 * @ahthor MddandPyy
	 */
	Release getInUseReleaseByReleaseId(String releaseId);

	/**
	 * 查询某一服务的发布信息
	 * @param serviceId
	 * @return java.util.List<com.ladeit.pojo.doo.Release>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	List<Release> getReleaseList(String serviceId);

	/**
	 * 查询某一服务的发布信息
	 * @param serviceId
	 * @return java.util.List<com.ladeit.pojo.doo.Release>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	List<Release> getReleaseListPager(String serviceId,int currentPage,int pageSize);

	int getReleaseListCount(String serviceId);

	/**
	 * 根据release的ID和名字查询
	 * @param releaseId, releaseName
	 * @return java.util.List<com.ladeit.pojo.doo.Release>
	 * @date 2019/11/12
	 * @ahthor MddandPyy
	 */


	List<Release> getReleaseListByIdAndName(String releaseId,String releaseName);

	List<Release> getReleasesByServiceIdAndImageId(String serviceId,String imageId);

	/**
	* 修改release状态
	* @author falcomlife
	* @date 19-12-13
	* @version 1.0.0
	* @return void
	* @param releaseInUse
	*/
	void updateReleaseStatusToRetire(Release releaseInUse);

	/**
	 * 发布完成，修改状态，同时修改时间
	 * @param r1
	 */
    void updateStatus(Release r1);

	/**
	 * 查询发布中的release
	 * @param serviceId
	 * @return
	 */
	Release getInUpdateRelease(String serviceId);

	Boolean isReleaseNew(String serviceId, Date imageDate);
}

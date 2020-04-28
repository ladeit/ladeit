package com.ladeit.biz.services.impl;

import com.ladeit.biz.annotation.Authority;
import com.ladeit.biz.dao.ImageDao;
import com.ladeit.biz.dao.ReleaseDao;
import com.ladeit.biz.services.CandidateService;
import com.ladeit.biz.services.ImageService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.ImageAO;
import com.ladeit.pojo.ao.QueryImageAO;
import com.ladeit.pojo.ao.ReleaseAO;
import com.ladeit.pojo.doo.Candidate;
import com.ladeit.pojo.doo.Image;
import com.ladeit.pojo.doo.Release;
import com.ladeit.util.ExecuteResultUtil;
import com.ladeit.util.ListUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: ladeit
 * @description: ImageServiceImpl
 * @author: falcomlife
 * @create: 2019/11/07
 * @version: 1.0.0
 */
@Service
public class ImageServiceImpl implements ImageService {

	@Autowired
	private ImageDao imageDao;
	@Autowired
	private CandidateService candidateService;
	@Autowired
	private ReleaseDao releaseDao;

	/**
	 * 通过id得到image
	 *
	 * @param id
	 * @return com.ladeit.pojo.doo.Image
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public Image getImageById(String id) {
		return this.imageDao.getImageById(id);
	}

	/**
	 * 查询镜像详情（有权限校验）
	 * @param serviceId, imageId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.QueryServiceAO>>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "service",level = "R")
	public ExecuteResult<QueryImageAO> getImageById(String serviceId, String imageId) {
		ExecuteResult<QueryImageAO> result = new ExecuteResult<>();
		Image image = imageDao.getImageById(imageId);
		if(image!=null){
			List<Release> releases = releaseDao.getReleasesByServiceIdAndImageId(serviceId, image.getId());
			List<ReleaseAO> releaseAOS = new ListUtil<Release, ReleaseAO>().copyList(releases,
					ReleaseAO.class);
			QueryImageAO imageAO = new QueryImageAO();
			BeanUtils.copyProperties(image,imageAO);
			imageAO.setReleaseAO(releaseAOS);
			result.setResult(imageAO);
		}
		return result;
	}

	/**
	 * 根据releaseId得到使用中的image
	 *
	 * @param releaseId
	 * @return com.ladeit.pojo.doo.Image
	 * @author falcomlife
	 * @date 19-11-11
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<Image> getImageByReleaseId(String releaseId) {
		ExecuteResult<Image> result = new ExecuteResult<>();
		ExecuteResult<Candidate> candidate = this.candidateService.getInUseCandidateByReleaseId(releaseId);
		if (candidate.getCode() != Code.SUCCESS) {
			result = new ExecuteResultUtil<Image>().copyWarnError(candidate);
		} else {
			Image image = this.imageDao.getImageById(candidate.getResult().getImageId());
 			result.setResult(image);
		}
		return result;
	}
}

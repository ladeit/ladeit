package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Image;
import io.ebean.SqlRow;

import java.text.ParseException;
import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ImageDao
 * @Date 2019/11/6 16:46
 */
public interface ImageDao {

	/**
	 * 插入数据
	 *
	 * @param image
	 * @return void
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	void insert(Image image);


    List<Image> queryImages(String serviceId);

	List<SqlRow> queryImagesOrderByCreateAt(String serviceId);

	List<Image> queryPageImages(int currentPage,int pageSize,String serviceId, String startDate, String endDate) throws ParseException;

	int queryPageImagesCount(String serviceId, String startDate, String endDate) throws ParseException;

	/**
	 * 根据id查询
	 *
	 * @param id
	 * @return com.ladeit.pojo.doo.Image
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	Image getImageById(String id);

	void update(Image image);

	Image getImageByServiceAndName(String serviceId,String imageVersion);

}

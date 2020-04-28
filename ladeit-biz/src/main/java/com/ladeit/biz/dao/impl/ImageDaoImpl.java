package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.ImageDao;
import com.ladeit.pojo.doo.Image;
import io.ebean.EbeanServer;
import io.ebean.ExpressionList;
import io.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ImageDaoImpl
 * @Date 2019/11/6 16:47
 */
@Repository
public class ImageDaoImpl implements ImageDao {

	@Autowired
	private EbeanServer server;

	/**
	 * 插入数据
	 *
	 * @param image
	 * @return void
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public void insert(Image image) {
		this.server.insert(image);
	}

	/**
	 * 根据Id查询
	 *
	 * @param id
	 * @return com.ladeit.pojo.doo.Image
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public Image getImageById(String id) {
		return this.server.createQuery(Image.class).where().idEq(id).findOne();
	}

	@Override
	public void update(Image image) {
		this.server.update(image);
	}

	@Override
	public Image getImageByServiceAndName(String serviceId,String imageVersion) {
		return this.server.createQuery(Image.class).where().eq("serviceId",serviceId).eq("version",imageVersion).findOne();
	}

	@Override
    public List<Image> queryImages(String serviceId) {
        return this.server.createQuery(Image.class).where().eq("serviceId",serviceId).orderBy("create_at desc").findList();
    }

    @Override
    public List<SqlRow> queryImagesOrderByCreateAt(String serviceId) {
		return this.server.createSqlQuery("select * from image where service_id =:serviceId  order by create_at desc limit 3").setParameter("serviceId",serviceId).findList();
    }

    @Override
    public List<Image> queryPageImages(int currentPage, int pageSize, String serviceId, String startDate, String endDate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		ExpressionList expressionList = this.server.createQuery(Image.class).where().eq("serviceId",serviceId);
		if(!(startDate==null || startDate.trim().length()==0)){
			Date sdate = sdf.parse(startDate);
			expressionList.ge("createAt",sdate);
		}
		if(!(endDate==null || endDate.trim().length()==0)){
			Date edate = sdf.parse(endDate);
			expressionList.le("createAt",edate);
		}
		return expressionList.setFirstRow((currentPage - 1) * pageSize).setMaxRows(pageSize).orderBy("create_at desc").findList();
    }

	@Override
	public int queryPageImagesCount(String serviceId, String startDate, String endDate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		ExpressionList expressionList = this.server.createQuery(Image.class).where().eq("serviceId",serviceId);
		if(!(startDate==null || startDate.trim().length()==0)){
			Date sdate = sdf.parse(startDate);
			expressionList.ge("createAt",sdate);
		}
		if(!(endDate==null || endDate.trim().length()==0)){
			Date edate = sdf.parse(endDate);
			expressionList.le("createAt",edate);
		}
		return expressionList.findCount();
	}
}

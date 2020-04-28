package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Yaml;

import java.util.List;


/**
 * @author MddandPyy
 * @version V1.0
 * @Classname YamlDao
 * @Date 2019/11/25 11:25
 */
public interface YamlDao {

	/**
	 * 插入数据
	 */
	void  insert(Yaml yaml);

	List<Yaml> queryYamls(String serviceGroupId,String serviceId,int currentPage,int pageSize);

	int queryYamlCount(String serviceGroupId,String serviceId);

	Yaml queryYaml(String id);

	List<Yaml> queryYamlsByServiceId(String serviceId);

}

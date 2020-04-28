package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.HeatMapAO;
import com.ladeit.pojo.ao.ServiceDeployAO;
import com.ladeit.pojo.ao.YamlAO;
import com.ladeit.pojo.doo.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname YamlService
 * @Date 2019/12/31
 */
public interface YamlService {

	/**
	 * 新增yaml记录
	 * @param serviceGroupId, serviceId, yamlContent
	 * @return void
	 * @date 2019/12/31
	 * @ahthor MddandPyy
	 */
	void insertYaml(String serviceGroupId,String serviceId,String yamlContent,String yamlName,String type);

	/**
	 * 查询服务组下某服务的yaml
	 * @param serviceGroupId, serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.YamlAO>>
	 * @date 2019/12/31
	 * @ahthor MddandPyy
	 */
	ExecuteResult<Pager<YamlAO>> queryYaml(String serviceGroupId,String serviceId,int currentPage,int pageSize);

	/**
	 * 下载yaml文件
	 * @param yamlId, response
	 * @return void
	 * @date 2019/12/31
	 * @ahthor MddandPyy
	 */
	void downloadYaml(String yamlId, HttpServletResponse response) throws IOException;

	/**
	 * 下载yaml文件
	 * @param serviceId, response
	 * @return void
	 * @date 2019/12/31
	 * @ahthor MddandPyy
	 */
	void downloadAllYaml(String serviceId, HttpServletResponse response) throws IOException;
}

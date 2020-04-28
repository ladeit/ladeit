package com.ladeit.biz.controller;

import com.ladeit.biz.services.YamlService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.YamlAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @program: ladeit
 * @description: AuthController
 * @author: falcomlife
 * @create: 2019/10/29
 * @version: 1.0.0
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/yaml")
public class YamlController {


	@Autowired
	private YamlService yamlService;

	/**
	 * 查询服务组下某服务的yaml
	 * @param serviceGroupId, serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.YamlAO>>
	 * @date 2019/12/31
	 * @ahthor MddandPyy
	 */
	@GetMapping("/get")
	public ExecuteResult<Pager<YamlAO>> queryYaml(@RequestParam("ServiceGroupId") String serviceGroupId, @RequestParam("ServiceId") String serviceId,@RequestParam("currentPage") int currentPage,
	@RequestParam("pageSize") int pageSize){
		return yamlService.queryYaml(serviceGroupId,serviceId,currentPage,pageSize);
	}

	/**
	 * 下载yaml文件
	 * @param yamlId, response
	 * @return void
	 * @date 2019/12/31
	 * @ahthor MddandPyy
	 */
	@GetMapping("/download")
	public void downloadYaml(@RequestParam("YamlId") String yamlId,HttpServletResponse response) throws IOException {
		yamlService.downloadYaml(yamlId,response);
	}

	/**
	 * 下载yaml文件
	 * @param serviceId, response
	 * @return void
	 * @date 2019/12/31
	 * @ahthor MddandPyy
	 */
	@GetMapping("/downloadAll")
	public void downloadAllYaml(@RequestParam("ServiceId") String serviceId,HttpServletResponse response) throws IOException {
		yamlService.downloadAllYaml(serviceId,response);
	}
}

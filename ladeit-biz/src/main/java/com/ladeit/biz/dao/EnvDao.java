package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Env;

import java.util.List;

/**
 * @program: ladeit
 * @description: EnvDao
 * @author: falcomlife
 * @create: 2019/11/07
 * @version: 1.0.0
 */
public interface EnvDao {

	/**
	 * 创建env
	 *
	 * @param bzEnv
	 * @return void
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	void createEnv(Env bzEnv);

	/**
	 * 更新env
	 * @param env
	 * @return void
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	void updateEnv(Env env);

	/**
	 * 得到env
	 *
	 * @param pageNum
	 * @param pageSize
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	List<Env> getEnv(int pageNum, int pageSize);

	/**
	 * 得到env count
	 *
	 * @param pageNum
	 * @param pageSize
	 * @return int
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	int getEnvCount(int pageNum, int pageSize);

	/**
	 * 查询envlist
	 * @author falcomlife
	 * @date 19-9-27
	 * @version 1.0.0
	 * @param bzEnv
	 */
	List<Env> getEnvList(Env bzEnv);

	/**
	 * 根据envids查询env
	 * @author falcomlife
	 * @date 19-9-27
	 * @version 1.0.0
	 * @param envIds
	 */
	List<Env> getEvnByIds(List<String> envIds);

	/**
	 * 查询单条env
	 * @author falcomlife
	 * @date 19-10-9
	 * @version 1.0.0
	 * @param k8sEnvId
	 */
	Env getEnvById(String k8sEnvId);

	/**
	 * 根绝cluster和namespace查询
	 * @author falcomlife
	 * @date 19-10-10
	 * @version 1.0.0
	 * @param k8sClusterId
	 * @param namespace
	 */
	Env getEnvByClusterAndNamespace(String k8sClusterId, String namespace);

    List<Env> getAllEnv();

	List<Env> getEnvListByClusterId(String clusterId);

	Env getEnvByClusterAndEnvId(String clusterId, String envId);
}

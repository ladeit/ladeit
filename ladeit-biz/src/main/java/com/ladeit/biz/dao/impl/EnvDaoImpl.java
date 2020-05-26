package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.EnvDao;
import com.ladeit.pojo.doo.Env;
import io.ebean.EbeanServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: ladeit
 * @description: EnvDaoImpl
 * @author: falcomlife
 * @create: 2019/11/07
 * @version: 1.0.0
 */
@Repository
public class EnvDaoImpl implements EnvDao {

	@Autowired
	private EbeanServer server;

	/**
	 * 创建env
	 *
	 * @param bzEnv
	 * @return void
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	@Override
	public void createEnv(Env bzEnv) {
		this.server.insert(bzEnv);
	}

	@Override
	public void updateEnv(Env env) {
		this.server.update(env);
	}

	/**
	 * 得到env
	 *
	 * @param pageNum
	 * @param pageSize
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	@Override
	public List<Env> getEnv(int pageNum, int pageSize) {
		return this.server.createQuery(Env.class).where().eq("isdel", false).setFirstRow((pageNum - 1) * pageSize).setMaxRows(pageSize).findList();
	}

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
	@Override
	public int getEnvCount(int pageNum, int pageSize) {
		return this.server.createQuery(Env.class).where().eq("isdel", false).findCount();
	}

	/**
	 * 查询envlist
	 *
	 * @param bzEnv
	 * @author falcomlife
	 * @date 19-9-27
	 * @version 1.0.0
	 */
	@Override
	public List<Env> getEnvList(Env bzEnv) {
		return this.server.createQuery(Env.class).where().eq("cluster_id", bzEnv.getClusterId()).eq("isdel", false).findList();
	}

	/**
	 * 根据envids查询env
	 *
	 * @param envIds
	 * @author falcomlife
	 * @date 19-9-27
	 * @version 1.0.0
	 */
	@Override
	public List<Env> getEvnByIds(List<String> envIds) {
		return this.server.createQuery(Env.class).where().eq("cluster_id", false).eq("isdel", false).findList();
	}

	/**
	 * 查询单条env
	 *
	 * @param k8sEnvId
	 * @author falcomlife
	 * @date 19-10-9
	 * @version 1.0.0
	 */
	@Override
	public Env getEnvById(String k8sEnvId) {
		return this.server.createQuery(Env.class).where().idEq(k8sEnvId).eq("isdel", false).findOne();
	}

	/**
	 * 根据cluster和namespace查询
	 *
	 * @param clusterId
	 * @param namespace
	 * @author falcomlife
	 * @date 19-10-10
	 * @version 1.0.0
	 */
	@Override
	public Env getEnvByClusterAndNamespace(String clusterId, String namespace) {
		return this.server.createQuery(Env.class).where().eq("clusterId", clusterId).eq("namespace", namespace).eq(
		        "isdel", false).findOne();
	}

	@Override
	public List<Env> getAllEnv() {
		return this.server.createQuery(Env.class).where().eq("isdel", false).findList();
	}

	@Override
	public List<Env> getEnvListByClusterId(String clusterId) {
		return this.server.createQuery(Env.class).where().eq("cluster_id", clusterId).eq("isdel", false).findList();
	}

	@Override
	public Env getEnvByClusterAndEnvId(String clusterId, String envId) {
		return this.server.createQuery(Env.class).where().eq("clusterId", clusterId).eq("id", envId).eq("isdel",
                false).findOne();
	}

	/**
	 * 更新env，同步刷新的时候调用，主要更新配额
	 *
	 * @param env
	 * @return void
	 * @author falcomlife
	 * @date 20-5-25
	 * @version 1.0.0
	 */
	@Override
	public void updateEnvQuota(Env env) {
		this.server.update(Env.class).set("cpu_limit",env.getCpuLimit()).set("cpu_limit_unit",env.getCpuLimitUnit()).set("mem_limit",env.getMemLimit()).set("mem_limit_unit",env.getMemLimitUnit()).set("cpu_request",env.getCpuRequest()).set("cpu_request_unit",env.getCpuRequestUnit()).set("mem_request",env.getMemRequest()).set("mem_request_unit",env.getMemRequestUnit()).set("resource_quota",env.getResourceQuota()).where().idEq(env.getId()).update();
	}
}

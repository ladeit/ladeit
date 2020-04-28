package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.ClusterDao;
import com.ladeit.pojo.doo.Cluster;
import io.ebean.EbeanServer;
import io.ebean.ExpressionList;
import io.ebean.SqlRow;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: ladeit
 * @description: ClusterDaoImpl
 * @author: falcomlife
 * @create: 2019/11/07
 * @version: 1.0.0
 */
@Repository
public class ClusterDaoImpl implements ClusterDao {

	@Autowired
	private EbeanServer server;

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return com.ladeit.pojo.doo.Cluster
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public Cluster getClusterById(String id) {
		return this.server.createQuery(Cluster.class).where().idEq(id).eq("isdel",false).findOne();
	}

	/**
	 * 创建cluster
	 *
	 * @param cluster
	 * @return void
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	@Override
	public void createCluster(Cluster cluster) {
		this.server.insert(cluster);
	}

	@Override
	public void update(Cluster cluster) {
		this.server.update(cluster);
	}

	/**
	 * 查询cluster列表
	 *
	 * @param bzK8sClusterDO
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	@Override
	public List<Cluster> getCluster(Cluster bzK8sClusterDO) {
		return this.server.createQuery(Cluster.class).where().eq("isdel", false).findList();
	}

	/**
	 * 查询单条记录
	 *
	 * @param bzK8sClusterDO
	 * @author falcomlife
	 * @date 19-9-20
	 * @version 1.0.0
	 */
	@Override
	public Cluster getOneCluster(Cluster bzK8sClusterDO) {
		ExpressionList expressionList = this.server.createQuery(Cluster.class).where();
		if (StringUtils.isNotBlank(bzK8sClusterDO.getId())) {
			expressionList.eq("id", bzK8sClusterDO.getId());
		}
		if (StringUtils.isNotBlank(bzK8sClusterDO.getK8sName())) {
			expressionList.eq("k8s_name", bzK8sClusterDO.getK8sName());
		}
		Cluster resultdo = (Cluster) expressionList.findOne();
		return resultdo;
	}

    @Override
    public List<Cluster> getClusterPager(int currentPage, int pageSize) {
		return this.server.createQuery(Cluster.class).where().eq("isdel", false).setFirstRow((currentPage - 1) * pageSize).setMaxRows(pageSize).orderBy("create_at desc").findList();
    }

	@Override
	public int getclusterCount() {
		return this.server.createQuery(Cluster.class).where().eq("isdel", false).findCount();
	}

    @Override
    public List<SqlRow> getClusterPagerSqlrow(int currentPage, int pageSize,String orderparam) {
		StringBuffer sbf = new StringBuffer();
		int start = (currentPage-1)*pageSize;
		int end = pageSize;
		sbf.append("select t1.* from (select t1.*,t2.k8s_name clustername,t2.create_at clustercreateat,t2.create_by createby from (select * from env where isdel = 0 ) t1 RIGHT JOIN (select * from cluster where isdel=0) t2 on t1.cluster_id = t2.id where 1=1 ");
		if(orderparam==null || orderparam.trim().length()==0){
			sbf.append("order by t2.k8s_name asc,t1.namespace asc,t1.env_tag asc ,t1.create_by asc,t1.create_at desc) t1 limit :start,:end ");
		}else if("namespaceasc".equals(orderparam)){
			sbf.append(" order by t1.namespace asc) t1 limit :start,:end ");
		}else if("namespacedesc".equals(orderparam)){
			sbf.append(" order by t1.namespace desc) t1 limit :start,:end ");
		}else if("envtagasc".equals(orderparam)){
			sbf.append(" order by t1.env_tag asc) t1 limit :start,:end ");
		}else if("envtagdesc".equals(orderparam)){
			sbf.append(" order by t1.env_tag desc) t1 limit :start,:end ");
		}else if("createatasc".equals(orderparam)){
			sbf.append(" order by t1.create_at asc) t1 limit :start,:end ");
		}else if("createatdesc".equals(orderparam)){
			sbf.append(" order by t1.create_at desc) t1 limit :start,:end ");
		}else if("createbyasc".equals(orderparam)){
			sbf.append(" order by t1.create_by asc) t1 limit :start,:end ");
		}else if("createbydesc".equals(orderparam)){
			sbf.append(" order by t1.create_by desc) t1 limit :start,:end ");
		}
		List<SqlRow> list = server.createSqlQuery(sbf.toString()).setParameter("start", start).setParameter("end",end).findList();
		return list;
    }

	@Override
	public int getclusterCountSqlrow() {
		StringBuffer sbf = new StringBuffer();
		sbf.append("select t1.* from (select t1.*,t2.k8s_name clustername,t2.create_at clustercreateat from (select * from env where isdel = 0 ) t1 RIGHT JOIN (select * from cluster where isdel=0) t2 on t1.cluster_id = t2.id where 1=1 order by t2.k8s_name asc,t1.namespace asc,t2.create_at desc) t1 ");
		List<SqlRow> list = server.createSqlQuery(sbf.toString()).findList();
		return list.size();
	}

	@Override
	public List<Cluster> getClusterByName(String clusterName) {
		return server.createQuery(Cluster.class).where().eq("k8sName",clusterName).eq("isdel",false).findList();
	}

	@Override
	public Cluster getClusterOneByName(String clusterName) {
		return server.createQuery(Cluster.class).where().eq("k8sName",clusterName).eq("isdel",false).findOne();
	}


}

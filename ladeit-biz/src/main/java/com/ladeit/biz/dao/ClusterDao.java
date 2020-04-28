package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Cluster;
import io.ebean.SqlRow;

import java.util.List;

/**
 * @program: ladeit
 * @description: ClusterDao
 * @author: falcomlife
 * @create: 2019/11/07
 * @version: 1.0.0
 */
public interface ClusterDao {
	/**
	* 通过id查询
	* @author falcomlife
	* @date 19-11-7
	* @version 1.0.0
	* @return com.ladeit.pojo.doo.Cluster
	* @param id
	*/
	Cluster getClusterById(String id);

	/**
	 * 创建cluster
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 * @return void
	 * @param bzCluster
	 */
	void createCluster(Cluster bzCluster);

	/**
	 * 更新
	 * @param cluster
	 * @return void
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	void update(Cluster cluster);

	/**
	 * 查询cluster列表
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 * @param bzCluster
	 */
	List<Cluster> getCluster(Cluster bzCluster);

	/**
	 * 查询单条记录
	 * @author falcomlife
	 * @date 19-9-20
	 * @version 1.0.0
	 * @param bzCluster
	 */
	Cluster getOneCluster(Cluster bzCluster);

	List<Cluster> getClusterPager(int currentPage,int pageSize);

	int getclusterCount();

	List<SqlRow> getClusterPagerSqlrow(int currentPage, int pageSize,String orderparam);
	int getclusterCountSqlrow();

	List<Cluster> getClusterByName(String clusterName);

	Cluster getClusterOneByName(String clusterName);
}

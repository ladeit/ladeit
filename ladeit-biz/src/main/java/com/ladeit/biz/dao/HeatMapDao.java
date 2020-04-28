package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.HeatMap;
import com.ladeit.pojo.doo.User;

import java.util.Date;
import java.util.List;

/**
 * @description: HeatMapDao
 * @author: MddandPyy
 * @create: 2019/12/09
 * @version: 1.0.0
 */
public interface HeatMapDao {

	void insert(HeatMap heatMap);

	void update(HeatMap heatMap);

	HeatMap queryHeatMapByTargetIdAndDate(String targetId, String date);

	List<HeatMap> queryHeatMapsByTargetIdAndDate(String targetId, String startDate,String endDate);


}

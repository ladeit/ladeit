package com.ladeit.pojo.ao;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @description: HeatMap
 * @author: MddandPyy
 * @create: 2019/12/09
 * @version: 1.0.0
 */
@Data
public class HeatMapAO {
	/**
	 * id
	 */
	private String id;

	/**
	 * target_id
	 */
	private String targetId;

	/**
	 * num
	 */
	private int num;

	/**
	 * date
	 */
	private Date date;

}

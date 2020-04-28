package com.ladeit.pojo.doo;

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
@Table(name = "heatmap")
@Entity
public class HeatMap {
	/**
	 * id
	 */
	@Id
	private String id;

	/**
	 * target_id
	 */
	@Column(name = "target_id")
	private String targetId;

	/**
	 * num
	 */
	@Column(name = "num")
	private int num;

	/**
	 * date
	 */
	@Column(name = "date")
	private Date date;

}

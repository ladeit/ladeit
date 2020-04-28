package com.ladeit.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 分页组件
 * 
 */
public class Pager<T> implements Serializable {

	/**
	 * UUID
	 */
	private static final long serialVersionUID = 7405623359546298020L;
	public static final int DEFAULT_PAGE_SIZE = 20;
	/**
	 * 结果集
	 */
	private List<T> records = new ArrayList<T>();
	/**
	 * 记录总数
	 */
	private int totalRecord = 0;
	/**
	 * 当前页
	 */
	private int pageNum = 1;
	/**
	 * 每页记录数,默认20条
	 */
	private int pageSize = DEFAULT_PAGE_SIZE;
	/**
	 * 排序字段名称,多个字段中间使用,分隔
	 */
	private String orderProperty = "";
	/**
	 * 排序方式asc或desc,多个字段中间使用,分隔
	 */
	private String order = "";
	/**
	 * 是否计算总数
	 */
	private boolean countTotal = true;
	/**
	 * 筛选使用集合
	 */
	private Set<String> set;

	public Set<String> getSet() {
		return set;
	}

	public void setSet(Set<String> set) {
		this.set = set;
	}

	public List<T> getRecords() {
		return records;
	}

	public void setRecords(List<T> records) {
		this.records = records;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		if (pageNum <= 0) {
			this.pageNum = 1;
		} else {
			this.pageNum = pageNum;
		}
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		if (pageSize <= 0) {
			this.pageSize = 1;
		} else {
			this.pageSize = pageSize;
		}
	}

	public boolean isCountTotal() {
		return countTotal;
	}

	public void setCountTotal(boolean countTotal) {
		this.countTotal = countTotal;
	}

	/**
	 * 计算记录的总页数
	 */
	public int getTotalPage() {
		if (getTotalRecord() == 0) {
			return 1;
		}
		int div = getTotalRecord() / getPageSize();
		int sub = getTotalRecord() % getPageSize();
		if (sub == 0) {
			return div;
		} else {
			return div + 1;
		}
	}

	/**
	 * 是否设置了排序属性
	 * 
	 * @return
	 */
	public boolean isOrderBySetted() {
		return StringUtils.isNotBlank(this.order) && StringUtils.isNotBlank(this.orderProperty);
	}

	/**
	 * 根据当前页获取记录开始号
	 * 
	 * @return
	 */
	public int getFirstResult() {
		return (getPageNum() - 1) * getPageSize();
	}

	/**
	 * 根据当前页获取记录开始号(for mysql)
	 * 
	 * @return
	 */
	public int getFirstResultExt() {
		int firstPage = getFirstResult();
		return firstPage <= 0 ? 0 : (firstPage - 1);
	}

	public String getOrderProperty() {
		return orderProperty;
	}

	public void setOrderProperty(String orderProperty) {
		this.orderProperty = orderProperty;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		String lowcaseOrderDir = StringUtils.lowerCase(order);
		// 检查order字符串的合法值
		String[] orderDirs = StringUtils.split(lowcaseOrderDir, ',');
		for (String orderDirStr : orderDirs) {
			if (!StringUtils.equals(Sort.DESC, orderDirStr) && !StringUtils.equals(Sort.ASC, orderDirStr)) {
				throw new IllegalArgumentException("排序方向" + orderDirStr + "不是合法值");
			}
		}
		this.order = lowcaseOrderDir;
	}

	/**
	 * 获得排序参数.
	 * 
	 * @return
	 */
	public List<Sort> getSort() {
		String[] orderBys = StringUtils.split(this.orderProperty, ',');
		String[] orderDirs = StringUtils.split(this.order, ',');
		Validate.isTrue(orderBys.length == orderDirs.length, "分页多重排序参数中,排序字段与排序方向的个数不相等");

		List<Sort> orders = new ArrayList<Sort>();
		for (int i = 0; i < orderBys.length; i++) {
			orders.add(new Sort(orderBys[i], orderDirs[i]));
		}
		return orders;
	}

	/**
	 * 复制pager的基本信息，totalRecords,currentPage,pageSize,orderProperty,order,
	 * countTotal
	 * 
	 * @param pager
	 * @return
	 */
	public static <X, M> Pager<M> cloneFromPager(Pager<X> pager) {
		Pager<M> result = new Pager<M>();
		result.setCountTotal(pager.isCountTotal());
		result.setPageNum(pager.getPageNum());
		result.setOrder(pager.getOrder());
		result.setOrderProperty(pager.getOrderProperty());
		result.setPageSize(pager.getPageSize());
		result.setTotalRecord(pager.getTotalRecord());
		return result;
	}

	/**
	 * 复制pager的基本信息，totalRecords,currentPage,pageSize,orderProperty,order,
	 * countTotal, 重新设置records，totalRecords属性
	 * 
	 * @param pager
	 * @return
	 */
	public static <X> Pager<X> cloneFromPager(Pager<X> pager, int totalRecords, List<X> records) {
		Pager<X> result = cloneFromPager(pager);
		result.setTotalRecord(totalRecords);
		result.setRecords(records);
		return result;
	}

	public static class Sort {
		public static final String ASC = "asc";
		public static final String DESC = "desc";

		private final String property;
		private final String dir;

		public Sort(String property, String dir) {
			this.property = property;
			this.dir = dir;
		}

		public String getProperty() {
			return property;
		}

		public String getDir() {
			return dir;
		}
	}
}

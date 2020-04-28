package com.ladeit.common.system;

public enum RoleType {
	
	INVALID(-1,""), 
	NONE(0,""),
	GUEST(10,"项目来宾"),
	REPORTER(20,"项目报告人"),
	DEVELOPER(30,"项目开发者"), 
	MAINTAINER(40,"项目维护者"),
	OWNER(50,"项目所有者"),
	ADMIN(60,"平台管理员");

	private final Integer accessLevel;
	
	private String desc;
	
	RoleType(int accessLevel,String desc) {
		this.accessLevel = accessLevel;
		this.desc = desc;
	}

	public Integer toValue() {
		return (accessLevel);
	}

	@Override
	public String toString() {
		return this.name()+","+accessLevel+","+desc;
	}
}

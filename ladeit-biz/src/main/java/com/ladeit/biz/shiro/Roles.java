package com.ladeit.biz.shiro;

public class Roles {
	public static final String ADMIN = "ADMIN";
	public static final String INVALID = "INVALID"; 
	public static final String GUEST = "GUEST";
	public static final String REPORTER = "REPORTER";
	public static final String DEVELOPER = "DEVELOPER";
	public static final String MAINTAINER = "MAINTAINER";
	public static final String OWNER = "OWNER";
	@Override
		protected Object clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			return super.clone();
		}
}

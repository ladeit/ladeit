package com.ladeit.biz.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * @program: ladeit-parent
 * @description: ErrorWebConfig
 * @author: falcomlife
 * @create: 2020/05/11
 * @version: 1.0.0
 */
@Configuration
public class ErrorWebConfig implements ErrorPageRegistrar {
	@Override
	public void registerErrorPages(ErrorPageRegistry registry) {
		/*1、按错误的类型显示错误的网页*/
		/*错误类型为404，找不到网页的，默认显示index.html网页*/
		ErrorPage e404 = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
		registry.addErrorPages(e404);
	}
}

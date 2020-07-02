package com.ladeit.biz.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author falcomlife
 * @date 19-2-25
 */
@Configuration
public class DruidConfiguration {
    private final static Logger logger = LoggerFactory.getLogger(DruidConfiguration.class);
    @Value("${datasource.default.url}")
    private String dbUrl;
    @Value("${datasource.default.username}")
    private String username;
    @Value("${datasource.default.password}")
    private String password;
    @Value("${datasource.default.driver}")
    private String driverClassName;
    @Value("${datasource.default.initialSize}")
    private int initialSize;
    @Value("${datasource.default.minIdle}")
    private int minIdle;
    @Value("${datasource.default.maxActive}")
    private int maxActive;
    @Value("${datasource.default.maxWait}")
    private int maxWait;
    @Value("${datasource.default.timeBetweenEvictionRunsMillis}")
    private int timeBetweenEvictionRunsMillis;
    @Value("${datasource.default.minEvictableIdleTimeMillis}")
    private int minEvictableIdleTimeMillis;
    @Value("${datasource.default.validationQuery}")
    private String validationQuery;
    @Value("${datasource.default.testWhileIdle}")
    private boolean testWhileIdle;
    @Value("${datasource.default.testOnBorrow}")
    private boolean testOnBorrow;
    @Value("${datasource.default.testOnReturn}")
    private boolean testOnReturn;
    @Value("${datasource.default.filters}")
    private String filters;
    @Value("${datasource.default.logSlowSql}")
    private String logSlowSql;

    @Bean(destroyMethod = "close", initMethod = "init", name = "defaultDs")
    @Primary
    public DataSource druidDataSource() {
        DruidDataSource datasource = new DruidDataSource();

        datasource.setUrl(dbUrl);
        datasource.setDbType("sqlite");
        datasource.setUsername(username);
        datasource.setPassword(password);
        datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxActive(maxActive);
        datasource.setMaxWait(maxWait);
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setValidationQuery(validationQuery);
        datasource.setTestWhileIdle(testWhileIdle);
        datasource.setTestOnBorrow(testOnBorrow);
        datasource.setTestOnReturn(testOnReturn);

        try {
            datasource.setFilters(filters);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        logger.info("==============Database:" + dbUrl + "===============");

        return datasource;
    }

    @Bean
    public ServletRegistrationBean druidServlet() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();

        servletRegistrationBean.setServlet(new StatViewServlet());
        servletRegistrationBean.addUrlMappings("/druid/*");
        servletRegistrationBean.addInitParameter("loginUsername", "admin");
        servletRegistrationBean.addInitParameter("loginPassword", "admin");
        servletRegistrationBean.addInitParameter("resetEnable", "false");
        servletRegistrationBean.addInitParameter("allow", "");

        return servletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();

        filterRegistrationBean.setFilter(new WebStatFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");

        return filterRegistrationBean;
    }
}

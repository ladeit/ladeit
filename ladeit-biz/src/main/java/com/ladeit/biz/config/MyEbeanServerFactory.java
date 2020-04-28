package com.ladeit.biz.config;

import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.AutoTuneConfig;
import io.ebean.config.ServerConfig;
import io.ebean.config.UnderscoreNamingConvention;
import io.ebean.spring.txn.SpringJdbcTransactionManager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author falcomlife
 * @date 19-2-25
 */
@Configuration
public class MyEbeanServerFactory implements FactoryBean<EbeanServer> {
    @Value("${ebean.search.packages}")
    private String packages;
    @Autowired
    DruidConfiguration druidConfig;
    @Autowired
    EbeanServer server;

    /**
     * Create a EbeanServer instance.
     *
     * @return
     */
    private EbeanServer createEbeanServer() {
        ServerConfig config = new ServerConfig();

        config.setName("default");
        config.addPackage(packages);

        // load configuration from ebean.properties
        config.setDataSource(druidConfig.druidDataSource());
        config.setAutoTuneConfig(new AutoTuneConfig());
        config.setExternalTransactionManager(new SpringJdbcTransactionManager());
        config.setNamingConvention(new UnderscoreNamingConvention());
        config.setDefaultServer(true);

        // other programmatic configuration
        return EbeanServerFactory.create(config);
    }

    @Override
    public EbeanServer getObject() throws Exception {
        return createEbeanServer();
    }

    @Override
    public Class<?> getObjectType() {
        return EbeanServer.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}


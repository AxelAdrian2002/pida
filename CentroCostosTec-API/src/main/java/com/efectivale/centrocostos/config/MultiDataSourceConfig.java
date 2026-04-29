package com.efectivale.centrocostos.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class MultiDataSourceConfig {

    @Bean(name = "dataSource")
    @Primary
    public DataSource primaryDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driverClassName) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(driverClassName);
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    @Bean(name = "pddespensaDataSource")
    public DataSource pddespensaDataSource(@Qualifier("dataSource") DataSource primaryDs) {
        return primaryDs;
    }

    @Bean(name = "megadbpedidoDataSource")
    public DataSource megadbpedidoDataSource(@Qualifier("dataSource") DataSource primaryDs) {
        return primaryDs;
    }

    @Bean(name = "dbemisDataSource")
    public DataSource dbemisDataSource(@Qualifier("dataSource") DataSource primaryDs) {
        return primaryDs;
    }

    @Bean(name = "pddespensaJdbc")
    public JdbcTemplate pddespensaJdbc(@Qualifier("pddespensaDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "dbdespensaJdbc")
    public JdbcTemplate dbdespensaJdbc(@Qualifier("dataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "megadbpedidoJdbc")
    public JdbcTemplate megadbpedidoJdbc(@Qualifier("megadbpedidoDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean(name = "dbemisJdbc")
    public JdbcTemplate dbemisJdbc(@Qualifier("dbemisDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}

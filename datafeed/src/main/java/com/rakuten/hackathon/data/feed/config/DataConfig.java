package com.rakuten.hackathon.data.feed.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.rakuten.hackathon.data.feed.repository",
                        entityManagerFactoryRef = "feedManagerFactoryManager",
                        transactionManagerRef = "feedTransactionManager")
@EnableAsync(proxyTargetClass = true)
public class DataConfig {

    @Bean
    @Autowired
    public LocalContainerEntityManagerFactoryBean feedManagerFactoryManager(
        EntityManagerFactoryBuilder builder, @Qualifier("dataSource") DataSource dataSource) {

        return builder.dataSource(dataSource)
                .packages("com.rakuten.hackathon.data.feed.dto","com.rakuten.hackathon.data.feed.service")
                .persistenceUnit("feed")
                .build();
    }

    @Bean
    @Autowired
    public JpaTransactionManager dmpTransactionManager(
            @Qualifier("feedManagerFactoryManager") LocalContainerEntityManagerFactoryBean feedManagerFactoryManager) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(feedManagerFactoryManager.getObject());
        return transactionManager;
    }
}

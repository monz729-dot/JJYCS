package com.ycs.lms.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * MyBatis 설정 클래스
 * 
 * 기능:
 * - SQL 세션 팩토리 설정
 * - 매퍼 스캔 설정
 * - 트랜잭션 관리자 설정
 * - XML 매퍼 위치 설정
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.ycs.lms.mapper")
public class MyBatisConfig {

    @Autowired
    private DataSource dataSource;

    /**
     * SqlSessionFactory 빈 설정
     * 
     * @return SqlSessionFactory
     * @throws Exception
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        
        // XML 매퍼 파일 위치 설정
        factoryBean.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath:mapper/**/*.xml")
        );
        
        // Type aliases 패키지 설정
        factoryBean.setTypeAliasesPackage("com.ycs.lms.entity");
        
        // MyBatis 설정
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        
        // 기본 설정
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setDefaultFetchSize(100);
        configuration.setDefaultStatementTimeout(30);
        configuration.setLazyLoadingEnabled(true);
        configuration.setAggressiveLazyLoading(false);
        configuration.setMultipleResultSetsEnabled(true);
        configuration.setUseGeneratedKeys(true);
        configuration.setAutoMappingBehavior(org.apache.ibatis.session.AutoMappingBehavior.PARTIAL);
        configuration.setAutoMappingUnknownColumnBehavior(org.apache.ibatis.session.AutoMappingUnknownColumnBehavior.WARNING);
        configuration.setCacheEnabled(true);
        configuration.setCallSettersOnNulls(true);
        configuration.setReturnInstanceForEmptyRow(false);
        configuration.setShrinkWhitespacesInSql(true);
        
        // 로그 설정
        configuration.setLogImpl(org.apache.ibatis.logging.slf4j.Slf4jImpl.class);
        
        factoryBean.setConfiguration(configuration);
        
        return factoryBean.getObject();
    }

    /**
     * 트랜잭션 매니저 설정
     * 
     * @return PlatformTransactionManager
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }
}
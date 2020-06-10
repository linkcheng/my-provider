package cn.hsb.router.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@ConditionalOnProperty("spring.datasource.bi-router.url")
@MapperScan(
        sqlSessionFactoryRef = "biRouterSqlSessionFactory",
        annotationClass = BiRouterMapper.class,
        basePackages = {"cn.hsb.router.mapper.birouter"}
)
public class BiRouterDbConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.bi-router")
    public DataSource biRouterDatasource() {
        return DataSourceBuilder.create().type(DruidDataSource.class).build();
    }

    @Bean
    public SqlSessionFactoryBean biRouterSqlSessionFactory(@Qualifier("biRouterDatasource") DataSource dataSource,
                                                           ApplicationContext applicationContext) throws IOException {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);

        bean.setMapperLocations(applicationContext.getResources("classpath*:mybatis/mapper/birouter/*.xml"));
        bean.setTypeAliasesPackage("cn.hsb.router.entity");

        org.apache.ibatis.session.Configuration conf = new org.apache.ibatis.session.Configuration();
        conf.setMapUnderscoreToCamelCase(true);
        bean.setConfiguration(conf);

        return bean;
    }

    @Bean
    public DataSourceTransactionManager biRouterTransactionManager(@Qualifier("biRouterDatasource")DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SqlSessionTemplate biRouterSqlSessionTemplate(@Qualifier("biRouterSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}

//
//package com.winning.config;
//import java.util.Properties;
//
//import javax.sql.DataSource;
//
//import org.apache.ibatis.plugin.Interceptor;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.mybatis.spring.SqlSessionFactoryBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//import org.springframework.core.io.support.ResourcePatternResolver;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import com.github.pagehelper.PageHelper;
//
//import tk.mybatis.spring.mapper.MapperScannerConfigurer;
//
//@Configuration
//@EnableTransactionManagement
//public class MyBatisConfig{
//	@Bean(name = "sqlSessionFactory")
//	public SqlSessionFactory sqlSessionFactoryBean(DataSource dataSource)
//			throws Exception {
//		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
//		bean.setDataSource(dataSource);
//
//		PageHelper pageHelper = new PageHelper();
//		Properties p = new Properties();
//		p.setProperty("dialect", "sqlserver");//sqlserver
//		p.setProperty("offsetAsPageNum", "true");
//		p.setProperty("rowBoundsWithCount", "true");
//		p.setProperty("reasonable", "false");
//		pageHelper.setProperties(p);
//		bean.setPlugins(new Interceptor[] { pageHelper });
//
//		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//		bean.setMapperLocations(resolver.getResources("classpath:/mapper/**/*.xml"));
//		return bean.getObject();
//
//	}
//
//	@Bean
//	public PlatformTransactionManager transaction(DataSource dataSource) {
//		return new DataSourceTransactionManager(dataSource);
//	}
//
//	@Bean
//	public org.mybatis.spring.mapper.MapperScannerConfigurer mapperScannerConfigurer() {
//
//		MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
//		//org.mybatis.spring.mapper.MapperScannerConfigurer mapperScannerConfigurer=new org.mybatis.spring.mapper.MapperScannerConfigurer();
//
//		mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
//		mapperScannerConfigurer.setBasePackage("com.winning.dao");
//		return mapperScannerConfigurer;
//
//	}
//}
//

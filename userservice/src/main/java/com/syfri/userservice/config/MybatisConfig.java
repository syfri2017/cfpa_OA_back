package com.syfri.userservice.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.pagehelper.PageHelper;
import com.syfri.common.AccessFilter;
import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;

import java.util.Properties;

/**
 * 1.采用Druid连接池的dataSource的配置
 * 2.配置Druid监控
 * @author li.xue  2017/11/15
 */

@Configuration
@EnableTransactionManagement
@AutoConfigureAfter(Environment.class)
public class MybatisConfig implements EnvironmentAware {

	private static final Logger logger = LoggerFactory.getLogger(MybatisConfig.class);

	@Autowired
	private Environment environment;

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/**
	 * 配置DataSource
	 *
	 * @author li.xue  2017/11/15
	 */
	@Bean(name = "baseDataSource")
	@Primary //这里定义的DataSource将覆盖其他来源的DataSource
	public DruidDataSource dataSource() {
		DruidDataSource dataSource = new DruidDataSource();

		if(environment.containsProperty("spring.druid.datasource.driverClassName")){
			dataSource.setDriverClassName(environment.getProperty("spring.druid.datasource.driverClassName"));
		}
		if(environment.containsProperty("spring.druid.datasource.url")){
			dataSource.setUrl(environment.getProperty("spring.druid.datasource.url"));
		}
		if(environment.containsProperty("spring.druid.datasource.username")){
			dataSource.setUsername(environment.getProperty("spring.druid.datasource.username"));
		}
		if(environment.containsProperty("spring.druid.datasource.password")){
			dataSource.setPassword(environment.getProperty("spring.druid.datasource.password"));
		}
		if(environment.containsProperty("spring.druid.datasource.initialSize")){
			dataSource.setInitialSize(Integer.parseInt(environment.getProperty("spring.druid.datasource.initialSize")));
		}
		if(environment.containsProperty("spring.druid.datasource.minIdle")){
			dataSource.setMinIdle(Integer.parseInt(environment.getProperty("spring.druid.datasource.minIdle")));
		}
		if(environment.containsProperty("spring.druid.datasource.maxActive")){
			dataSource.setMaxActive(Integer.parseInt(environment.getProperty("spring.druid.datasource.maxActive")));
		}
		if(environment.containsProperty("spring.druid.datasource.maxWait")){
			dataSource.setMaxWait(Long.parseLong(environment.getProperty("spring.druid.datasource.maxWait")));
		}
		if(environment.containsProperty("spring.druid.datasource.timeBetweenEvictionRunsMillis")){
			dataSource.setTimeBetweenEvictionRunsMillis(Integer.parseInt(environment.getProperty("spring.druid.datasource.timeBetweenEvictionRunsMillis")));
		}
		if(environment.containsProperty("spring.druid.datasource.minEvictableIdleTimeMillis")){
			dataSource.setMinEvictableIdleTimeMillis(Long.parseLong(environment.getProperty("spring.druid.datasource.minEvictableIdleTimeMillis")));
		}
		if(environment.containsProperty("spring.druid.datasource.validationQuery")){
			dataSource.setValidationQuery(environment.getProperty("spring.druid.datasource.validationQuery"));
		}

		if (environment.containsProperty("spring.druid.datasource.testWhileIdle")) {
			String temp = environment.getProperty("spring.druid.datasource.testWhileIdle");
			if ("true".equals(temp)) {
				dataSource.setTestWhileIdle(true);
			} else {
				dataSource.setTestWhileIdle(false);
			}
		}
		if (environment.containsProperty("spring.druid.datasource.testOnBorrow")) {
			String temp = environment.getProperty("spring.druid.datasource.testOnBorrow");
			if ("true".equals(temp)) {
				dataSource.setTestOnBorrow(true);
			} else {
				dataSource.setTestOnBorrow(false);
			}
		}
		if (environment.containsProperty("spring.druid.datasource.testOnReturn")) {
			String temp = environment.getProperty("spring.druid.datasource.testOnReturn");
			if ("true".equals(temp)) {
				dataSource.setTestOnReturn(true);
			} else {
				dataSource.setTestOnReturn(false);
			}
		}
		if (environment.containsProperty("spring.druid.datasource.poolPreparedStatements")) {
			String temp = environment.getProperty("spring.druid.datasource.poolPreparedStatements");
			if ("true".equals(temp)) {
				dataSource.setPoolPreparedStatements(true);
			} else {
				dataSource.setPoolPreparedStatements(false);
			}
		}
		if(environment.containsProperty("spring.druid.datasource.maxPoolPreparedStatementPerConnectionSize")){
			dataSource.setMaxPoolPreparedStatementPerConnectionSize(Integer.parseInt(environment.getProperty("spring.druid.datasource.maxPoolPreparedStatementPerConnectionSize")));
		}
		if(environment.containsProperty("spring.druid.datasource.filters")){
			try {
				dataSource.setFilters(environment.getProperty("spring.druid.datasource.filters"));
			} catch (SQLException e) {
				logger.error("Druid configuration initialization filter", e);
			}
		}
		if(environment.containsProperty("spring.druid.datasource.connectionProperties")){
			String connectionPropertiesStr = environment.getProperty("spring.druid.datasource.connectionProperties");
			if (connectionPropertiesStr != null && !"".equals(connectionPropertiesStr)) {
				Properties connectionProperties = new Properties();
				String[] propertiesList = connectionPropertiesStr.split(";");
				for (String temp : propertiesList) {
					String[] obj = temp.split("=");
					connectionProperties.put(obj[0], obj[1]);
				}
				dataSource.setConnectProperties(connectionProperties);
			}
		}
		//设置WallFilter
		/* Oracle批量更新，但是不好用
		WallConfig wallConfig = new WallConfig();
		wallConfig.setMultiStatementAllow(true);
		wallConfig.setNoneBaseStatementAllow(true);
		WallFilter wallFilter = new WallFilter();
		wallFilter.setConfig(wallConfig);
		List<Filter> filterList = new ArrayList<>();
		filterList.add(wallFilter);
		dataSource.setProxyFilters(filterList);
		*/
		return dataSource;
	}

	/**
	 * 根据数据源创建SqlSessionFactory
	 * @author li.xue  2017/11/17
	 */
	@Bean(name = "sqlSessionFactory")
	public SqlSessionFactory sqlSessionFactory(DataSource ds) throws Exception {
		SqlSessionFactoryBean ssf = new SqlSessionFactoryBean();
		ssf.setDataSource(ds);

		VFS.addImplClass(SpringBootVFS.class);

		if (environment.containsProperty("spring.mybatis.configLocation")) {
			ssf.setConfigLocation(new PathMatchingResourcePatternResolver().getResource(environment.getProperty("spring.mybatis.configLocation")));
		}
		if (environment.containsProperty("spring.mybatis.mapperLocations")) {
			ssf.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(environment.getProperty("spring.mybatis.mapperLocations")));
		}
		if (environment.containsProperty("spring.mybatis.typeAliasesPackage")) {
			ssf.setTypeAliasesPackage(environment.getProperty("spring.mybatis.typeAliasesPackage"));
		}
		if (environment.containsProperty("spring.mybatis.typeHandlersPackage")) {
			ssf.setTypeHandlersPackage(environment.getProperty("spring.mybatis.typeHandlersPackage"));
		}

		//Configuration的设置，目前只配置了cache-enabled
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		//cache-enabled
		if (environment.containsProperty("spring.mybatis.configuration.cacheEnabled")) {
			String temp = environment.getProperty("spring.mybatis.configuration.cacheEnabled");
			if ("true".equals(temp)) {
				configuration.setCacheEnabled(true);
			} else {
				configuration.setCacheEnabled(false);
			}
		}
		//lazy-loading-enabled
		if (environment.containsProperty("spring.mybatis.configuration.lazy-loadingEnabled")) {
			String temp = environment.getProperty("spring.mybatis.configuration.lazy-loadingEnabled");
			if ("true".equals(temp)) {
				configuration.setLazyLoadingEnabled(true);
			} else {
				configuration.setLazyLoadingEnabled(false);
			}
		}
		ssf.setConfiguration(configuration);

		/*分页插件.*/
		PageHelper pageHelper = new PageHelper();
		Properties properties = new Properties();

		properties.setProperty("dialect", "oracle");

		/*默认为false；设置为true时，会将RowBounds第一个参数offset当成pageNum页码使用.*/
		properties.setProperty("offsetAsPageNum", "true");

		/*默认为false设置为true时，使用RowBounds分页会进行count查询.*/
		properties.setProperty("rowBoundsWithCount", "true");

		/*设置为true时，如果pageSize=0或者RowBounds.limit = 0就会查询出全部的结果.相当于没有执行分页查询，但是返回结果仍然是Page类型*/
		properties.setProperty("pageSizeZero", "true");

		/*启用合理化时，如果pageNum<1会查询第一页，如果pageNum>pages会查询最后一页.*/
		/*禁用合理化时，如果pageNum<1或pageNum>pages会返回空数据.*/
		properties.setProperty("reasonable", "true");

		/*3.5.0版本可用 - 为了支持startPage(Object params)方法.*/
		/*增加了一个`params`参数来配置参数映射，用于从Map或ServletRequest中取值.*/
		/*可以配置pageNum,pageSize,count,pageSizeZero,reasonable,不配置映射的用默认值.*/
		properties.setProperty("params", "pageNum=start;pageSize=limit;pageSizeZero=zero;reasonable=heli;count=contsql");

		pageHelper.setProperties(properties);
		//添加插件
		ssf.setPlugins(new Interceptor[]{pageHelper});
		try{
			return ssf.getObject();
		}catch(Exception e){
			throw new RuntimeException();
		}
	}

	@Bean
	public MapperScannerConfigurer mapperScannerConfigurer(){
		MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
		mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
		mapperScannerConfigurer.setBasePackage(environment.getProperty("spring.mybatis.basePackage"));
		return mapperScannerConfigurer;
	}

	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory){
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	//将自定义的AccessFilter注册到bean中
	@Bean
	public FilterRegistrationBean filter(){
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(new AccessFilter());
		return registrationBean;
	}

	@Bean
	public DataSourceTransactionManager dataSourceTransactionManager(DataSource ds) throws Exception{
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(ds);
		return transactionManager;
	}
	//流程配置，与spring整合采用SpringProcessEngineConfiguration这个实现
	@Bean
	public ProcessEngineConfiguration processEngineConfiguration(DataSource dataSource, PlatformTransactionManager transactionManager){
		SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
		processEngineConfiguration.setDataSource(dataSource);
		//processEngineConfiguration.setDatabaseSchemaUpdate("true");
		//processEngineConfiguration.setDatabaseType("mysql");

		//processEngineConfiguration.setTransactionManager(transactionManager);

		//流程图字体
		processEngineConfiguration.setActivityFontName("宋体");
		processEngineConfiguration.setAnnotationFontName("宋体");
		processEngineConfiguration.setLabelFontName("宋体");

		return processEngineConfiguration;
	}

	//流程引擎，与spring整合使用factoryBean
	@Bean
	public ProcessEngineFactoryBean processEngine(ProcessEngineConfiguration processEngineConfiguration){
		ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
		processEngineFactoryBean.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
		return processEngineFactoryBean;
	}

	//八大接口
	@Bean
	public RepositoryService repositoryService(ProcessEngine processEngine){
		return processEngine.getRepositoryService();
	}

	@Bean
	public RuntimeService runtimeService(ProcessEngine processEngine){
		return processEngine.getRuntimeService();
	}

	@Bean
	public TaskService taskService(ProcessEngine processEngine){
		return processEngine.getTaskService();
	}

	@Bean
	public HistoryService historyService(ProcessEngine processEngine){
		return processEngine.getHistoryService();
	}

	@Bean
	public FormService formService(ProcessEngine processEngine){
		return processEngine.getFormService();
	}

	@Bean
	public IdentityService identityService(ProcessEngine processEngine){
		return processEngine.getIdentityService();
	}

	@Bean
	public ManagementService managementService(ProcessEngine processEngine){
		return processEngine.getManagementService();
	}

	@Bean
	public DynamicBpmnService dynamicBpmnService(ProcessEngine processEngine){
		return processEngine.getDynamicBpmnService();
	}
}
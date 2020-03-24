package com.ninthnails.acme.hierarchy.config

import com.ninthnails.acme.hierarchy.repository.EmployeeRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class AppConfig {
    @Bean
    fun txManager(dataSource : DataSource) : PlatformTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }
    @Bean
    fun employeeRepository(jdbcTemplate : JdbcTemplate) : EmployeeRepository {
        return EmployeeRepository(jdbcTemplate)
    }
}
package com.ninthnails.acme.hierarchy.repository

import com.ninthnails.acme.hierarchy.domain.Employee
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import java.sql.PreparedStatement

@Transactional(readOnly = true)
class EmployeeRepository(private val jdbcTemplate : JdbcTemplate) {

    @Transactional(readOnly = false)
    fun storeAll(employees : List<Employee>) {
        jdbcTemplate.batchUpdate("INSERT INTO hierarchy (employee, supervisor) VALUES (?, ?)",
            object : BatchPreparedStatementSetter {
                override fun setValues(ps: PreparedStatement, i: Int) {
                    ps.setString(1, employees[i].name)
                    ps.setString(2, if (employees[i].supervisor != Employee.NONE) employees[i].supervisor?.name else null)
                }
                override fun getBatchSize(): Int {
                    return employees.size
                }
            })
    }

    @Transactional(readOnly = false)
    fun deleteAll() = jdbcTemplate.execute("DELETE FROM hierarchy")

    fun getAll() : Map<String, String> {
        val result = mutableMapOf<String, String>()
        val list = jdbcTemplate.queryForList("SELECT employee, supervisor FROM hierarchy")
        list.forEach {
            result[it["employee"] as String] = (it["supervisor"] ?: "NONE") as String
        }
        return result
    }
}
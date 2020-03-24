package com.ninthnails.acme.hierarchy.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmployeeTest {

    @Test
    fun testFindSubordinate() {
        val alice = Employee("Alice", Employee.NONE)
        alice.manage("Bob").manage("Charlie").manage("Dorothy")
        alice.manage("Edward")
        assertThat(alice.findSubordinate("Dorothy")).isNotNull.hasFieldOrPropertyWithValue("name", "Dorothy")
    }

    @Test
    fun testFindSubordinateWithNoEmployees() {
        val alice = Employee("Alice", Employee.NONE)
        assertThat(alice.findSubordinate("Dorothy")).isNotNull.isSameAs(Employee.NONE)
    }

    @Test
    fun testFindNonExistingSubordinate() {
        val alice = Employee("Alice", Employee.NONE)
        alice.manage("Bob").manage("Charlie").manage("Dorothy")
        assertThat(alice.findSubordinate("Zachary")).isNotNull.isSameAs(Employee.NONE)
    }
}
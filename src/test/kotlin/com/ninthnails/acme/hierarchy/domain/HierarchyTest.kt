package com.ninthnails.acme.hierarchy.domain

import com.ninthnails.acme.hierarchy.domain.Employee.Companion.NONE
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HierarchyTest {

    @Test
    fun testInsert() {
        val mapping = mutableMapOf<String, String>()
        mapping["Alice"] = "Charlie"
        mapping["Bob"] = "Charlie"
        mapping["Charlie"] = "Daisy"
        mapping["Daisy"] = "Elody"

        val hierarchy = Hierarchy(mapping).structure()

        assertThat(hierarchy.employees).isNotNull.isNotEmpty.hasSize(1)

        val elody = hierarchy.employees.iterator().next()
        assertThat(elody).isNotNull.hasFieldOrPropertyWithValue("name", "Elody").hasFieldOrPropertyWithValue("supervisor", NONE)

        val daisy = elody.subordinates.iterator().next()
        assertThat(daisy).isNotNull.hasFieldOrPropertyWithValue("name", "Daisy").hasFieldOrPropertyWithValue("supervisor", elody)

        val charlie = daisy.subordinates.iterator().next()
        assertThat(charlie).isNotNull.hasFieldOrPropertyWithValue("name", "Charlie").hasFieldOrPropertyWithValue("supervisor", daisy)

        charlie.subordinates.forEach {
            assertThat(it.name).isIn("Bob", "Alice")
            assertThat(it.supervisor).isSameAs(charlie)
        }
    }

    @Test(expected = CyclicHierarchyException::class)
    fun testInsertDirectCycle() {
        val mapping = mutableMapOf<String, String>()
        mapping["Alice"] = "Charlie"
        mapping["Charlie"] = "Alice"

        Hierarchy(mapping).structure()
    }

    @Test
    fun testInsertIndirectCycle() {
        val mapping = mutableMapOf<String, String>()
        mapping["Alice"] = "Charlie"
        mapping["Bob"] = "Alice"
        mapping["Charlie"] = "Bob"

        assertThat(Hierarchy(mapping).structure().isCyclic()).isNotEmpty
    }

    @Test
    fun testFlatten() {
        val mapping = mutableMapOf<String, String>()
        mapping["Alice"] = "Charlie"
        mapping["Alice"] = "Bob"
        mapping["Bob"] = "Charlie"

        assertThat(Hierarchy(mapping).structure().flatten()).isNotNull.isNotEmpty
    }
}
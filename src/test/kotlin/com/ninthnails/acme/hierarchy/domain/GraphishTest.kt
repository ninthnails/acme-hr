package com.ninthnails.acme.hierarchy.domain

import org.assertj.core.api.Assertions
import org.junit.Test

class GraphishTest {

    @Test
    fun testBaseline() {
        val cycles = Graphish(mapOf("P" to "N", "B" to "N", "N" to "S", "S" to "J")).isCyclic()
        Assertions.assertThat(cycles).isNotNull.isEmpty()
    }

    @Test
    fun testIndirectCycle() {
        val cycles = Graphish(mapOf("A" to "B", "B" to "C", "C" to "A")).isCyclic()
        Assertions.assertThat(cycles).isNotNull.isNotEmpty.hasSize(1)
    }

    @Test
    fun testDirectCycle() {
        val cycles = Graphish(mapOf("A" to "B", "B" to "A")).isCyclic()
        Assertions.assertThat(cycles).isNotNull.isNotEmpty.hasSize(1)
    }

    @Test
    fun testSelfCycle() {
        val cycles = Graphish(mapOf("A" to "A")).isCyclic()
        Assertions.assertThat(cycles).isNotNull.isNotEmpty.hasSize(1)
    }
}
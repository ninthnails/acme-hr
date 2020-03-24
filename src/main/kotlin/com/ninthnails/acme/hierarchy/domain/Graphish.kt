package com.ninthnails.acme.hierarchy.domain

import java.util.LinkedList
import kotlin.collections.HashMap

/**
 * Represents a graph like structure of simple string to string mapping.
 */
class Graphish(input : Map<String, String>) {
    private val links : MutableMap<String, LinkedList<String>> = HashMap()

    init {
        input.forEach { (source, destination) ->
            links.computeIfAbsent(source) { LinkedList() }.add(destination)
        }
    }

    /**
     * Returns the cycles find, if any, in the graph.
     */
    fun isCyclic() : Map<String, Collection<String>> {
        val visited: MutableMap<String, Boolean> = mutableMapOf()
        val stack: MutableMap<String, Boolean> = mutableMapOf()
        for (vertex in links.keys) {
            if (search(vertex, visited, stack)) {
                return mapOf(vertex to stack.keys)
            }
        }
        return mapOf()
    }

    /**
     * Do Depth-first-search on the graph.
     */
    private fun search(vertex : String, visited : MutableMap<String, Boolean>, stack : MutableMap<String, Boolean>) : Boolean {
        visited[vertex] = true
        stack[vertex] = true

        for (child in links.getOrDefault(vertex, emptySet<String>())) {
            if (!visited.getOrDefault(child, false) && search(child, visited, stack)) {
                return true
            } else if (stack.getOrDefault(child, false)) {
                return true
            }
        }
        stack[vertex] = false
        return false
    }
}
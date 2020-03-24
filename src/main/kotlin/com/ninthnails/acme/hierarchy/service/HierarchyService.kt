package com.ninthnails.acme.hierarchy.service

import com.ninthnails.acme.hierarchy.HierarchyController
import com.ninthnails.acme.hierarchy.domain.CyclicHierarchyException
import com.ninthnails.acme.hierarchy.domain.Hierarchy
import com.ninthnails.acme.hierarchy.domain.MultipleRootsHierarchyException
import com.ninthnails.acme.hierarchy.repository.EmployeeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class HierarchyService(val employeeRepository : EmployeeRepository) {

    companion object {
        fun logger(): Logger = LoggerFactory.getLogger(HierarchyController::class.qualifiedName)
    }

    /**
     * Saves proposed hierarchy as simple employee to supervisor mapping.
     * @throws CyclicHierarchyException if evaluated graph form mapping contains cycle (loops)
     * @throws MultipleRootsHierarchyException if evaluated graph form mapping contains more than one root node
     */
    @Transactional(readOnly = false)
    fun saveHierarchy(data : Map<String, String>) : Hierarchy {
        val hierarchy = Hierarchy(data)

        // Detect first loops
        val cycles = hierarchy.isCyclic()

        logger().info("Proposed hierarchy has {} cycle(s)", cycles.size)

        if (cycles.isNotEmpty()) {
            val first = cycles.entries.first()
            throw CyclicHierarchyException("Invalid cyclic referencing for ${first.key} toward one of ${first.value}")
        }

        hierarchy.structure()
        if (hierarchy.employees.size > 1) {
            throw MultipleRootsHierarchyException("Hierarchy has multiple roots: ${hierarchy.employees.map { it.name }}")
        }

        // Since requirements aren't specific as whether a POST is an "update" rather than a "save",
        // I interpret it as save, i.e. what is given is what we expect to be. Hence,
        // we remove all data as we don't want dangling records not par of the hierarchy.
        // Much simpler than trying to do a diff. Quite drastic, but effective.
        employeeRepository.deleteAll()
        logger().info("Current hierarchy has been dropped")

        employeeRepository.storeAll(hierarchy.flatten())
        logger().info("New hierarchy has been saved")

        return hierarchy
    }

    /**
     * Retrieves structured hierarchy.
     */
    fun getHierarchy() : Hierarchy {
        val data = employeeRepository.getAll()
        return Hierarchy(data).structure()
    }

}
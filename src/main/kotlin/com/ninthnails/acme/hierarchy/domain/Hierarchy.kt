package com.ninthnails.acme.hierarchy.domain

import com.ninthnails.acme.hierarchy.HierarchyController
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Represents the organisational structure of employees.
 */
class Hierarchy(private val data : Map<String, String>) {

    companion object {
        fun logger(): Logger = LoggerFactory.getLogger(HierarchyController::class.qualifiedName)
    }

    val employees : MutableSet<Employee> = mutableSetOf()
    private val graph : Graphish = Graphish(if (data.isNotEmpty()) data else mapOf())

    /**
     * @see Graphish.isCyclic
     */
    fun isCyclic(): Map<String, Collection<String>> {
       return graph.isCyclic()
    }

    /**
     * Build up the hierarchy. This should be call after checking for potential cycle.
     */
    fun structure() : Hierarchy {
        data.forEach { (e, s) -> if (s != "NONE") insert(s, e) }
        return this
    }

    /**
     * Insert the pair of supervisor and employee in the set of employees.
     */
    private fun insert(supervisorName : String, subordinateName : String) {
        if (employees.isEmpty()) {
            logger().debug("First time inserting, no need to search for supervisor")

            val supervisor = Employee(supervisorName, Employee.NONE)
            employees.add(supervisor)
            logger().debug("Inserted supervisor {}", supervisorName)

            supervisor.manage(Employee(subordinateName, supervisor))
            logger().debug("Inserted subordinate {} under {}", subordinateName, supervisor.name)
        } else {
            var supervisor = find(supervisorName)

            if (supervisor != Employee.NONE) {
                logger().debug("Found supervisor {}", supervisor.name)

                val subordinate = find(subordinateName)
                if (subordinate == Employee.NONE) {
                    logger().debug("Subordinate {} not found", subordinateName)

                    supervisor.manage(Employee(subordinateName, supervisor))
                    logger().debug("Inserted subordinate {} under {}", subordinateName, supervisor.name)
                } else {
                    logger().debug("Found subordinate {}", subordinate.name)

                    if (supervisor != subordinate && supervisor.supervisor != subordinate) {
                        logger().debug("Subordinate {} not found", subordinateName)

                        employees.remove(subordinate)
                        logger().debug("Pulled subordinate {}", subordinate.name)

                        subordinate.supervisor = supervisor
                        supervisor.manage(subordinate)
                        logger().debug("Inserted subordinate {} under {}", subordinate.name, supervisor.name)
                    } else {
                        throw CyclicHierarchyException("Invalid cyclic referencing for ${supervisor.name} toward either ${supervisor.supervisor?.name}")
                    }
                }
            } else {
                logger().debug("No supervisor found for {}", supervisorName)

                supervisor = Employee(supervisorName, Employee.NONE)
                employees.add(supervisor)
                logger().debug("Inserted supervisor {}", supervisor.name)

                insert(supervisorName, subordinateName)
            }
        }
    }

    /**
     * Try to find an employee in the hierarchy, trickling down the paths.
     */
    fun find(name : String) : Employee {
        employees.forEach { it ->
            if (it.name == name) {
                return it
            }
            val found = it.findSubordinate(name)
            if (found != Employee.NONE) {
                return found
            }
        }
        return Employee.NONE
    }

    /**
     * Returns a flat representation of the entire hierarchy as a plain list of employees
     */
    fun flatten() : List<Employee> {
        val flat = mutableListOf<Employee>()
        collect(employees.iterator().next(), flat)
        return flat
    }

    private fun collect(e : Employee, set: MutableList<Employee>) {
        set.add(e)
        e.subordinates.forEach { collect(it, set) }
    }

    override fun toString(): String = employees.toString()
}

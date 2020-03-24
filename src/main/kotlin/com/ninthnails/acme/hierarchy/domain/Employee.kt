package com.ninthnails.acme.hierarchy.domain

import java.lang.StringBuilder

/**
 * Entity representing someone part of a hierarchy that knows its direct supervisor and employees.
 */
class Employee(val name: String, var supervisor: Employee?) {

    val subordinates = mutableSetOf<Employee>()

    companion object {
        val NONE = Employee("none", null)
    }

    /**
     * Add employee to this set of managed employees.
     */
    fun manage(employee : Employee) = subordinates.add(employee)

    /**
     * Convenient method for adding an employee to this set of managed employees.
     * Support chaining calls.
     */
    fun manage(name : String) : Employee {
        val employee = Employee(name, this)
        manage(employee)
        return employee
    }

    /**
     * Try to find an employee in the sub-hierarchy starting from this employee.
     */
    fun findSubordinate(name : String) : Employee = if (this.name == name) this else walkDown(this, name)

    private fun walkDown(employee : Employee, name : String) : Employee {
        if (employee.name == name) return employee

        var found = NONE
        val iterator = employee.subordinates.iterator()
        while (found == NONE && iterator.hasNext()) {
            found = walkDown(iterator.next(), name)
        }
        return found
    }

    override fun toString(): String {
        val builder = StringBuilder("$name: {")
        val iterator = subordinates.iterator()
        while (iterator.hasNext()) {
            builder.append(iterator.next().toString()).append(if (iterator.hasNext()) ", " else "")
        }
        return builder.append("}").toString()
    }
}

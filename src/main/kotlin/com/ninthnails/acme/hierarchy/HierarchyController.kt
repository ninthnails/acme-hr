package com.ninthnails.acme.hierarchy

import com.ninthnails.acme.hierarchy.domain.Employee
import com.ninthnails.acme.hierarchy.domain.ValidationException
import com.ninthnails.acme.hierarchy.service.HierarchyService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import javax.validation.constraints.NotBlank

@Controller
@RequestMapping("/api/v1/")
@Transactional(readOnly = true)
class HierarchyController(val hierarchyService : HierarchyService) {

    companion object {
        fun logger(): Logger = LoggerFactory.getLogger(HierarchyController::class.qualifiedName)
    }

    @PostMapping(path = ["/hierarchy"], consumes = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = false)
    fun saveHierarchy(@RequestBody data : Map<String, String>) : ResponseEntity<Map<String, Any>> {
        // FIXME I couldn't make Spring Validation working, seems not to be playing balls with Kotlin (annotation stuff)
        if (data.isEmpty() || data.filter { it.key.isBlank() || it.value.isBlank() }.isNotEmpty()) {
            throw ValidationException("Data can't contain blank values")
        }

        logger().info("Processing mapping: {}", data)

        val hierarchy = hierarchyService.saveHierarchy(data)

        val result = mutableMapOf<String, Any>()
        hierarchy.employees.forEach { convert(it, result) }

        logger().info("Hierarchy is now: {}", result)

        return ResponseEntity.ok(result)
    }

    private fun convert(employee : Employee, data : MutableMap<String, Any>) {
        if (employee.subordinates.isNotEmpty()) {
            val subs = mutableMapOf<String, Any>()
            employee.subordinates.forEach { convert(it, subs) }
            data[employee.name] = subs
        } else {
            data[employee.name] = mutableMapOf<String, Any>()
        }
    }

    @GetMapping(path = ["/hierarchy"], produces = ["application/json"])
    @ResponseBody
    fun getHierarchy() : ResponseEntity<Map<String, Any>> {
        val result = mutableMapOf<String, Any>()

        val hierarchy = hierarchyService.getHierarchy()
        hierarchy.employees.forEach { convert(it, result) }

        return ResponseEntity.ok(result)
    }

    @GetMapping(path = ["/hierarchy/employees/{name}/supervisors"], produces = ["application/json"])
    @ResponseBody
    fun getSupervisors(@PathVariable @NotBlank name : String) : ResponseEntity<Map<String, Any>>? {
        logger().info("Searching supervisor chain for {}", name)

        // TODO Although really not ideal, this is the simplest way for now.
        val hierarchy = hierarchyService.getHierarchy()

        val employee = hierarchy.find(name)
        if (employee == Employee.NONE) {
            logger().info("Employee {} not found in hierarchy", name)
            return ResponseEntity.notFound().build()
        }

        val result = mutableMapOf<String, Any>()

        // Requirements only ask for two levels up
        val direct = employee.supervisor
        val second = employee.supervisor?.supervisor
        if (direct == Employee.NONE) {
            result["<NONE>"] = employee.name
        } else if (direct != Employee.NONE && second == Employee.NONE) {
            result[direct!!.name] = mapOf<String, Any>(employee.name to mapOf<String, Any>())
        } else {
            result[second!!.name] = mapOf<String, Any>(direct!!.name to mapOf<String, Any>(employee.name to mapOf<String, Any>()))
        }

        logger().info("Employee supervisor(s) is/are {}", result)

        return ResponseEntity.ok(result)
    }
}
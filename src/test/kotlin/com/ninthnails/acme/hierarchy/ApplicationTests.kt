package com.ninthnails.acme.hierarchy

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(secure = false)
@Sql(scripts = ["/schema.sql", "/data.sql"])
class ApplicationTests {

	companion object {
		const val APP_JSON = "application/json"
		const val DATA = "{\"Alice\":\"Charlie\",\"Bob\":\"Charlie\",\"Charlie\":\"Daisy\",\"Daisy\":\"Elody\"}"
		const val HIERARCHY = "{\"Elody\":{\"Daisy\":{\"Charlie\":{\"Alice\":{},\"Bob\":{}}}}}"
	}

	@Autowired
	val mvc: MockMvc? = null

	private fun forPost(url: String): MockHttpServletRequestBuilder {
		return post("/api/v1$url").with(httpBasic("test", "test")).contentType(APP_JSON)
	}

	private fun forGet(url: String, vararg uriVars : Any): MockHttpServletRequestBuilder {
		return get("/api/v1$url", *uriVars).with(httpBasic("test", "test")).accept(APP_JSON)
	}

	@Test
	fun postFullDataSet() {
		mvc?.perform(forPost("/hierarchy").content(DATA))?.andExpect(status().isOk)
	}

	@Test
	fun postUnsupportedStructure() {
		val content = "{\"Alice\": {\"Charlie\":\"Bob\"}}}"
		mvc?.perform(forPost("/hierarchy").content(content))?.andExpect(status().isBadRequest)
	}

	@Test
	fun postEmptyDataSet() {
		mvc?.perform(forPost("/hierarchy").content("{}"))?.andExpect(status().isBadRequest)
	}

	@Test
	fun postMalformed() {
		mvc?.perform(forPost("/hierarchy").content("{\"Alice\": {\"Charlie\": }}}"))?.andExpect(status().isBadRequest)
		mvc?.perform(forPost("/hierarchy").content("{\"\": \"Charlie\"}"))?.andExpect(status().isBadRequest)
	}

	@Test
	fun postNoData() {
		mvc?.perform(forPost("/hierarchy"))?.andExpect(status().isBadRequest)
	}

	@Test
	fun postIndirectCycle() {
		val content =  "{\"Alice\":\"Charlie\",\"Charlie\":\"Daisy\",\"Daisy\":\"Alice\"}"
		mvc?.perform(forPost("/hierarchy").content(content))?.andExpect(status().isBadRequest)
	}

	@Test
	fun postDirectCycle() {
		val content =  "{\"Alice\":\"Charlie\",\"Charlie\":\"Alice\"}"
		mvc?.perform(forPost("/hierarchy").content(content))?.andExpect(status().isBadRequest)
	}

	@Test
	fun postMultipleRoots() {
		val content = "{\"Alice\":\"Charlie\",\"Charlie\":\"Alice\"}"
		mvc?.perform(forPost("/hierarchy").content(content))?.andExpect(status().isBadRequest)
	}

	@Test
	fun postEmptyValue() {
		val content = "{\"Alice\": \"\"}"
		mvc?.perform(forPost("/hierarchy").content(content))?.andExpect(status().isBadRequest)
	}

	@Test
	fun getHierarchy() {
		mvc?.perform(forGet("/hierarchy"))?.andExpect(status().isOk)?.andExpect(content().json(HIERARCHY))
	}

	@Test
	fun getEmployeeSupervisor() {
		val actual = "{\"Daisy\":{\"Charlie\":{\"Alice\":{}}}}"
		mvc?.perform(forGet("/hierarchy/employees/{name}/supervisors", "Alice"))?.andExpect(status().isOk)?.andExpect(content().json(actual))
	}

	@Test
	fun getUnknownEmployeeSupervisor() {
		mvc?.perform(forGet("/hierarchy/employees/{name}/supervisors", "Seraphin"))?.andExpect(status().isNotFound)
	}

	@Test
	fun getRootEmployeeSupervisor() {
		mvc?.perform(forGet("/hierarchy/employees/{name}/supervisors", "Elody"))
				?.andExpect(status().isOk)?.andExpect(content().json("{\"<NONE>\":\"Elody\"}"))
	}
}

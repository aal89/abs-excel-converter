package com.weaverplatform.absexcelconverter.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class ApplicationTest extends BaseTest {

	@Test
	public void rootTest() {

		given()
			.contentType("application/json")
			.expect()
			.statusCode(200)
			.when()
			.get("http://localhost:4567/");

	}

	@Test
	public void aboutTest() {

		given()
			.contentType("application/json")
			.expect()
			.statusCode(200)
			.when()
			.get("http://localhost:4567/about");

	}

	@Test
	public void connectionTest() {

		given()
			.contentType("application/json")
			.expect()
			.statusCode(204)
			.when()
			.get("http://localhost:4567/connection");

	}

	@Test
	public void notfoundTest() {

		given()
			.contentType("application/json")
			.expect()
			.statusCode(404)
			.body(equalTo("404 - Route not found"))
			.when()
			.get("http://localhost:4567/this-route-may-never-exist-for-testing-purposes-only");

	}
}

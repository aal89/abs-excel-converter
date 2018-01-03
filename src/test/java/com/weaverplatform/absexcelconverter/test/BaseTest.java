package com.weaverplatform.absexcelconverter.test;

import org.junit.BeforeClass;

import com.weaverplatform.absexcelconverter.Application;

import io.restassured.RestAssured;

/**
 * @author Mohamad Alamili
 */
public class BaseTest {

  static Application application;

  @BeforeClass
  public static void setup(){
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = 4567;

    if(application == null) {
      application = new Application();
    }
  }
  
}

package com.weaverplatform.absexcelconverter.test;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;

import com.google.common.io.Resources;

public class FileTest extends BaseTest {

  @Test
  public void uploadTest() throws URISyntaxException {
    
    File file = new File(Resources.getResource("excel/abs_import_sheet(invalid_3).xlsx").toURI());
    
    given()
      .multiPart(file)
      .expect()
      .statusCode(400)
      .when()
      .post("http://localhost:4567/upload");

  }
  
  @Test
  public void upload2Test() throws URISyntaxException {
    
    File file = new File(Resources.getResource("excel/abs_import_sheet(invalid_4).xlsx").toURI());
    
    given()
      .multiPart(file)
      .expect()
      .statusCode(400)
      .when()
      .post("http://localhost:4567/upload");

  }
  
  @Test
  public void upload3Test() throws URISyntaxException {
    
    File file = new File(Resources.getResource("excel/abs_import_sheet(invalid_1).xlsx").toURI());
    
    given()
      .multiPart(file)
      .expect()
      .statusCode(400)
      .when()
      .post("http://localhost:4567/upload");

  }
  
  @Test
  public void upload4Test() throws URISyntaxException {
    
    File file = new File(Resources.getResource("excel/abs_import_sheet(invalid_2).xlsx").toURI());
    
    given()
      .multiPart(file)
      .expect()
      .statusCode(400)
      .when()
      .post("http://localhost:4567/upload");

  }
  
  @Test
  public void upload5Test() throws URISyntaxException {
    
    File file = new File(Resources.getResource("excel/abs_import_sheet(valid_1).xlsx").toURI());
    
    given()
      .multiPart(file)
      .expect()
      .statusCode(200)
      .when()
      .post("http://localhost:4567/upload");

  }

}

/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.


 https://login.salesforce.com/services/oauth2/authorize?response_type=code&client_id=3MVG9yZ.WNe6byQDinV4pEtYbk.XKrK3LwCNZtKCJ9lKnd6keoaNjuNXu7i3EBK_lLzNSZnXAkQE.2gw4xFZn&redirect_uri=http://localhost:8080/ForceNavigator/_auth&prompt=login%20consent&display=page
 */

package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import static javax.measure.unit.SI.KILOGRAM;
import javax.measure.quantity.Mass;
import org.jscience.physics.model.RelativisticModel;
import org.jscience.physics.amount.Amount;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.Header;


@Controller
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "index";
  }

  @RequestMapping("/sfauth")
  public String sfoauth() throws IOException {

    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost("https://login.salesforce.com/services/oauth2/authorize");
 
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("response_type", "code"));
    params.add(new BasicNameValuePair("client_id", "3MVG9yZ.WNe6byQDinV4pEtYbk.XKrK3LwCNZtKCJ9lKnd6keoaNjuNXu7i3EBK_lLzNSZnXAkQE.2gw4xFZn"));
    params.add(new BasicNameValuePair("redirect_uri", "https://localhost:5000/oauth/_callback"));
    params.add(new BasicNameValuePair("display", "page"));
    
    httpPost.setEntity(new UrlEncodedFormEntity(params));
 
    CloseableHttpResponse response = client.execute(httpPost);
    //assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
    client.close();

    System.out.println("### HTTP: "+response);

    System.out.println("### "+response.getStatusLine());

    String redirectUrl = "https://login.salesforce.com";
    Header[] headers = response.getAllHeaders();
    for (Header header : headers) {
      System.out.println("#Key : " + header.getName() + " ,#Value : " + header.getValue());
      String key =  header.getName();
      if (key.equals("Location")){
          redirectUrl = header.getValue();
      }

    }
    System.out.println("### SFDC OAUTH URL: "+redirectUrl);

    return "redirect:" + redirectUrl;

  }

  // Method will recieve authorization code from Salesforce to get access and refresh tokens
  // Auth code will expire after 15 min in thsi OAuth flow
  @RequestMapping(value="/oauth/_callback", method={RequestMethod.GET,RequestMethod.POST}, produces=MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  public String oauth(@RequestParam("code") String code) throws IOException {
    System.out.println("### OAuth RESPONSE: "+code);

    try{
      CloseableHttpClient client = HttpClients.createDefault();
      HttpPost httpPost = new HttpPost("https://login.salesforce.com/services/oauth2/token");
 
      List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("grant_type", "authorization_code"));
      params.add(new BasicNameValuePair("client_id", "3MVG9yZ.WNe6byQDinV4pEtYbk.XKrK3LwCNZtKCJ9lKnd6keoaNjuNXu7i3EBK_lLzNSZnXAkQE.2gw4xFZn"));
      params.add(new BasicNameValuePair("client_secret", "8219049706333485472"));
      params.add(new BasicNameValuePair("redirect_uri", "https://localhost:5000/oauth/_callback"));
      params.add(new BasicNameValuePair("code", code)); // Add authorization code from login attempt
    
      httpPost.setEntity(new UrlEncodedFormEntity(params));
 
      CloseableHttpResponse response = client.execute(httpPost);
      //assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
      

      HttpEntity entity = response.getEntity();
      // Read the contents of an entity and return it as a String.
      String content = EntityUtils.toString(entity);
      System.out.println("### BODY: "+content);
      client.close();

      
      
    } catch (IOException e) {
        e.printStackTrace();
    }
    return "hello";
  }

  @RequestMapping("/hello")
  String hello(Map<String, Object> model) {
    RelativisticModel.select();
    Amount<Mass> m = Amount.valueOf("12 GeV").to(KILOGRAM);
    model.put("science", "E=mc^2: 12 GeV = " + m.toString());
    return "hello";
  }

  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
      ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from DB: " + rs.getTimestamp("tick"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

}

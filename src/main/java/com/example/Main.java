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
/*
    initParams = { 
    @WebInitParam(name = "clientId", value = "3MVG9yZ.WNe6byQDinV4pEtYbk.XKrK3LwCNZtKCJ9lKnd6keoaNjuNXu7i3EBK_lLzNSZnXAkQE.2gw4xFZn"),
    @WebInitParam(name = "clientSecret", value = "8219049706333485472"),
    @WebInitParam(name = "redirectUri", value = "https://localhost:5000/oauth/_callback"),
    @WebInitParam(name = "environment", value = "https://login.salesforce.com/services/oauth2/token")  }
 
    HttpClient httpclient = new HttpClient();
    PostMethod post = new PostMethod(environment);
    post.addParameter("code",code);
    post.addParameter("grant_type","authorization_code");

   ** For session ID instead of OAuth 2.0, use "grant_type", "password" **
    post.addParameter("client_id",clientId);
    post.addParameter("client_secret",clientSecret);
    post.addParameter("redirect_uri",redirectUri);  

    HttpResponse response = httpclient.execute(post);
**/

    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost("https://login.salesforce.com/services/oauth2/authorize");
 
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("grant_type", "authorization_code"));
    //params.add(new BasicNameValuePair("response_type", "code"));
    params.add(new BasicNameValuePair("client_id", "3MVG9yZ.WNe6byQDinV4pEtYbk.XKrK3LwCNZtKCJ9lKnd6keoaNjuNXu7i3EBK_lLzNSZnXAkQE.2gw4xFZn"));
    params.add(new BasicNameValuePair("client_secret", "8219049706333485472"));
    params.add(new BasicNameValuePair("redirect_uri", "https://localhost:5000/oauth/_callback"));
    
    httpPost.setEntity(new UrlEncodedFormEntity(params));
 
    CloseableHttpResponse response = client.execute(httpPost);
    //assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
    client.close();

    System.out.println("### HTTP: "+response);
    return "hello";
  }

  @RequestMapping("/oauth/_callback")
  String oauth() {
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

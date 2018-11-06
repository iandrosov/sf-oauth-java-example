/*
 * MIT License
 *
 * Copyright (c) 2018 Igor Androsov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
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

import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
  // ------- Connected App Parameters ------
  // For production code tehe value need to be store secure.
  // For local run time use .env config file to store local convigration property
  // To apply this code to a specific Salesforce Connected App security context replace bellow consumer key, secret and callback URL with
  // one from your own Connected APP parameters
  private static String SF_CLIENT_ID     = "3MVG9yZ.WNe6byQDinV4pEtYbk.XKrK3LwCNZtKCJ9lKnd6keoaNjuNXu7i3EBK_lLzNSZnXAkQE.2gw4xFZn";
  private static String SF_CLIENT_SECRET = "8219049706333485472";
  private static String SF_REDIRECT_URI  = "https://localhost:5000/oauth/_callback";
  private static String SF_AUTH_ENDPOINT = "https://login.salesforce.com/services/oauth2";

  private String instance_url;
  private String access_token;
  private String refresh_token;

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }
  // Strating method for main home page index
  @RequestMapping("/")
  String index() {
    return "index";
  }

  // Method to initiate Salesforce Login OAuth Flow using default consumer key for basic dev org
  // enables to login to any Salesforce ORG with athorization via Salesforce Central IDP
  // This method will perform a redirect to Salesforce autorization point : https://login.salesforce.com/services/oauth2/authorize
  // result getting auth code that will be used next to get actual access & reefresh tokens
  @RequestMapping("/sfauth")
  public String sfoauth() throws IOException{

    String redirectUrl = "https://login.salesforce.com"; // default login Salesforce URL
    // Initialize HTTP Client
      CloseableHttpClient client = HttpClients.createDefault();
      HttpPost httpPost = new HttpPost(SF_AUTH_ENDPOINT + "/authorize"); // Request Authorization
   
      List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("response_type", "code"));
      params.add(new BasicNameValuePair("client_id", SF_CLIENT_ID));
      params.add(new BasicNameValuePair("redirect_uri", SF_REDIRECT_URI)); 
      params.add(new BasicNameValuePair("display", "page"));

    
      
      httpPost.setEntity(new UrlEncodedFormEntity(params));
   
      CloseableHttpResponse response = client.execute(httpPost);
      client.close();

      System.out.println("### HTTP: "+response);
      System.out.println("### "+response.getStatusLine());

      Header[] headers = response.getAllHeaders();
      for (Header header : headers) {
        System.out.println("#Key : " + header.getName() + " ,#Value : " + header.getValue());
        String key =  header.getName();
        if (key.equals("Location")){
            redirectUrl = header.getValue();
        }

      }
    System.out.println("### SFDC OAUTH URL: "+redirectUrl);

    return "redirect:" + redirectUrl; // redirect to Salesforce login front doore

  }

  // Method will recieve authorization code from Salesforce to get access and refresh tokens
  // Auth code will expire after 15 min in thsi OAuth flow
  @RequestMapping(value="/oauth/_callback", method={RequestMethod.GET,RequestMethod.POST}, produces=MediaType.TEXT_PLAIN_VALUE)
  //@ResponseBody
  public String oauth(@RequestParam("code") String code) throws IOException {
    System.out.println("### OAuth RESPONSE: "+code);

    
      CloseableHttpClient client = HttpClients.createDefault();
      HttpPost httpPost = new HttpPost(SF_AUTH_ENDPOINT + "/token"); // Request for TOKEN
 
      List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("grant_type", "authorization_code"));
      params.add(new BasicNameValuePair("client_id", SF_CLIENT_ID));
      params.add(new BasicNameValuePair("client_secret", SF_CLIENT_SECRET));
      params.add(new BasicNameValuePair("redirect_uri", SF_REDIRECT_URI));
      params.add(new BasicNameValuePair("code", code)); // Add authorization code from login attempt
    
      httpPost.setEntity(new UrlEncodedFormEntity(params));
 
      CloseableHttpResponse response = client.execute(httpPost);
      

      HttpEntity entity = response.getEntity();
      // Read the contents of an entity and return it as a String.
      String content = EntityUtils.toString(entity);
      System.out.println("### BODY: "+content);
      client.close();

      JsonParser jsonParser = new BasicJsonParser();
      Map<String, Object> jsonMap = jsonParser.parseMap(content);
      this.instance_url  = (String)jsonMap.get("instance_url");
      this.refresh_token = (String)jsonMap.get("refresh_token");
      this.access_token  = (String)jsonMap.get("access_token");

      // Here we get accsess & refresh tokens from response
      System.out.println("### URL: "+instance_url);
      System.out.println("### Access Token: "+access_token);
      System.out.println("### Refresh Token: "+refresh_token);

    return "redirect:/authresult";
  }

  @RequestMapping("/authresult")
  String authresult(Map<String, Object> model) {
    model.put("instance_url", this.instance_url);
    model.put("access_token", this.access_token);
    model.put("refresh_token", this.refresh_token);

    return "authresult";
  }

  // Test code method check environment values
  @RequestMapping("/hello")
  String hello(Map<String, Object> model) {
    RelativisticModel.select();
    // Get Values from .env config
    String energy = System.getenv().get("ENERGY");
    System.out.println("### ENERGY: "+energy);
    if (energy == null) {
       energy = "12 GeV";
    }
    String secret = System.getenv().get("SF_CLIENT_SECRET");
    System.out.println("### SF_CLIENT_SECRET:"+secret);

    Amount<Mass> m = Amount.valueOf(energy).to(KILOGRAM);
    model.put("science", "E=mc^2: "+energy+" = " + m.toString());
    return "hello";
  }





  // ========= THIS SECTION CODE NOT USED IN OAUTH DEMO ===========

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

# sf-oauth-java-example
Java web app example intended to demostrate how to connect via OAuth to Salesforce for REST or SOAP API calls.

This example web application is Java Spring Boot based implementation of [Web Server OAuth Authentication Flow](https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/intro_understanding_web_server_oauth_flow.htm) to enable access to Salesforce Platform REST API.

## Running Locally

Make sure you have Java and Maven installed.  Also, install the [Heroku CLI](https://cli.heroku.com/).

```sh
$ git clone https://github.com/iandrosov/sf-oauth-java-example
$ cd sf-oauth-java-example
$ mvn install
$ heroku local:start
```

Your app should now be running on [localhost:5000](http://localhost:5000/).

# sf-oauth-java-example
Java web app example intended to demostrate how to connect via OAuth to Salesforce for REST or SOAP API calls.

This example web application is Java Spring Boot based implementation of [Web Server OAuth Authentication Flow](https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/intro_understanding_web_server_oauth_flow.htm) to enable access to Salesforce Platform API. The resulting authenticated session can be used to call any Salesforce REST or SOAP API.

The app can deploy to Heroku or run locally.
Here is an [example app](https://sf-oauth-java-example.herokuapp.com/) feel free to try it Live on Heroku.

To deploy your own copy of this app use this handy Heroku button.

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

## Running Locally

Make sure you have Java and Maven installed.  Also, install the [Heroku CLI](https://cli.heroku.com/).

```
$ git clone https://github.com/iandrosov/sf-oauth-java-example
$ cd sf-oauth-java-example
$ mvn install or mvn clean install
$ heroku local:start
```

Your app should now be running on [localhost:5000](http://localhost:5000/).

## Environment Configuration

This applcation will use OAuth to authorize user access to Salesforce ORG. The app will need environment configuration for Connected APP to set consumer key and secret values.

### Connected APP

Create your connected app that will represent your web app in Salesforce org. Once app is set up copy consumer key and secret generated for this new connected app.

### Set Enviroment Variables Local

For local runtime create `.env` file in your application root directory and set tehse variables

```
SF_CLIENT_ID=<Consumer Key from Connected App>
SF_CLIENT_SECRET=<Consumer secret from Connected App>
SF_REDIRECT_URI=https://localhost:5000/oauth/_callback
```

The callback URL need to be set on connected app, it can be any valid url choice taht your app will respond to, we selected this example `https://localhost:5000/oauth/_callback`. 

This is the url Salesforce will call HTTP GET method on to send tokens to. Your app needs to code this endpoint to get access tokens. Note callback must be HTTPS, the HTTP will not work.

### Set Environment on Heroku

To reun on Heroku app, need to configure Heroku Variables

```
SF_CLIENT_ID=<Consumer Key from Connected App>
SF_CLIENT_SECRET=<Consumer secret from Connected App>
SF_REDIRECT_URI=<Heroku app URL>/oauth/_callback
```

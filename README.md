# Choices

A small maven project to test Google App Engine features.

It allow you to create question-based rooms and with the room number other people can login and respond to questions on their mobile.

## Features
* Room creation
* Google + login
* Mobile answering
* Question database / customisation

## Languages / Frameworks / Libraries

* Front :
  * HTML 5 / CSS 3
  * JQuery
  * Bootstrap 4
  * Font-awesome 5
* Back :
  * Java 8
  * Google Appengine API 1.9.64
  * Google Cloud Client 0.47.0-alpha
  * JSON 20180130 (json.org)
  * Betterlists 1.4 (klemek)
  * Testing : Junit 4.12
  * Testing : Mockito 2.8.9
  * Testing : Powermock 1.7.1

## Launching the project

### Before launch

First, in your Google App Engine project, make sure Datastores and Google+ APIs are enabled.
(or follow [this link](https://console.cloud.google.com/flows/enableapi?apiid=datastore.googleapis.com,datastore,plus) to do so)

Get OAuth2 credentials (see how in [this page](https://cloud.google.com/java/getting-started/authenticate-users)) and edit the auth.clientID and auth.clientSecret in the pom.xml properties

Then create a `release.properties` file (located in the `WEB-INF` folder) which contains `admins={every admin's mails separated by ;}`

### Launching development server

You can launch the project with the jetty command :

```mvn clean verify jetty:run -Plocal```

Or appengine command :

```mvn clean verify appengine:run -Plocal```

Please note that, unlike App Engine, Jetty will load static files (html, css, js, etc.) directly from the source folder which allow you to do live front-end development.

You can add `-DskipTests` to skip all tests.

### Uploading to Google App Engine

Upload the project to Google App Engine with :

```mvn clean verify appengine:deploy -DprojectID={projectid}```
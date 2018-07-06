# Choices

A small maven project to test Google App Engine features.

It allow you to create question-based rooms and with the room number other people can login and respond to questions on their mobile.

## Features
* Room creation
* Google + login
* Mobile answering

Not yet implemented :
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

First, edit the auth.clientID and auth.clientSecret in the pom.xml properties

You can launch the project with the command :

```mvn -Plocal clean jetty:run -DskipTests```

and upload it to Google App Engine with :

```mvn clean appengine:deploy -DprojectID={projectid}```


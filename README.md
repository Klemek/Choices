[![Scc Count Badge](https://sloc.xyz/github/klemek/choices/?category=code)](https://github.com/boyter/scc/#badges-beta)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/Klemek/Choices.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/Klemek/Choices/context:java)
[![Language grade: JavaScript](https://img.shields.io/lgtm/grade/javascript/g/Klemek/Choices.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/Klemek/Choices/context:javascript)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/Klemek/Choices.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/Klemek/Choices/alerts/)

# Choices

A small maven project to test Google App Engine features.

It allow you to create question-based rooms and with the room number other people can login and respond to questions on their mobile.

[See API description](API.md)

## Features
* Room creation
* Google + login
* Mobile answering
* Question database / customisation

## Languages / Frameworks / Libraries

* Front :
  * HTML 5 / CSS 3 / ECMA 6
  * JQuery 3.3.1
  * Bootstrap 4.1.1
  * Bootstrap Slider 10.0.2 (+fix for BS4)
  * Font-awesome 5
  * MathJax 2.7.4
  * SheetJS
* Back :
  * Java 8
  * Google Appengine API 1.9.64
  * Google Cloud Client 0.47.0-alpha
  * JSON 20180813 (json.org)
  * Betterlists 1.4 (klemek)
  * SimpleLogger 1.3 (klemek)
  * Testing : Junit 4.12
  * Testing : Mockito 2.19.0
  * Testing : Powermock 2.0.0-beta.5

## Launching the project

### Before launch

First, in your Google App Engine project, make sure Datastores and Google+ APIs are enabled.
(or follow [this link](https://console.cloud.google.com/flows/enableapi?apiid=datastore.googleapis.com,datastore,plus) to do so)

Get OAuth2 credentials if you haven't already (see how in [this page](https://cloud.google.com/java/getting-started/authenticate-users))

Then create a `release.properties` file (located in the `src/main/java/resources` folder) which contains
```
admins={every admin's mails separated by ;}
auth.clientID={OAuth2 client ID}
auth.clientSecret={OAuth2 client secret}
mail.recipient={report mail recipient}
mail.sender={report mail sender, ex:report@yourapp.appspot.com}
mail.title={report mail title}
app.name={self explaining}
```

Don't forget to replace `app.id` and `app.version` in the `pom.xm` properties

### Launching development server

You can launch the project with the jetty command :

```mvn clean verify jetty:run -Plocal```

Or appengine command :

```mvn clean verify appengine:run -Plocal```

Please note that, unlike App Engine, Jetty will load static files (html, css, js, etc.) directly from the source folder which allow you to do live front-end development.

You can add `-DskipTests` to skip all tests.

### Uploading to Google App Engine

Upload the project to Google App Engine with :

```mvn clean verify appengine:deploy```

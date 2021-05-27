TOC
---
- [0  Introduction](#0-introduction) <br/>
- [1  Setup](#1-setup) <br/>
  * [1-a- MiniKube](#1-a-minikube) <br/>
  * [1-b- Camel-K Client](#1-b-camel-k-client) <br/>
- [2  Running An Integration Demo](#2-running-an-integration-demo) <br/>
- [3  Language Support](#3-language-support) <br/>
- [4  Developer Mode](#4-developer-mode) <br/>
- [5  A Rest API Example](#5-a-rest-api-example) <br/>
- [6  Final Words](#6-final-words) <br/>
- [7  Further Reading](#7-further-reading) <br/>

 0 Introduction
---------------

Apache Camel is an integration framework that helps us to implement Enterprise Integration Patterns easily. Kubernetes on the other hand is not about building software development, but system for automating deployment and management of containerized applications. So, when we build an integration application with using Apache Camel and then deploy it in a Kubernetes environment, some of the tasks that we do is just the same or mostly similar to any other project. We start with a boilerplate code, write scripts and setup configuration to run it on kubernetes based environment.

The thing is, using apache maven as a build tool as an example, we start adding similar dependencies to the pom file, codewise, boilerplate part is almost starts the same with any other project. We copy and paste it as a start, or copy and update it. Also, we need to setup configuration on our kubernetes platform, make an image, write config maps, do configuration/dev ops stuff. They start mostly the same. So, what happens if we just want to make a simple route from and ftp endpoint, which polls file and put it to some folder? Actually our code will just be a simple route, using quartz, polling it from an ftp endpoint, and routing the received files to the target folder, a simple cron job. If we have a need to create a dozen of simple routes, cron jobs like these, everytime we create a project, we need to copy all the boilerplate code, the deployment scripts, configurations. That's where Camel K can help. It runs natively on Kubernetes, thus, we don't need to deal with writing the boilerplate code and excessive config. We just write and roun our route like this;

```java
import org.apache.camel.builder.RouteBuilder;

public class Routing extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:java?period=1000")
            .id("java-logger")
            .setBody()
                .simple("Hello Camel K from route: ${routeId}")
            .to("log:info")      
    }

}
```

As you can see, what we have here above is just a simple route, being triggered for every second, creating a body and logging that, with INFO level. No pom or gradle file, no setup, we just need a simple route doing a simple job. With Apache Camel K, we can send this route without any boilerplate code, without any configuration directly to kubernetes.


[Go back to TOC](#toc)

 1 Setup
--------
In order to run Apache Camel K, we need two things at first;

1. Docker Desktop with Kubernetes context.
2. MiniKube (or any equivalent)
3. Camel K CLI

I assume, you can find how to setup docker desktop very easily. Then you can setup MiniKube start it and have a kubernetes dashboard. I'm going to use this [source](https://minikube.sigs.k8s.io/docs/start/) to setup minikube. After you follow the referenced source to install minikube, we can start it as follows;


[Go back to TOC](#toc)

 1-a MiniKube
-------------
1. start minikube: ```minikube start```
2. Interact with cluster: ```minikube cubectl get po -A```
3. Start minikube dashboard: ```minikube dashboard```

After you start the dashboard, a browser page should automatically pop up and you can see the kubernetes dashboard.


[Go back to TOC](#toc)

 1-b Camel-K Client
-------------------
You need to download the Apache Camel K client from the followiing source;

(source-for-apache-camel-client](https://github.com/apache/camel-k/releases)

Unzip it, copy it to a folder, and either add this folder to your path, or copy it to /usr/bin or any equivalent based on your system. This optional configuraiton is just only to call it anywhere from terminal or command line. For windows, you can add an environmental variable which has the binary, and add this environmental variable to the PATH.


[Go back to TOC](#toc)

 2 Running An Integration Demo
------------------------------
If your kubernetes dashboard runs locally, and your Camel-K client is ready then we can run a simple route example, the one that is shown in the introduction. You can check the file [here](https://github.com/bzdgn/camel-k-introduction/blob/main/sources/BasicRouting.java)

The files content is as below;

```java
import org.apache.camel.builder.RouteBuilder;

public class Routing extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:java?period=1000")
            .id("java-logger")
            .setBody()
                .simple("Hello Camel K from route: ${routeId}")
            .to("log:info")      
    }

}
```

What we are going to do is, just push it to the camel cluster, and the rest will be done automatically. To do this;

1. Enable registry addon ```minikube addons enable registry```
2. kamel install
3. kamel run BasicRouting.java

Then you can see in the log that the BasicRouting is created in the dashboard.

You can easily delete this route, by applying the following command;

```kamel delete basic-routing```


[Go back to TOC](#toc)

 3 Language Support
-------------------

Camel K supports following languages;

- Groovy
- Kotlin
- JavaScript
- Java (JShell)
- XML
- YAML

For the simplicity, the following code is written in Java DSL

```java
import org.apache.camel.builder.RouteBuilder;

public class TickerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:tick")
            .setBody()
                .simple("Hello Camel K! This written with Java DSL")
            .to("log:info")      
    }

}

```

And the xml equivalent is as below;

```xml
<routes xmlns="http://camel.apache.org/schema/spring">
    <route>
        <from uri="timer:tick"/>
        <setBody>
            <constant>Hello Camel K! This is written with XML</constant>
         </setBody>
        <to uri="log:info"/>
    </route>
</routes>

```

You can find these files under sources as below

- [Java Example](https://github.com/bzdgn/camel-k-introduction/blob/main/sources/TickerRoute.java)
- [XML Example ](https://github.com/bzdgn/camel-k-introduction/blob/main/sources/TickerRoute.xml)


[Go back to TOC](#toc)

 4 Developer Mode
-----------------
Camel K has a very nice option. You can run it on developer mode, and you can see the fast-redeploy mechanism of Camel K, as you make changes on your code.

Let's run one of the examples we have mentioned earlier, the ticker;

```java
import org.apache.camel.builder.RouteBuilder;

public class TickerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:tick")
            .setBody()
                .simple("Hello Camel K! This written with Java DSL")
            .to("log:info")      
    }

}
```

The file is located in [here](https://github.com/bzdgn/camel-k-introduction/blob/main/sources/TickerRoute.java), so to start this integration in developer mode, we should just add ```--dev``` to the end of the run command;

```kamel run TickerRoute.java --dev```

Then after some time, both ont MiniKube and in our terminal, we can see the logs in the terminal. Then we should just update our file as below;

```java
import org.apache.camel.builder.RouteBuilder;

public class TickerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:tick")
            .setBody()
                .simple("Updated: Hello Camel K! This written with Java DSL and this line is updated!")
            .to("log:info")      
    }

}
```

Soon after our change on the file, automatically the local logs will change in the log.

But also on the MiniKube (or remote Kubernetes environment), will create a new updated pod as the Camel Operator is triggered.


[Go back to TOC](#toc)

 5 A Rest API Example
---------------------
In this example, a simple REST Api will be introduced. You can find the source of this integration [here](https://github.com/bzdgn/camel-k-introduction/blob/main/sources/Api.java).

The Api has 5 operations, basic CRUD operations as listed below;

| Operation | HTTP Method | endpoint           |
|-----------|-------------|--------------------|
| Status    | GET         | /api/person/status | 
| Get       | GET         | /api/person/{id}   | 
| Get All   | GET         | /api/person/       |
| Create    | POST        | /api/person/       |
| Update    | PUT         | /api/person/       |
| Delete    | DELETE      | /api/person/{id}   |

To play with the API and see how it works, we are going to use the following cURL commands within the terminal;

```
curl localhost:8080/api/person/status
curl -H "Content-Type: application/json" -X POST -d "{\"id\":12,\"name\":\"John\"}" http://localhost:8080/api/person
curl -H "Content-Type: application/json" -X POST -d "{\"id\":15,\"name\":\"Marry\"}" http://localhost:8080/api/person
curl -H "Content-Type: application/json" -X PUT -d "{\"id\":15,\"name\":\"Jane\"}" http://localhost:8080/api/person
curl -H "Content-Type: application/json" -X DELETE http://localhost:8080/api/person/15
```

Optionally, we can to expose our service as below so that we can call our service outside the pod;

```
kubectl expose deployment api --type=LoadBalancer --name=my-service 
```

Then we can run the following command to expose it via MiniKube and then we can change the cURL commands, localhost port with the external IP;

```
minikube service api
```


[Go back to TOC](#toc)

 6 Final Words
--------------

There are pros and cons for the Camel K. 

Pros;
-----
- Fast deployment
- Removal of the boilerplate code
- Easy to write integrations
- Native kubernetes

Cons;
-----
- Not enough resources for the complex examples
- Still need to get mature for complexer scenarios
- Less control over the code
- Unit testing


[Go back to TOC](#toc)

 7 Further Reading
------------------

[Apache Camel K Documentation](https://camel.apache.org/camel-k/latest/) <br/>
[Claus Ibsen Camel K Intro](https://youtu.be/d1Hr78a7Lww?t=630) <br/>
[Camel K in a Nutshell](https://www.youtube.com/watch?v=LaBvBonUC6g) <br/>
[MiniKube Documentation](https://minikube.sigs.k8s.io/docs/start/) <br/>


[Go back to TOC](#toc)


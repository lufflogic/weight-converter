# Weight Converter 

[![Travis CI](https://api.travis-ci.org/cbm64chris/weight-converter.svg?branch=master)](https://travis-ci.org/cbm64chris/weight-converter)
![GitHub](https://img.shields.io/github/license/cbm64chris/weight-converter)

[![WorksWithOpenJDK](https://github.com/foojayio/badges/raw/main/works_with_openjdk/WorksWithOpenJDK-80.png)](https://foojay.io/works-with-openjdk)

## Introduction

A simple weight conversion application designed to run on macOS and iOS using the [Gluon](https://github.com/gluonhq/client-samples) substrate and [GraalVM](https://github.com/oracle/graal).

![IMG_A5691D24EF68-1](https://user-images.githubusercontent.com/20171342/133999088-b8c8cd1a-d7b4-4df9-833b-043aa9e1d0a1.jpeg)

## Build & Execute

Building and running requires JDK11 and can be done with a simple ```mvn clean install``` and ```mvn javafx:run```

If you wish to bake the run command into [Apache NetBeans](https://netbeans.apache.org) you can modify the the nbactions.xml to include that command;

```
<actions>
    <action>
        <actionName>run</actionName>
        <packagings>
            <packaging>jar</packaging>
        </packagings>
        <goals>
            <goal>process-classes</goal>
            <goal>org.openjfx:javafx-maven-plugin:0.0.4:run</goal>
        </goals>
        <properties>
            <exec.args>--enable-preview</exec.args>
            <exec.executable>java</exec.executable>
        </properties>
    </action>
</actions>
```

Compilation for iOS requires additional steps to provision an iOS device for installation. I recommend reading the excellent tutorial on [Gluon's own site](https://docs.gluonhq.com/client/#_overview).

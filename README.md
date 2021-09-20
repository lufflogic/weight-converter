# Weight Converter 

[![Travis CI](https://api.travis-ci.org/cbm64chris/weight-converter.svg?branch=master)](https://travis-ci.org/cbm64chris/weight-converter)
![GitHub](https://img.shields.io/github/license/cbm64chris/weight-converter)

[![WorksWithOpenJDK](https://github.com/foojayio/badges/raw/main/works_with_openjdk/WorksWithOpenJDK-80.png)](https://foojay.io/works-with-openjdk)

## Introduction

A simple weight conversion application designed to run on macOS and iOS using the [Gluon](https://github.com/gluonhq/client-samples) substrate and [GraalVM](https://github.com/oracle/graal).

![IMG_1209](https://user-images.githubusercontent.com/20171342/84010008-f679e580-a96b-11ea-904f-460e0dd31d60.png)

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

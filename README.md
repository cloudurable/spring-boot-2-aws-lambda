# Use Spring Boot 2 with AWS Lambda

* [AWS Lambda and Java Spring Boot: Getting Started January 2020](https://epsagon.com/blog/aws-lambda-and-java-spring-boot-getting-started/)
* [AWS Serverless Application Model - Developer Guide](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install-mac.html)
* [AWS Spring Boot Support](https://github.com/awslabs/aws-serverless-java-container/wiki/Quick-start---Spring-Boot2)
* [AWS Spring Boot Support Sample](https://github.com/awslabs/aws-serverless-java-container/tree/master/samples/springboot2/pet-store)

I started with [AWS Serverless Application Model - Developer Guide](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install-mac.html)
but then upgraded to Spring 2.latest using this sample: [AWS Spring Boot Support Sample](https://github.com/awslabs/aws-serverless-java-container/tree/master/samples/springboot2/pet-store).

# Changes I made 

* Moved to Spring Boot 2.2.6.RELEASE
* Moved from maven to Gradle 
* Moved From Java to Kotlin 


# Install AWS serverless development tools
```sh
brew tap aws/tap
brew install aws-sam-cli
```
Installing this `aws-sam-cli` allows you to easily test locally and to test via AWS Lambda.


## Do a clean deploy to regenerate the zip file 

```sh
./gradlew clean build 
```

# Start the sample locally
```sh
sam local start-api --template sam.yaml
```


# Ping local app (runs in docker)
```sh
curl -s http://127.0.0.1:3000/ping   
```

```                                                          
{"pong":"Hello, World!"}
```

# Create an S3 bucket to store our app deploy

```sh
aws --region us-gov-east-1 s3 mb s3://monkey-books-sample
```

Replace `monkey-books-sample` with your bucket name. 

## Do a clean deploy to regenerate the zip file 

```sh
./gradlew clean build 
```

# Create cloudformation to deploy our app

```sh
aws --region us-gov-east-1 cloudformation package --template-file sam.yaml \
--output-template-file output-sam.yaml --s3-bucket monkey-books-sample
```

Replace `us-gov-east-1` region with your region. 

```
Uploading to 29c42f3dd99ad1ba469c0990d304e915  13086985 / 13086985.0  (100.00%)
Successfully packaged artifacts and wrote output template to file output-sam.yaml.
Execute the following command to deploy the packaged template
aws cloudformation deploy --template-file /Users/richardhightower/sample/book-service/output-sam.yaml --stack-name <YOUR STACK NAME>
```

# Run cloudformation to deploy our app

```sh
aws --region us-gov-east-1 cloudformation deploy --template-file output-sam.yaml \
--stack-name bookApi --capabilities CAPABILITY_IAM

```

output

```
Waiting for changeset to be created..
Waiting for stack create/update to complete
Successfully created/updated stack - bookApi
```

# View stack just created

```sh
aws --region us-gov-east-1 cloudformation describe-stacks --stack-name bookApi            
```

```javascript
{
   "Stacks": [
       {
           "StackId": "arn:aws-us-gov:cloudformation:us-gov-east-1:387017700358:stack/bookApi/4b6f3b20-828a-11ea-bb32-0ab67c0dfb1e",
           "StackName": "bookApi",
           "ChangeSetId": "arn:aws-us-gov:cloudformation:us-gov-east-1:387017700358:changeSet/awscli-cloudformation-package-deploy-1587334106/e41fcd74-0405-43da-96a1-40249f8965e8",
           "Description": "AWS Serverless Spring Boot API - bookapp::book-service",
           "CreationTime": "2020-04-19T22:08:27.113Z",
           "LastUpdatedTime": "2020-04-19T22:08:32.617Z",
           "RollbackConfiguration": {},
           "StackStatus": "CREATE_COMPLETE",
           "DisableRollback": false,
           "NotificationARNs": [],
           "Capabilities": [
               "CAPABILITY_IAM"
           ],
           "Outputs": [
               {
                   "OutputKey": "BookServiceApi",
                   "OutputValue": "https://j2hbm6p413.execute-api.us-gov-east-1.amazonaws.com/Prod/ping",
                   "Description": "URL for application",
                   "ExportName": "BookServiceApi"
               }
           ],
           "Tags": [],
           "EnableTerminationProtection": false,
           "DriftInformation": {
               "StackDriftStatus": "NOT_CHECKED"
           }
       }
   ]
}
```

# Test lambda function 

```sh
curl https://j2hbm6p413.execute-api.us-gov-east-1.amazonaws.com/Prod/ping
```

output

```javascript
{"pong":"Hello, World!"}
```


## If you have jq, then you can do this 

```sh

 export END_POINT=`aws --region us-gov-east-1 cloudformation describe-stacks --stack-name bookApi  | jq -r ".Stacks[0].Outputs[0].OutputValue"`
 curl  $END_POINT                                                                                                                              

```
## Todo 

* Automate deploy to AWS 

              
# Older notes. 
____


Don't do this one anymore. It brings an older version of the project (Spring 1.5). 

# Create maven project
```sh
mvn archetype:generate -DgroupId=bookapp -DartifactId=book-service \
-Dversion=1.0-SNAPSHOT -DarchetypeGroupId=com.amazonaws.serverless.archetypes \
-DarchetypeArtifactId=aws-serverless-springboot-archetype  -DarchetypeVersion=1.4

cd bookservice
```

The above also creates a gradle build.


## Moved to Kotlin from Java 

The major change here other than the right kotlin libs was copying the kotlin files
from Kotlin 

```groovy

task buildZip(type: Zip) {
  from compileJava
  from processResources
  into('lib') {
    from(configurations.compileClasspath) {
      exclude 'tomcat-embed-*'
    }
  }
  into('.') {
    from("build/classes/kotlin/main") {
      include '**/**'
    }
  }
}


```

And, then triggering that task after the build 

```groovy 
build.finalizedBy(buildZip)

``` 

Now it works just like it did for Java. 

```groovy
buildscript {
  ext.kotlin_version = '1.3.72'

  ext.spring_boot_version = '2.2.6.RELEASE'
  repositories {
    jcenter()
    mavenLocal()
    mavenCentral()
  }
  dependencies {
    classpath "org.springframework.boot:spring-boot-gradle-plugin:$spring_boot_version"
    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    classpath "org.jetbrains.kotlin:kotlin-allopen:$kotlin_version" // See https://kotlinlang.org/docs/reference/compiler-plugins.html#spring-support
  }
}

version = '1.0.0'

apply plugin: 'org.springframework.boot'
apply plugin: 'kotlin'
apply plugin: 'io.spring.dependency-management'
apply plugin: "kotlin-spring" // https://kotlinlang.org/docs/reference/compiler-plugins.html#spring-support
apply plugin: 'java'

//apply plugin: 'java-library-distribution'


sourceCompatibility = 1.8
targetCompatibility = 1.8

repositories {
  jcenter()
  mavenLocal()
  mavenCentral()
}

dependencies {


  testCompile "org.springframework.boot:spring-boot-starter-test"
  annotationProcessor "org.springframework.boot:spring-boot-configuration-processor"

  compile (
          implementation('org.springframework.boot:spring-boot-starter-web:2.2.6.RELEASE') {
            //exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
          },
          "com.amazonaws.serverless:aws-serverless-java-container-springboot2:1.5",
          'io.symphonia:lambda-logging:1.0.1'
  )
  testCompile("junit:junit")
  implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
  runtime "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"
  testCompile "org.jetbrains.kotlin:kotlin-test:$kotlin_version"
  compile "com.fasterxml.jackson.module:jackson-module-kotlin"
}

task buildZip(type: Zip) {
  from compileJava
  from processResources
  into('lib') {
    from(configurations.compileClasspath) {
      exclude 'tomcat-embed-*'
    }
  }
  into('.') {
    from("build/classes/kotlin/main") {
      include '**/**'
    }
  }
}


compileKotlin {
  kotlinOptions {
    jvmTarget = "1.8"
  }
}
compileTestKotlin {
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

build.finalizedBy(buildZip)

//distributions {
//  main {
//    distributionBaseName = 'book-service'
//  }
//}



```

## Upgraded to Gradle and got rid of maven.

Moved to Spring 2.x. 
I had to get rid of the exception handler that was configured in `Application.java.` 

```groovy
buildscript {

  ext.spring_boot_version = '2.2.6.RELEASE'
  repositories {
    jcenter()
    mavenLocal()
    mavenCentral()
  }
  dependencies {
    classpath "org.springframework.boot:spring-boot-gradle-plugin:$spring_boot_version"
  }
}

version = '1.0.0'

apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'
apply plugin: 'java'

sourceCompatibility = 1.8
targetCompatibility = 1.8

repositories {
  jcenter()
  mavenLocal()
  mavenCentral()
}

dependencies {


  testCompile "org.springframework.boot:spring-boot-starter-test"
  annotationProcessor "org.springframework.boot:spring-boot-configuration-processor"

  compile (
          implementation('org.springframework.boot:spring-boot-starter-web:2.2.6.RELEASE') {
            exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
          },
          "com.amazonaws.serverless:aws-serverless-java-container-springboot2:1.5",
          'io.symphonia:lambda-logging:1.0.1'
  )
  testCompile("junit:junit")
}

task buildZip(type: Zip) {
  from compileJava
  from processResources
  into('lib') {
    from(configurations.compileClasspath) {
      exclude 'tomcat-embed-*'
    }
  }
}

build.dependsOn buildZip

```
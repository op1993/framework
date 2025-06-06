# API Test Automation Framework

A comprehensive Java-based test automation framework for REST API testing using TestNG, RestAssured, and Allure reporting.

## Prerequisites

Make sure you have the following installed on your system:
Required Software:
1. Java 24
2. Allure
3. Git

## Verify Installation

java --version
allure --version
git --version

## Default configuration

Default configuration is in **_src/main/resources/configuration/automation-application.yml_**


## Override Configuration from command line 
Frameworks allows to update any configuration on the fly. Populate parameter using full variable path.

./gradlew clean test -Dexecution.retry=5 -Dapplication.baseApi=https://google.com -DthreadCount=10

## Run tests without report
**windows:**

./gradlew clean test -Dgroups=
## Run tests with allure report
**windows:** 

./gradlew clean test; ./gradlew allureReport; ./gradlew allureServe
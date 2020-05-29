# Ktor flyway feature
[![Build Status](https://travis-ci.org/viartemev/ktor-flyway-feature.svg?branch=master)](https://travis-ci.org/viartemev/ktor-flyway-feature)
[ ![Download](https://api.bintray.com/packages/viartemev/Maven/ktor-flyway-feature/images/download.svg) ](https://bintray.com/viartemev/Maven/ktor-flyway-feature/_latestVersion)

Ktor feature for version control for your database by Flyway.

## Usage
<details><summary>Set up in Gradle:</summary>

```groovy
repositories {
    jcenter()
}

dependencies {
    implementation("com.viartemev:ktor-flyway-feature:$ktor_flyway_feature_version")
}
```
</details>

<details><summary>Set up in Maven:</summary>

```xml
<repositories>
    <repository>
        <id>jcenter</id>
        <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>

<dependency>
  <groupId>com.viartemev</groupId>
  <artifactId>ktor-flyway-feature</artifactId>
  <version>${ktor_flyway_feature_version}</version>
</dependency>
```
</details>

Add the feature to the code:
```kotlin
install(FlywayFeature) {
    dataSource = database.connectionPool //required
    location = "custom/dir" //optional, default value = "db/migration"
    commands(Info, Migrate) //optional, default command list is: Info, Migrate
    schemas = arrayOf("CUSTOM_SCHEMA_1", "CUSTOM_SCHEMA_2") // optional, default value is the DB product specific default schema
}
```

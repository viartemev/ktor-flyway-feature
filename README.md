# Ktor flyway feature
[ ![Download](https://api.bintray.com/packages/viartemev/Maven/ktor-flyway-feature/images/download.svg) ](https://bintray.com/viartemev/Maven/ktor-flyway-feature/_latestVersion)

Ktor feature for version control for your database by Flyway.
## Usage
Set up in Gradle:
```groovy
repositories {
    jcenter()
}
dependencies {
    implementation("com.viartemev:ktor-flyway-feature:$ktor_flyway_feature_version")
}
```
Set up in Maven:
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
Add the feature to the code:
```kotlin
install(Flyway) {
    dataSource = database.connectionPool //required
    location = "custom/dir" //optional, default value = "db/migration"
    commands(Info, Migrate) //optional, default command list is: Info, Migrate
}
```

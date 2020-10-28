# OpenEHR REST tests

## Configuring maven project to run tests from openehr-rest-tests dependency

1. Add maven dependencies to pom file:

```      
<properties>
    <openehr-rest-tests.version>1.0-SNAPSHOT</openehr-rest-tests.version>
</properties>
                             
<dependencies>
    <dependency>
        <groupId>care.better.platform.test</groupId>
        <artifactId>openehr-rest-tests</artifactId>
        <version>${openehr-rest-tests.version}</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>care.better.platform.test</groupId>
        <artifactId>openehr-rest-tests</artifactId>
        <version>${openehr-rest-tests.version}</version>
        <classifier>tests</classifier>
        <type>test-jar</type>
        <scope>test</scope>
    </dependency>
</dependencies>
```

2. Configure maven-surefire-plugin

```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>${maven-surefire-plugin.version}</version>
    <configuration>
        <dependenciesToScan>
            <dependency>care.better.platform.test:openehr-rest-tests</dependency>
        </dependenciesToScan>
    </configuration>
</plugin>
```

3. Run the tests

```
mvn clean test
```


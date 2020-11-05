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

3. Configure EHR server parameters in `application-test.properties`:

| Property key    | Description |
| --------------- | ----------- |
| `openehr.rest.uri`  | REST endpoint of OpenEHR server |
| `auth.basic.username`  | Username for basic auth. If not set, there is no authentication |
| `auth.basic.password`  | Password for basic auth |
| `openehr.conformance`  | Settings for conformance OPTIONS [call](https://specifications.openehr.org/releases/ITS-REST/Release-1.0.0/ehr.html#design-considerations-options-and-conformance) |

4. Run the tests

```
mvn clean test
```


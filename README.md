java-client
===========

Idibon API Java SDK

Requires a JDK (JDK 6 should work, but only tested on JDK 7) and
[Maven 3](http://maven.apache.org/download.cgi).

The SDK is structured as a multimodule Maven project, with the core
API client as

```
<groupId>com.idibon.api.java-sdk</groupId>
<artifactId>java-api-client</artifactId>
<version>0.1.0-SNAPSHOT</version>
```

Example apps are located in the `examples/` subdirectory.

* To build the API client and all of the example apps, run `mvn package`
from the top-level directory.

* To install the API client JAR in your local maven repository, run
  `mvn install` from the `java-api-client/` directory.

* To generate JavaDocs for the API client, run `mvn javadoc` from the
  `java-api-client/` directory.

## Example Apps

Name|Description
--------|--------
[list-documents](#list-documents)|Lists all of the document names in a collection

### <a name="list-documents">list-documents Example App</a>

To run

```
cd examples/list-documents/target
java -cp list-documents-0.1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.idibon.ListDocuments $API_KEY $COLLECTION
```

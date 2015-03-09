java-client
===========

Idibon API Java SDK

Requires JDK 7 or newer, and [Maven 3](http://maven.apache.org/download.cgi).

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

* To generate JavaDocs for the API client, run `mvn javadoc:javadoc` from
  the `java-api-client/` directory.

## Example Apps

Name|Description
--------|--------
[list-documents](#list-documents)|Lists all of the document names in a collection
[upload-json-documents](#upload-json-documents)|Upload one or more JSON documents to a collection

### <a name="list-documents">list-documents Example App</a>

To run

```
cd examples/list-documents/target
java -cp list-documents-0.1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.idibon.ListDocuments $API_KEY $COLLECTION
```

### <a name="upload-json-documents">upload-json-documents Example App</a>

To run
```
cd examples/upload-json-documents/target
java -cp upload-json-documents-0.1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.idibon.UploadJsonDocuments $API_KEY $COLLECTION files.json...
```

JSON files should have the following structure:

```
{
  "content":$CONTENT,
  "metadata":$METADATA,
  "name":$NAME
}
```

* `$CONTENT` should be a String, and include the document content (e.g., e-mail, tweet, or SMS message text).

* `$METADATA` should be a JSON object of metadata for the document. Any metadata may be submitted, there are no restrictions or expectations about the data that is included. If no metadata is needed, this key can be excluded.

* `$NAME` should be a String. It is optional; if present, the document will be created using the provided name.

Idibon Java SDK
===========

Requires JDK 7 or newer, and [Maven 3](http://maven.apache.org/download.cgi). Some example applications require JDK 8.

The SDK is structured as a multimodule Maven project, with the core
API client as

```
<groupId>com.idibon.api.java-sdk</groupId>
<artifactId>java-api-client</artifactId>
<version>1.0.2-SNAPSHOT</version>
```

Example apps are located in the `examples/` subdirectory.

* To build the API client and all of the example apps, run `mvn package`
from the top-level directory.

* To install the API client JAR in your local maven repository, run
  `mvn install` from the top-level directory.

* To generate JavaDocs for the API client, run `mvn javadoc:javadoc` from
  the `java-api-client/` directory.

## Example Apps

Name|Description
--------|--------
[list-documents](#list-documents)|Lists all of the document names in a collection
[upload-json-documents](#upload-json-documents)|Upload one or more JSON documents to a collection
[annotate-document](#annotate-document)|Add an assignment annotation for a document-scope task to a document
[predict-content](#predict-content)|Generate API classifications for text on the command line
[predict-idibon-public](#predict-idibon-public)|Generate API classifications using an Idibon Public server
[show-updated-tasks](#show-updated-tasks)|List all tasks in a collection updated since a specific time (JDK 8)
[print-ontology](#print-ontology)|Prints a collection ontology, or branch of an ontology, to the console.

### <a name="list-documents">list-documents Example App</a>

To run

```
cd examples/list-documents/target
java -cp list-documents-$SDK_VERSION-jar-with-dependencies.jar \
  com.idibon.ListDocuments $API_KEY $COLLECTION
```

### <a name="upload-json-documents">upload-json-documents Example App</a>

To run
```
cd examples/upload-json-documents/target
java -cp upload-json-documents-$SDK_VERSION-jar-with-dependencies.jar \
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

### <a name="annotate-document">annotate-document Example App</a>

To run
```
cd examples/annotate-document/target
java -cp annotate-document-$SDK_VERSION.jar-with-dependencies.jar \
  com.idibon.AnnotateDocument $API_KEY $COLLECTION $DOCUMENT $TASK $LABEL
```

* `$DOCUMENT` should be the name of a document inside `$COLLECTION`

* `$TASK` should be the name of a document-scope task inside `$COLLECTION`

* `$LABEL` should be the name of a label within `$TASK`

### <a name="predict-content">predict-content Example App</a>

This example app demonstrates the Idibon API's streaming predictive
classification capabilities by taking an arbitrary string of text
provided on the command line and generating a prediction against a
user-defined task. The full classification results, including
confidences for each label and significant features, are printed
to the console.

To run
```
cd examples/predict-content/target
java -cp predict-content-$SDK_VERSION-jar-with-dependencies.jar \
  com.idibon.PredictContent $API_KEY $COLLECTION $TASK "Some content..."
```

* `$TASK` should be the name of a document-scope task inside `$COLLECTION`

* `Some content` should be whatever text you want to predict. It may be provided in quotes or unquoted.

### <a name="predict-idibon-public">predict-idibon-public Example App</a>

This example is based off of [predict-content](predict-content), but uses
an Idibon Public server running on the local system rather than the Idibon
enterprise service.


To run
```
cd examples/predict-idibon-public/target
java -cp predict-idibon-public-$SDK_VERSION-jar-with-dependencies.jar \
  com.idibon.PredictIdibonPublic $COLLECTION $TASK "Some content..."
```

* `$TASK` should be the name of a document-scope task inside `$COLLECTION`

* `Some content` should be whatever text you want to predict. It may be provided in quotes or unquoted.

### <a name="show-updated-tasks">show-updated-tasks Example App (JDK 8)</a>

This example app demonstrates how to filter tasks using JDK 8 streams and
lambdas based on task metadata properties; in this case, the property is
the task's `updated_at` property.

To run
```
cd examples/show-updated-tasks/target
java -cp show-updated-tasks-$SDK_VERSION-jar-with-dependencies.jar \
  com.idibon.ShowUpdatedTasks $API_KEY $COLLECTION ISO-8601-Date
```

* `ISO-8601-Date` should be an ISO-8601 date string (`YYYY-mm-dd'T'HH:mm:ss'Z'`);
tasks last updated before this time will not be reported.

### <a name="print-ontology">print-ontology Example App</a>

This example app prints an ontology, or a branch of an ontology, using a tree view
style visualization to the console. It demonstrates using the task and sub-task
navigation features of the SDK.

To run
```
cd examples/print-ontology/target
java -cp print-ontology-$SDK_VERSION-jar-with-dependencies.jar \
  com.idibon.PrintOntology $API_KEY $COLLECTION [$TASK]
```

* `$TASK` is an optional parameter, restricting the print out to just the ontology
branch starting at the named task.

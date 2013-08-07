package com.idibon.api.client;

import java.util.*;

public class TestAPI {

	public static void main(String[] args) throws Exception {

		Client client = new Client();

		// Test put collection
		System.out.println("****** Test put collection ******");
		Collection collection = new Collection();
		collection.setName("TestPutCollection");
		collection.setDescription("Testing on put");
		Collection c = client.putCollection("TestPutCollection", collection);
		System.out
				.println("Collection creation timestamp: " + c.getCreatedAt());
		System.out
				.println("Collection updation timestamp: " + c.getUpdatedAt());
		System.out.println("Collection name: " + c.getName());
		System.out.println("Collection description: " + c.getDescription());
		System.out.println();

		collection = new Collection();
		collection.setName("NewCollection3");
		collection.setDescription("Testing on PUT");
		c = client.putCollection("NewCollection3", collection);
		System.out.println();

		// Test get collections
		System.out.println("****** Test get collections ******");
		ArrayList<Collection> collections = client.getCollections();
		for (Collection col : collections) {
			System.out.println("NAME: " + col.getName());
			System.out.println("DESCRIPTION: " + col.getDescription());
		}
		System.out.println();

		// Test get collection
		System.out.println("****** Test get collection ******");
		Collection collection1 = client.getCollection("TestingCollection");
		System.out.println("Collection Name: " + collection1.getName());
		System.out.println("Collection Description: "
				+ collection1.getDescription());
		System.out.println("Collection creation timestamp: "
				+ collection1.getCreatedAt());
		System.out.println();

		// Test post collection
		System.out.println("****** Test post collection ******");
		collection = new Collection();
		collection.setName("TestingCollection");
		collection.setDescription("Testing on POST");
		c = client.postCollection("TestingCollection", collection);
		System.out
				.println("Collection creation timestamp: " + c.getCreatedAt());
		System.out
				.println("Collection updation timestamp: " + c.getUpdatedAt());
		System.out.println("Collection name: " + c.getName());
		System.out.println("Collection description: " + c.getDescription());
		System.out.println();

		// Test delete collection
		System.out.println("****** Test delete collection ******");
		DeletedObject delobj = client.deleteCollection("NewCollection3");
		System.out.println("Deleted collection: " + delobj.getName());
		System.out.println("Deleted? : " + delobj.getIsDeleted());
		System.out.println();

		// Test flush collection
		System.out.println("****** Test flush collection ******");
		String collectionid;
		collectionid = client.flushCollection("TestingCollection");
		System.out.println("Flushed Collection Id: " + collectionid);
		System.out.println();

		// Test get document
		System.out.println("****** Test get document ******");
		Document doc = client
				.getDocument("muc7_ner_ashlesha", "nyt960511.0137");
		// Document doc = client.getDocument("AuthorsData", "LewisCaroll");
		System.out.println("Document name: " + doc.getName());
		System.out.println("Document updation date: " + doc.getUpdatedAt());
		System.out.println("Document Annotation #2: "
				+ doc.getAnnotations().get(1).getText1());
		System.out.println();

		// Test put document
		System.out.println("****** Test put document ******");
		Document d = new Document();
		d.setName("JaneAusten");
		d.setContent("Emma and Charlotte");
		Document res = client.putDocument("AuthorsData", "JaneAusten", d);
		System.out.println("Doc name " + res.getName());
		System.out.println("Doc content " + res.getContent());
		System.out.println();

		d = new Document();
		d.setName("JonathanSwift");
		d.setContent("Gulliver Travels");
		res = client.putDocument("AuthorsData", "JonathanSwift", d);
		System.out.println("Doc name " + res.getName());
		System.out.println("Doc content " + res.getContent());
		System.out.println();

		// Test get documents
		System.out.println("****** Test get documents ******");
		DocumentSearchResponse docs = new DocumentSearchResponse();
		DocumentSearchRequest request = new DocumentSearchRequest();
		request.setCount(25);
		docs = client.getDocuments("AuthorsData", request);
		for (DocumentReference ds : docs.getDocumentsAfterSearch()) {
			System.out.println(ds.getName() + " ------ " + ds.getContent());
		}
		System.out.println("Docs count: " + docs.getCount());
		System.out.println("Total docs: " + docs.getTotal());
		System.out.println("Sorting order: " + docs.getOrder());
		System.out.println();

		// Test get documents full
		System.out.println("****** Test get documents full ******");
		FullDocumentSearchResponse resp = new FullDocumentSearchResponse();
		request = new DocumentSearchRequest();
		request.setCount(10);
		resp = client.getFullDocuments("AuthorsData", request);
		for (Document ds : resp.getDocumentsAfterSearch()) {
			System.out.println(ds.getName() + " -- " + ds.getContent() + " -- "
					+ ds.getUuid() + " -- " + ds.getCreatedAt());
		}
		System.out.println("Docs count: " + resp.getCount());
		System.out.println("Total docs: " + resp.getTotal());
		System.out.println("Sorting order: " + resp.getOrder());
		System.out.println();

		// Test iterate over documents
		System.out.println("****** Test document iterator ******");
		request = new DocumentSearchRequest();
		Iterator<DocumentReference> iter = client.documentIterator(
				"AuthorsData", request);
		int i = 0;
		DocumentReference dr;
		// while ((dr = iter.next()) != null){
		while (iter.hasNext()) {
			dr = iter.next();
			i++;
			System.out.println("Name: " + dr.getName() + " Content: "
					+ dr.getContent());
		}
		System.out.println("Total docs: " + i);
		System.out.println();

		// Test iterate over full documents
		System.out.println("****** Test full document iterator ******");
		request = new DocumentSearchRequest();
		Iterator<Document> it = client.fullDocumentIterator("AuthorsData",
				request);
		i = 0;
		// while ((d = iter.next()) != null){
		while (it.hasNext()) {
			d = it.next();
			i++;
			System.out.println("Name: " + d.getName() + " Content: "
					+ d.getContent() + " UUID: " + d.getUuid()
					+ " Created_at: " + d.getCreatedAt());
		}
		System.out.println("Total docs: " + i);
		System.out.println();

		// Test delete document
		System.out.println("****** Test delete document ******");
		DeletedObject dd = client.deleteDocument("AuthorsData", "JaneAusten");
		System.out.println("Deleted document name: " + dd.getName());
		System.out.println("Deleted? : " + dd.getIsDeleted());
		System.out.println();

		// Testing post documents
		System.out.println("****** Test post document ******");
		ArrayList<Document> list = new ArrayList<Document>();
		d = new Document();
		d.setName("JaneAustenNew");
		d.setContent("Emma");
		list.add(d);
		d = new Document();
		d.setName("LewisCaroll");
		d.setContent("Alice in Wonderland");
		list.add(d);
		ArrayList<Document> result = client.postDocuments("AuthorsData", list);
		for (Document document : result) {
			System.out.println("Doc name " + document.getName());
			System.out.println("Doc content " + document.getContent());
		}
		System.out.println();

		// Test put task
		System.out.println("****** Test put task ******");
		Task t = new Task();
		t.setName("NER");
		t.setDescription("Named entities in the documents");
		Task task = client.putTask("AuthorsData", "NER", t);
		System.out.println("Put Task name: " + task.getName());
		System.out.println("Put Task description: " + task.getDescription());
		System.out.println("Put Task creation date: " + task.getCreatedAt());
		System.out.println();

		t = new Task();
		t.setName("TopicClassification");
		t.setDescription("Topics in the documents");
		task = client.putTask("AuthorsData", "TopicClassification", t);
		System.out.println("Put Task name: " + task.getName());
		System.out.println("Put Task description: " + task.getDescription());
		System.out.println("Put Task creation date: " + task.getCreatedAt());
		System.out.println();

		// Test post task
		System.out.println("****** Test post task ******");
		t = new Task();
		t.setName("NER");
		t.setDescription("Named entities");
		task = client.postTask("AuthorsData", "NER", t);
		System.out.println("Post Task name: " + task.getName());
		System.out.println("Post Task description: " + task.getDescription());
		System.out.println("Post Task creation date: " + task.getCreatedAt());
		System.out.println();

		// Test post labels
		System.out.println("****** Test post labels ******");
		ArrayList<Label> labels = new ArrayList<Label>();
		Label l = new Label();
		l.setName("Person");
		l.setDescription("Person label");
		labels.add(l);
		l = new Label();
		l.setName("Location");
		labels.add(l);
		task = client.postLabels("AuthorsData", "NER", labels);
		System.out.println("Post Label Task name: " + task.getName());
		System.out.println("Post Label Get Task description: "
				+ task.getDescription());
		System.out.println("Post Label Get Task creation date: "
				+ task.getCreatedAt());
		System.out.println();

		// Test get task
		System.out.println("****** Test get task ******");
		task = client.getTask("AuthorsData", "TopicClassification");
		System.out.println("Get Task name: " + task.getName());
		System.out.println("Get Task description: " + task.getDescription());
		System.out.println("Get Task creation date: " + task.getCreatedAt());
		System.out.println();

		// Test delete labels
		System.out.println("****** Test delete labels ******");
		DeletedObject dobj = client.deleteLabel("AuthorsData", "NER",
				"Location");
		System.out.println("Deleted Label name: " + dobj.getName());
		System.out.println("Deleted Label: " + dobj.getIsDeleted());
		System.out.println();

		// Test delete task
		System.out.println("****** Test delete task ******");
		dobj = client.deleteTask("AuthorsData", "TopicClassification");
		System.out.println("Deleted Task name: " + dobj.getName());
		System.out.println("Deleted Task: " + dobj.getIsDeleted());
		System.out.println();

		// Test existing annotations
		System.out.println("****** Test existing annotations ******");
		doc = client.getDocument("AuthorsData", "JaneAustenNew");
		System.out.println(doc.getName());
		for (Annotation an : doc.getAnnotations()) {
			System.out.println("Annotation: " + an.getLabel().getName() + " : "
					+ an.getTask().getName());
		}
		System.out.println();

		// Test post annotations
		System.out.println("****** Test post annotations ******");
		ArrayList<Annotation> an_list = new ArrayList<Annotation>();
		Annotation a = new Annotation();
		Label label = new Label();
		label.setName("Location");
		a.setLabel(label);
		task = new Task();
		task.setName("NER");
		a.setTask(task);
		an_list.add(a);

		a = new Annotation();
		label = new Label();
		label.setName("Person");
		a.setLabel(label);
		task = new Task();
		task.setName("NER");
		a.setTask(task);
		an_list.add(a);
		d = new Document();
		d = client.postAnnotations("AuthorsData", "JaneAustenNew", an_list);
		System.out.println("Post Annotation to Doc: " + d.getName());
		for (Annotation an : d.getAnnotations()) {
			System.out.println("Annotation: " + an.getLabel().getName() + " : "
					+ an.getTask().getName() + " : " + an.getUuid());
		}
		System.out.println();

		// Test delete annotation
		System.out.println("****** Test delete annotation ******");
		String uuid = client.getDocument("AuthorsData", "JaneAustenNew")
				.getAnnotations().get(1).getUuid();
		DeletedObject obj = client.deleteAnnotation("AuthorsData",
				"JaneAustenNew", uuid);
		System.out.println(obj.getName());
		System.out.println(obj.getIsDeleted());
		System.out.println();

		// Test existing annotations
		System.out.println("****** Test existing annotations ******");
		doc = client.getDocument("AuthorsData", "JaneAustenNew");
		System.out.println(doc.getName());
		for (Annotation an : doc.getAnnotations()) {
			System.out.println("Annotation: " + an.getLabel().getName() + " : "
					+ an.getTask().getName() + " : " + an.getUuid());
		}
		System.out.println();

		// Test get features
		System.out.println("****** Test get features ******");
		HashMap<String, Feature> featuremap = new HashMap<String, Feature>();
		// featuremap = client.getFeatures();

		featuremap = client.getFeatures();
		System.out.println("Total features: " + featuremap.size());
		System.out.println("Features: " + featuremap.keySet());
		System.out.println("Edgecount: "
				+ featuremap.get("RelationBetweenWords").getParameters()
						.get("edgecount").getDefault());
		System.out.println("Get attributes: "
				+ featuremap.get("FKReadingLevel").getParameters().get("round")
						.getType());
		System.out.println();

		// Test post features
		System.out.println("****** Test post features ******");
		ArrayList<HashMap<String, Object>> array = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", "Regex");
		HashMap<String, Integer> m = new HashMap<String, Integer>();
		m.put("offset", 3);
		map.put("parameters", m);
		array.add(map);
		Task newtask = new Task();
		newtask = client.postFeatures("AuthorsData", "NER", array);
		System.out.println("Feature for " + newtask.getName() + " : "
				+ newtask.getFeatures().toString());
		System.out.println();

		array = new ArrayList<HashMap<String, Object>>();
		map = new HashMap<String, Object>();
		map.put("name", "CharNGrams");
		m = new HashMap<String, Integer>();
		m.put("min", 2);
		m.put("max", 7);
		map.put("parameters", m);
		array.add(map);
		newtask = new Task();
		newtask = client.postFeatures("AuthorsData", "NER", array);
		System.out.println("Feature for " + newtask.getName() + " : "
				+ newtask.getFeatures().toString());
		System.out.println();

		// Test delete features
		System.out.println("****** Test delete features ******");
		map = new HashMap<String, Object>();
		map.put("name", "CharNGrams");
		m = new HashMap<String, Integer>();
		m.put("min", 2);
		m.put("max", 7);
		map.put("parameters", m);
		dobj = client.deleteFeature("AuthorsData", "NER", map);
		System.out.println("Deleted Feature: " + dobj.getName());
		System.out.println("Deleted? : " + dobj.getIsDeleted());
		System.out.println();

		// Test get predictions for raw text
		System.out.println("****** Test get predictions for raw text ******");
		map = new HashMap<String, Object>();
		map.put("content", "Book, England, Paris, Radar Co., John");
		ArrayList<HashMap<String, Object>> predictions = client
				.getPredictionsForText("muc7_ner_ashlesha", "NER", map);
		for (HashMap<String, Object> hm : predictions) {
			System.out.println("Class: " + hm.get("class") + " Text: "
					+ hm.get("text") + hm.toString());
		}
		System.out.println();

		// Test get predictions for document
		System.out.println("****** Test get predictions for document ******");
		predictions = client.getPredictionsForDocument("muc7_ner_ashlesha",
				"NER", "nyt960511.0137");
		for (HashMap<String, Object> hm : predictions) {
			System.out.println("Class: " + hm.get("class") + " Text: "
					+ hm.get("text") + hm.toString());
		}
		System.out.println();

		System.out.println("Done!");
	}
}

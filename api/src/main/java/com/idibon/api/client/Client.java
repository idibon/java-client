package com.idibon.api.client;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import com.google.gson.*;

public class Client {

	private String apiKey = "";
	private String apiPwd = "";
	private String apiUrl = "dev-api.idibon.com";
	private Gson gson = new Gson();
	private String url;
	private String method;
	private HttpClient client;
	private int port;

	public Client() {
		this(System.getenv().get("IDIBON_API_KEY"), "dev-api.idibon.com", 443,
				new DefaultHttpClient());
	}

	public Client(String apiKey) {
		this(apiKey, "dev-api.idibon.com", 443, new DefaultHttpClient());
	}

	public Client(String apiKey, String host, int port) {
		this(apiKey, host, port, new DefaultHttpClient());
	}

	public Client(String host, int port) {
		this(System.getenv().get("IDIBON_API_KEY"), host, port,
				new DefaultHttpClient());
	}

	public Client(String apiKey, String host, int port, HttpClient client) {
		this.apiKey = apiKey;
		this.apiUrl = host;
		this.port = port;
		this.client = client;
	}

	protected JsonObject request(String apiurl, String method,
			Object requestBody) throws Exception {
		JsonParser parser = new JsonParser();
		StringBuffer output = new StringBuffer();
		HttpEntityEnclosingRequestBase request = null;

		// System.out.println(apiurl);
		URI url = getURI(apiurl);
		request = getServerConnection(method, requestBody, url);

		try {
			HttpResponse response = getResponse(request);
			InputStream content = response.getEntity().getContent();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					content));
			String line;
			while ((line = in.readLine()) != null) {
				output.append(line);
				output.append('\r');
			}
			in.close();
		} finally {
			request.releaseConnection();
		}
		if (output.toString().startsWith("[")) {
			output.insert(0, "{\"predictions\" : ");
			output.insert(output.length(), "}");
		}
		if (parser.parse(output.toString()).getAsJsonObject().get("errors") != null) {
			if (parser.parse(output.toString()).getAsJsonObject().get("errors")
					.getAsJsonObject().get("content") != null) {
				throw new IllegalStateException(parser.parse(output.toString())
						.getAsJsonObject().get("errors").getAsJsonObject()
						.get("content").getAsString());
			} else {
				throw new IllegalStateException(parser.parse(output.toString())
						.getAsJsonObject().get("errors").getAsString());
			}
		}
		// System.out.println(body);
		// System.out.println(output.toString());
		return parser.parse(output.toString()).getAsJsonObject();
	}

	protected HttpEntityEnclosingRequestBase getServerConnection(String method,
			Object requestBody, URI url) throws UnsupportedEncodingException {
		HttpEntityEnclosingRequestBase request = null;
		String body = gson.toJson(requestBody).toString();
		StringEntity entity = new StringEntity(body, "UTF-8");
		entity.setContentType("application/json");
		@SuppressWarnings("restriction")
		String encoding = javax.xml.bind.DatatypeConverter
				.printBase64Binary((this.apiKey + ":" + this.apiPwd).getBytes());
		if (method == "GET") {
			request = new HttpGetWithEntity();
		} else if (method == "POST") {
			request = new HttpPost();
		} else if (method == "PUT") {
			request = new HttpPut();
		} else if (method == "DELETE") {
			request = new HttpDeleteWithEntity();
		}

		request.setHeader("Authorization", "Basic " + encoding);
		request.addHeader("Accept", "application/json");
		request.addHeader("Content-Type", "application/json");
		request.setEntity(entity);
		request.setURI(url);
		return request;
	}

	protected HttpResponse getResponse(HttpEntityEnclosingRequestBase request)
			throws ClientProtocolException, IOException {
		HttpResponse response = client.execute(request);
		return response;
	}

	protected URI getURI(String apiurl) throws URISyntaxException {
		URI url = new URI("https", null, this.apiUrl, this.port, apiurl, "", "");
		return url;
	}

	/******* COLLECTIONS ********/
	public ArrayList<Collection> getCollections() throws Exception {
		url = "/" + "";
		method = "GET";
		Object requestBody = new Object();
		ArrayList<Collection> setofcollections = new ArrayList<Collection>();
		JsonObject response = this.request(url, method, requestBody);
		JsonArray collections = response.getAsJsonArray("collections");
		for (JsonElement j : collections) {
			Collection c = gson.fromJson(j, Collection.class);
			setofcollections.add(c);
		}
		return setofcollections;
	}

	public Collection getCollection(String collectionname) throws Exception {
		url = "/" + collectionname;
		method = "GET";
		Collection c;
		Object requestBody = new Object();
		JsonObject response = this.request(url, method, requestBody);
		JsonObject collection = response.getAsJsonObject("collection");
		c = gson.fromJson(collection, Collection.class);
		return c;
	}

	public Collection putCollection(String collectionname, Collection collection)
			throws Exception {
		url = "/" + collectionname;
		method = "PUT";
		Collection c;

		HashMap<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("collection", (Object) collection);

		JsonObject response = this.request(url, method, requestBody);
		JsonObject obj = response.getAsJsonObject("collection");
		c = gson.fromJson(obj, Collection.class);
		return c;
	}

	public Collection postCollection(String collectionname,
			Collection collection) throws Exception {
		// Collection oldcollection = this.getCollection(collectionname);
		url = "/" + collectionname;
		method = "POST";
		Collection c;

		HashMap<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("collection", (Object) collection);

		JsonObject response = this.request(url, method, (Object) requestBody);
		JsonObject obj = response.getAsJsonObject("collection");
		c = gson.fromJson(obj, Collection.class);
		return c;
	}

	public DeletedObject deleteCollection(String collectionname)
			throws Exception {
		DeletedObject d = new DeletedObject();
		url = "/" + collectionname;
		method = "DELETE";
		Object requestBody = new Object();
		JsonObject response = this.request(url, method, requestBody);
		JsonObject deleted = response.getAsJsonObject();
		d = gson.fromJson(deleted, DeletedObject.class);
		return d;
	}

	public String flushCollection(String collectionname) throws Exception {
		url = "/" + collectionname + "/*";
		method = "DELETE";
		Object requestBody = new Object();
		JsonObject response = this.request(url, method, requestBody);
		return response.get("flushed").getAsString();
	}

	/******* DOCUMENTS ********/
	public DocumentSearchResponse getDocuments(String collectionname,
			DocumentSearchRequest requestBody) throws Exception {
		url = "/" + collectionname + "/*";
		method = "GET";
		DocumentSearchResponse documents = new DocumentSearchResponse();
		requestBody.setFull(false);
		JsonObject response = this.request(url, method, (Object) requestBody);
		documents = gson.fromJson(response, DocumentSearchResponse.class);
		return documents;
	}

	public FullDocumentSearchResponse getFullDocuments(String collectionname,
			DocumentSearchRequest requestBody) throws Exception {
		url = "/" + collectionname + "/*";
		method = "GET";
		FullDocumentSearchResponse documents = new FullDocumentSearchResponse();
		requestBody.setFull(true);
		JsonObject response = this.request(url, method, (Object) requestBody);
		documents = gson.fromJson(response, FullDocumentSearchResponse.class);
		return documents;
	}

	public Document getDocument(String collectionname, String documentname)
			throws Exception {
		url = "/" + collectionname + "/" + documentname;
		method = "GET";
		Document d;
		Object requestBody = new Object();
		JsonObject response = this.request(url, method, (Object) requestBody);
		JsonObject document = response.getAsJsonObject("document");
		d = gson.fromJson(document, Document.class);
		return d;
	}

	public Document putDocument(String collectionname, String documentname,
			Document document) throws Exception {
		url = "/" + collectionname + "/" + documentname;
		method = "PUT";
		Document d;

		HashMap<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("document", (Object) document);

		JsonObject response = this.request(url, method, requestBody);
		JsonObject obj = response.getAsJsonObject("document");
		d = gson.fromJson(obj, Document.class);
		return d;
	}

	public ArrayList<Document> postDocuments(String collectionname,
			ArrayList<Document> documents) throws Exception {
		url = "/" + collectionname + "/*";
		method = "POST";
		ArrayList<Document> setofdocs = new ArrayList<Document>();

		HashMap<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("documents", (Object) documents);

		JsonObject response = this.request(url, method, (Object) requestBody);
		JsonArray docs = response.getAsJsonArray("documents");
		for (JsonElement j : docs) {
			setofdocs.add(gson.fromJson(j, Document.class));
		}
		return setofdocs;
	}

	public DeletedObject deleteDocument(String collectionname,
			String documentname) throws Exception {
		DeletedObject d = new DeletedObject();
		url = "/" + collectionname + "/" + documentname;
		method = "DELETE";

		HashMap<String, Boolean> requestBody = new HashMap<String, Boolean>();
		requestBody.put("document", true);

		JsonObject response = this.request(url, method, (Object) requestBody);
		d = gson.fromJson(response, DeletedObject.class);
		return d;
	}

	public Iterator<DocumentReference> documentIterator(String collectionname,
			DocumentSearchRequest requestBody) throws Exception {
		url = "/" + collectionname + "/*";
		method = "GET";
		DocumentSearchResponse documents = new DocumentSearchResponse();
		requestBody.setFull(false);
		JsonObject response = this.request(url, method, (Object) requestBody);
		documents = gson.fromJson(response, DocumentSearchResponse.class);
		documents.setCollectionName(collectionname);
		documents.setClient(this);
		return documents.iterator();
	}

	public Iterator<Document> fullDocumentIterator(String collectionname,
			DocumentSearchRequest requestBody) throws Exception {
		url = "/" + collectionname + "/*";
		method = "GET";
		FullDocumentSearchResponse documents = new FullDocumentSearchResponse();
		requestBody.setFull(true);
		JsonObject response = this.request(url, method, (Object) requestBody);
		documents = gson.fromJson(response, FullDocumentSearchResponse.class);
		documents.setCollectionName(collectionname);
		documents.setClient(this);
		return documents.iterator();
	}

	/****** TASKS *****/
	public Task getTask(String collectionname, String taskname)
			throws Exception {
		url = "/" + collectionname + "/" + taskname;
		method = "GET";
		Task t;
		Object requestBody = new Object();
		JsonObject response = this.request(url, method, requestBody);
		JsonObject task = response.getAsJsonObject("task");
		t = gson.fromJson(task, Task.class);
		return t;
	}

	public Task putTask(String collectionname, String taskname, Task task)
			throws Exception {
		url = "/" + collectionname + "/" + taskname;
		method = "PUT";
		Task t;

		HashMap<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("task", (Object) task);

		JsonObject response = this.request(url, method, (Object) requestBody);
		JsonObject obj = response.getAsJsonObject("task");
		t = gson.fromJson(obj, Task.class);
		return t;
	}

	public Task postTask(String collectionname, String taskname, Task task)
			throws Exception {
		url = "/" + collectionname + "/" + taskname;
		method = "POST";
		Task t;

		HashMap<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("task", (Object) task);

		JsonObject response = this.request(url, method, (Object) requestBody);
		JsonObject obj = response.getAsJsonObject("task");
		t = gson.fromJson(obj, Task.class);
		return t;
	}

	public DeletedObject deleteTask(String collectionname, String taskname)
			throws Exception {
		DeletedObject d = new DeletedObject();
		url = "/" + collectionname + "/" + taskname;
		method = "DELETE";

		HashMap<String, Boolean> requestBody = new HashMap<String, Boolean>();
		requestBody.put("task", true);

		JsonObject response = this.request(url, method, (Object) requestBody);
		d = gson.fromJson(response, DeletedObject.class);
		return d;
	}

	/******* LABELS **********/
	public Task postLabels(String collectionname, String taskname,
			ArrayList<Label> labels) throws Exception {
		url = "/" + collectionname + "/" + taskname;
		method = "POST";
		Task t;

		HashMap<String, ArrayList<Label>> requestBody = new HashMap<String, ArrayList<Label>>();
		requestBody.put("labels", labels);

		JsonObject response = this.request(url, method, (Object) requestBody);
		JsonObject obj = response.getAsJsonObject("task");
		t = gson.fromJson(obj, Task.class);
		return t;
	}

	public DeletedObject deleteLabel(String collectionname, String taskname,
			String labelname) throws Exception {
		DeletedObject d = new DeletedObject();
		url = "/" + collectionname + "/" + taskname;
		method = "DELETE";

		HashMap<String, String> requestBody = new HashMap<String, String>();
		requestBody.put("label", labelname);

		JsonObject response = this.request(url, method, (Object) requestBody);
		d = gson.fromJson(response, DeletedObject.class);
		return d;
	}

	/******** ANNOTATIONS *******/
	public Document postAnnotations(String collectionname, String documentname,
			ArrayList<Annotation> annotations) throws Exception {
		url = "/" + collectionname + "/" + documentname;
		method = "POST";
		Document d;

		HashMap<String, ArrayList<Annotation>> requestBody = new HashMap<String, ArrayList<Annotation>>();
		requestBody.put("annotations", annotations);

		JsonObject response = this.request(url, method, (Object) requestBody);
		JsonObject obj = response.getAsJsonObject("document");
		d = gson.fromJson(obj, Document.class);
		return d;
	}

	public DeletedObject deleteAnnotation(String collectionname,
			String documentname, String annotationuuid) throws Exception {
		DeletedObject d = new DeletedObject();
		url = "/" + collectionname + "/" + documentname;
		method = "DELETE";

		HashMap<String, String> requestBody = new HashMap<String, String>();
		requestBody.put("annotation", annotationuuid);

		JsonObject response = this.request(url, method, (Object) requestBody);
		d = gson.fromJson(response, DeletedObject.class);
		return d;
	}

	/******** FEATURES *******/
	@SuppressWarnings("unchecked")
	public HashMap<String, Feature> getFeatures() throws Exception {
		url = "/" + "_features";
		method = "GET";
		Object requestBody = new Object();

		/*
		 * HashMap<String, Feature> features = new HashMap<String, Feature>();
		 * JsonObject response = this.request(url, method, requestBody);
		 * JsonObject results = response.getAsJsonObject("features");
		 * HashMap<String, Object> map = new HashMap<String, Object>(); map =
		 * gson.fromJson(results, HashMap.class); for (String name :
		 * map.keySet()){ String params = gson.toJson(map.get(name));
		 * HashMap<String, LinkedTreeMap<String, Object>> featureparams = new
		 * HashMap<String, LinkedTreeMap<String,Object>>(); featureparams =
		 * gson.fromJson(params, HashMap.class); Feature featuretype = new
		 * Feature(); featuretype.setParameters(featureparams);
		 * featuretype.setName(name); features.put(name, featuretype); }
		 */

		HashMap<String, Feature> features = new HashMap<String, Feature>();
		JsonObject response = this.request(url, method, requestBody);
		JsonObject results = response.getAsJsonObject("features");
		HashMap<String, Object> map = new HashMap<String, Object>();
		map = gson.fromJson(results, HashMap.class);

		for (String name : map.keySet()) {
			Feature feature = new Feature();
			HashMap<String, FeatureParameter> parametermap = new HashMap<String, FeatureParameter>();
			String params = gson.toJson(map.get(name));
			HashMap<String, Object> hm = gson.fromJson(params, HashMap.class);
			for (String key : hm.keySet()) {
				String attributes = gson.toJson(hm.get(key));
				FeatureParameter param = gson.fromJson(attributes,
						FeatureParameter.class);
				HashMap<String, Object> m = gson.fromJson(attributes,
						HashMap.class);
				if (m.get("default") != null) {
					param.setDefault(m.get("default").toString());
				}
				parametermap.put(key, param);
			}
			feature.setName(name);
			feature.setParameters(parametermap);
			features.put(name, feature);
		}
		return features;
	}

	public Task postFeatures(String collectionname, String taskname,
			ArrayList<HashMap<String, Object>> features) throws Exception {
		url = "/" + collectionname + "/" + taskname;
		method = "POST";
		Task task;
		HashMap<String, ArrayList<HashMap<String, Object>>> requestBody = new HashMap<String, ArrayList<HashMap<String, Object>>>();
		requestBody.put("features", features);
		JsonObject response = this.request(url, method, (Object) requestBody);
		JsonObject obj = response.getAsJsonObject("task");
		task = gson.fromJson(obj, Task.class);
		return task;
	}

	public DeletedObject deleteFeature(String collectionname, String taskname,
			HashMap<String, Object> feature) throws Exception {
		url = "/" + collectionname + "/" + taskname;
		method = "DELETE";
		DeletedObject d;
		HashMap<String, HashMap<String, Object>> requestBody = new HashMap<String, HashMap<String, Object>>();
		requestBody.put("feature", feature);
		JsonObject response = this.request(url, method, (Object) requestBody);
		d = gson.fromJson(response, DeletedObject.class);
		return d;
	}

	/******** PREDICTIONS *******/
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> getPredictionsForText(
			String collectionname, String taskname,
			HashMap<String, Object> requestBody) throws Exception {
		url = "/" + collectionname + "/" + taskname;
		method = "GET";
		ArrayList<HashMap<String, Object>> predictions = new ArrayList<HashMap<String, Object>>();
		JsonObject response = this.request(url, method, (Object) requestBody);

		JsonArray results = response.getAsJsonArray("predictions");
		for (JsonElement j : results) {
			predictions.add(gson.fromJson(j, HashMap.class));
		}
		return predictions;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> getPredictionsForDocument(
			String collectionname, String taskname, String documentname)
			throws Exception {
		url = "/" + collectionname + "/" + taskname;
		method = "GET";
		ArrayList<HashMap<String, Object>> predictions = new ArrayList<HashMap<String, Object>>();
		HashMap<String, String> requestBody = new HashMap<String, String>();
		requestBody.put("document", documentname);
		JsonObject response = this.request(url, method, (Object) requestBody);

		JsonArray results = response.getAsJsonArray("predictions");
		for (JsonElement j : results) {
			predictions.add(gson.fromJson(j, HashMap.class));
		}
		return predictions;
	}

}

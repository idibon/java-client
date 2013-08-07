package com.idibon.api.client;

import java.io.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.*;
import com.idibon.api.client.*;
import com.idibon.api.client.Collection;

public class TestClient extends Client {
	private HttpResponse preparedResponse;
	private String apikey;

	public TestClient(String apikey) {
		this.apikey = apikey;
	}

	public void prepareResponse(int expectedResponseStatus,
			String expectedResponseBody) throws HttpException {
		if (expectedResponseStatus != 200) {
			throw new HttpException();
		}
		preparedResponse = new BasicHttpResponse(new BasicStatusLine(
				new ProtocolVersion("HTTP", 1, 1), expectedResponseStatus, ""));
		preparedResponse.setStatusCode(expectedResponseStatus);
		try {
			preparedResponse.setEntity(new StringEntity(expectedResponseBody));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	protected HttpResponse getResponse(HttpEntityEnclosingRequestBase request)
			throws ClientProtocolException, IOException {
		// previousRequest = request;
		return preparedResponse;
	}

	public String getAPIKey() {
		return this.apikey;
	}

	/*
	 * public Iterator<DocumentReference> documentIterator(String
	 * collectionname, ArrayList<DocumentReference> array){
	 * Iterator<DocumentReference> iter = array.iterator(); return iter; }
	 */
}

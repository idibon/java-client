package com.idibon.api.client;

import java.util.ArrayList;

public class Document {

	private String uuid;
	private String name;
	private String collection_id;
	private String title;
	private String content;
	private Object metadata;
	private String mimetype;
	private ArrayList<Object> tokens;
	private int size;
	private boolean is_active;
	private String created_at;
	private String updated_at;
	private ArrayList<Annotation> annotations;
	private String id;

	public Document() {
	}

	public String getName() {
		return this.name;
	}

	public String getUuid() {
		return this.uuid;
	}

	public String getCollectionId() {
		return this.collection_id;
	}

	public String getTitle() {
		return this.title;
	}

	public String getContent() {
		return this.content;
	}

	public Object getMetaData() {
		return this.metadata;
	}

	public String getMimetype() {
		return this.mimetype;
	}

	public ArrayList<Object> getTokens() {
		return this.tokens;
	}

	public int getSize() {
		return this.size;
	}

	public String getId() {
		return id;
	}

	public String getCreatedAt() {
		return this.created_at;
	}

	public String getUpdatedAt() {
		return this.updated_at;
	}

	public boolean getIsActive() {
		return this.is_active;
	}

	public ArrayList<Annotation> getAnnotations() {
		return this.annotations;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCollectionId(String collectionId) {
		this.collection_id = collectionId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setMetaData(Object metadata) {
		this.metadata = metadata;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public void setTokens(ArrayList<Object> tokens) {
		this.tokens = tokens;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setCreatedAt(String createdAt) {
		this.created_at = createdAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updated_at = updatedAt;
	}

	public void setIsActive(boolean isActive) {
		this.is_active = isActive;
	}

	public void setAnnotations(ArrayList<Annotation> annotations) {
		this.annotations = annotations;
	}

}

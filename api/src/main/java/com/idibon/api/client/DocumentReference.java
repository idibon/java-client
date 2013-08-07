package com.idibon.api.client;

public class DocumentReference {

	private int annotation_count;
	private String content;
	private String created_at;
	private String name;
	private int size;

	public int getAnnotationCount() {
		return this.annotation_count;
	}

	public String getContent() {
		return this.content;
	}

	public String getCreatedAt() {
		return this.created_at;
	}

	public String getName() {
		return this.name;
	}

	public int getSize() {
		return this.size;
	}

}

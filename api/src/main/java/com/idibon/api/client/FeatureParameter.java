package com.idibon.api.client;

public class FeatureParameter {
	private String type;
	private String defaultval = null;
	private boolean required;

	public void setType(String type) {
		this.type = type;
	}

	public void setDefault(String defaultval) {
		this.defaultval = defaultval;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getType() {
		return this.type;
	}

	public String getDefault() {
		return (String) this.defaultval;
	}

	public boolean getRequired() {
		return this.required;
	}

}

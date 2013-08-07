package com.idibon.api.client;

import java.util.HashMap;

public class FeaturesinTask {
	private String created_at;
	private boolean is_active = true;
	private String name;
	private HashMap<String, String> parameters;
	private String significance;
	private String task_id;
	private String updated_at;
	private String uuid;

	public void setCreatedAt(String createdAt) {
		this.created_at = createdAt;
	}

	public void setIsActive(boolean isActive) {
		this.is_active = isActive;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}

	public void setSignificance(String significance) {
		this.significance = significance;
	}

	public void setTaskId(String taskId) {
		this.task_id = taskId;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updated_at = updatedAt;
	}

	public String getCreatedAt() {
		return this.created_at;
	}

	public boolean getIsActive() {
		return this.is_active;
	}

	public String getName() {
		return this.name;
	}

	public HashMap<String, String> getParameters() {
		return this.parameters;
	}

	public String getSignificance() {
		return this.significance;
	}

	public String getTaskId() {
		return this.task_id;
	}

	public String getUpdatedAt() {
		return this.updated_at;
	}

	public String getUuid() {
		return this.uuid;
	}

}

public class Label {

	private String created_at;
	private String description;
	private boolean is_active;
	private String name;
	private String task_id;
	private String updated_at;
	private String uuid;

	public Label() {
	}

	public String getName() {
		return this.name;
	}

	public String getUuid() {
		return this.uuid;
	}

	public String getTaskId() {
		return this.task_id;
	}

	public String getDescription() {
		return this.description;
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

	public void setName(String name) {
		this.name = name;
	}

	public void setTaskId(String taskId) {
		this.task_id = taskId;
	}

	public void setDescription(String description) {
		this.description = description;
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
}

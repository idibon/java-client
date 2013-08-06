import java.util.ArrayList;

public class Collection {

	private Object config;
	private String created_at;
	private String description;
	private boolean is_active;
	private boolean is_public;
	private String name;
	private String subscriber_id;
	private String updated_at;
	private String uuid;
	private ArrayList<Task> tasks;

	public Collection() {
	}

	public String getName() {
		return this.name;
	}

	public String getUuid() {
		return this.uuid;
	}

	public String getSubscriberId() {
		return this.subscriber_id;
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

	public Object getConfig() {
		return this.config;
	}

	public boolean getIsActive() {
		return this.is_active;
	}

	public boolean getIsPublic() {
		return this.is_public;
	}

	public ArrayList<Task> getTasks() {
		return this.tasks;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSubscriberId(String subscriberID) {
		this.subscriber_id = subscriberID;
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

	public void setConfig(Object config) {
		this.config = config;
	}

	public void setIsActive(boolean is_active) {
		this.is_active = is_active;
	}

	public void setIsPublic(boolean is_public) {
		this.is_public = is_public;
	}

	public void setTasks(Task t) {
		this.tasks.add(t);
	}

}

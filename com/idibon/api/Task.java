import java.util.ArrayList;

public class Task {

	private String collection_id = "";
	private String created_at;
	private String description = "";
	private boolean is_active = true;
	private String name = "";
	private String scope = "document";
	private boolean trainable = true;
	private String trained_at;
	private String updated_at;
	private String uuid = "";
	private ArrayList<Label> labels;
	private ArrayList<FeaturesinTask> features;

	public Task() {
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

	public String getDescription() {
		return this.description;
	}

	public String getScope() {
		return this.scope;
	}

	public ArrayList<Label> getLabels() {
		return this.labels;
	}

	public String getCreatedAt() {
		return this.created_at;
	}

	public String getUpdatedAt() {
		return this.updated_at;
	}

	public String getTrainedAt() {
		return this.trained_at;
	}

	public boolean getIsActive() {
		return this.is_active;
	}

	public boolean getTrainable() {
		return this.trainable;
	}

	public ArrayList<FeaturesinTask> getFeatures() {
		return this.features;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCollectionId(String collectionid) {
		this.collection_id = collectionid;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public void setLabels(ArrayList<Label> labels) {
		this.labels = labels;
	}

	public void setCreatedAt(String createdAt) {
		this.created_at = createdAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updated_at = updatedAt;
	}

	public void setTrainedAt(String trainedAt) {
		this.trained_at = trainedAt;
	}

	public void setIsActive(boolean isActive) {
		this.is_active = isActive;
	}

	public void setTrainable(boolean trainable) {
		this.is_active = trainable;
	}

	public void setFeatures(ArrayList<FeaturesinTask> features) {
		this.features = features;
	}
}

package com.idibon.api.client;

public class Annotation {

	private String uuid;
	private String document_id;
	private String task_id;
	private String label_id;
	private long offset;
	private long length;
	private String text;
	private long offset2;
	private long length2;
	private String text2;
	private boolean is_active;
	private boolean is_in_agreement;
	private boolean is_negated;
	private boolean is_trainable;
	private float boost;
	private float confidence;
	private String provenance;
	private String importance;
	private String created_at;
	private String updated_at;
	private Task task;
	private Label label;
	private String pending_at;
	private String queued_at;
	private String requested_for;
	private String status;
	private String subject_id;
	private String user_id;

	public Annotation() {
	}

	public String getUuid() {
		return this.uuid;
	}

	public String getTaskId() {
		return this.task_id;
	}

	public String getDocumentId() {
		return this.document_id;
	}

	public String getLabelId() {
		return this.label_id;
	}

	public long getOffset1() {
		return this.offset;
	}

	public long getLength1() {
		return this.length;
	}

	public String getText1() {
		return this.text;
	}

	public long getOffset2() {
		return this.offset2;
	}

	public long getLength2() {
		return this.length2;
	}

	public String getText2() {
		return this.text2;
	}

	public float getBoost() {
		return this.boost;
	}

	public float getConfidence() {
		return this.confidence;
	}

	public String getProvenance() {
		return this.provenance;
	}

	public String getImportance() {
		return this.importance;
	}

	public String getCreatedAt() {
		return this.created_at.toString();
	}

	public String getUpdatedAt() {
		return this.updated_at.toString();
	}

	public String getPendingAt() {
		return this.pending_at;
	}

	public String getQueuedAt() {
		return this.queued_at;
	}

	public String getRequestedFor() {
		return this.requested_for;
	}

	public String getStatus() {
		return this.status;
	}

	public String getUserId() {
		return this.user_id;
	}

	public String getSubjectId() {
		return this.subject_id;
	}

	public boolean getIsActive() {
		return this.is_active;
	}

	public boolean getIsInAgreement() {
		return this.is_in_agreement;
	}

	public boolean getIsNegated() {
		return this.is_negated;
	}

	public boolean getIsTrainable() {
		return this.is_trainable;
	}

	public Task getTask() {
		return this.task;
	}

	public Label getLabel() {
		return this.label;
	}

	public void setTaskId(String taskId) {
		this.task_id = taskId;
	}

	public void setDocumentId(String documentId) {
		this.document_id = documentId;
	}

	public void setLabelId(String labelId) {
		this.label_id = labelId;
	}

	public void setOffset1(long offset1) {
		this.offset = offset1;
	}

	public void setLength1(long length1) {
		this.length = length1;
	}

	public void setText1(String text) {
		this.text = text;
	}

	public void setOffset2(long offset2) {
		this.offset2 = offset2;
	}

	public void setLength2(long length2) {
		this.length2 = length2;
	}

	public void setText2(String text2) {
		this.text2 = text2;
	}

	public void setBoost(float boost) {
		this.boost = boost;
	}

	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}

	public void setProvenance(String provenance) {
		this.provenance = provenance;
	}

	public void setImportance(String importance) {
		this.importance = importance;
	}

	public void setCreatedAt(String createdAt) {
		this.created_at = createdAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updated_at = updatedAt;
	}

	public void setPendingAt(String pendingAt) {
		this.pending_at = pendingAt;
	}

	public void setQueuedAt(String queuedAt) {
		this.queued_at = queuedAt;
	}

	public void setRequestedFor(String requestedFor) {
		this.requested_for = requestedFor;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setUserId(String userId) {
		this.user_id = userId;
	}

	public void setSubjectId(String subjectId) {
		this.subject_id = subjectId;
	}

	public void setIsActive(boolean isActive) {
		this.is_active = isActive;
	}

	public void setIsInAgreement(boolean isInAgreement) {
		this.is_in_agreement = isInAgreement;
	}

	public void setIsNegated(boolean isNegated) {
		this.is_negated = isNegated;
	}

	public void setIsTrainable(boolean isTrainable) {
		this.is_trainable = isTrainable;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public void setLabel(Label label) {
		this.label = label;
	}
}

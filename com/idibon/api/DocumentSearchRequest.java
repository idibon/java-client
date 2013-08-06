public class DocumentSearchRequest {

	private int start = 0;
	private int count = 1000;
	private String sort = "created_at";
	private String order = "asc";
	private String before;
	private String after;
	private String task;
	private String label;
	private boolean full = false;

	public void setStart(int start) {
		this.start = start;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public void setBefore(String before) {
		this.before = before;
	}

	public void setAfter(String after) {
		this.after = after;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setFull(boolean full) {
		this.full = full;
	}

	public int getStart() {
		return this.start;
	}

	public int getCount() {
		return this.count;
	}

	public String getSort() {
		return this.sort;
	}

	public String getOrder() {
		return this.order;
	}

	public String getBefore() {
		return this.before;
	}

	public String getAfter() {
		return this.after;
	}

	public String getTask() {
		return this.task;
	}

	public String getLabel() {
		return this.label;
	}

	public boolean getFull() {
		return this.full;
	}

}

import java.util.*;

public class DocumentSearchResponse implements Iterable<DocumentReference> {

	private int total;
	private ArrayList<DocumentReference> documents;
	private int start;
	private int count;
	private String sort = "updated_at";
	private String order = "asc";
	private String task;
	private String label;
	private String content;
	private String before;
	private String after;
	private Client client;
	private String collectionname;
	private boolean full;

	public void setCollectionName(String collectionname) {
		this.collectionname = collectionname;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public int getTotal() {
		return this.total;
	}

	public ArrayList<DocumentReference> getDocumentsAfterSearch() {
		return this.documents;
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

	public String getTask() {
		return this.task;
	}

	public String getLabel() {
		return this.label;
	}

	public String getContent() {
		return this.content;
	}

	public String getAfter() {
		return this.after;
	}

	public String getBefore() {
		return this.before;
	}

	public boolean getFull() {
		return this.full;
	}

	public Iterator<DocumentReference> iterator() {
		Iterator<DocumentReference> it = new Iterator<DocumentReference>() {
			private DocumentSearchResponse response;
			private int cursor = 0;

			public void remove() {
			}

			public boolean hasNext() {
				// System.out.println("Total: " + total + " Start: " + start);
				return ((total > start + cursor) && (start < total));
			}

			private boolean retrieveMoreDocs() {
				return (cursor < documents.size());
			}

			public DocumentReference next() {
				if (retrieveMoreDocs()) {
					return documents.get(cursor++);
				} else if ((total > start + count) && (start < total)) {
					cursor = 0;
					DocumentSearchRequest request = setRequest();
					request.setStart(start + 1000);
					start = start + 1000;
					count = 1000;
					request.setCount(1000);
					try {
						response = client.getDocuments(collectionname, request);
						documents = response.getDocumentsAfterSearch();
						return documents.get(cursor++);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		};
		return it;
	}

	private DocumentSearchRequest setRequest() {
		DocumentSearchRequest request = new DocumentSearchRequest();
		request.setAfter(this.after);
		request.setBefore(this.before);
		request.setLabel(this.label);
		request.setOrder(this.order);
		request.setSort(this.sort);
		request.setTask(this.task);
		request.setFull(this.full);
		return request;
	}
}

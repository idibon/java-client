package com.idibon.api.client;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;
import com.google.gson.Gson;
import com.idibon.api.client.*;
import com.idibon.api.client.Collection;

public class APITest {
	private static TestClient client;

	private static String getcollectionsjson;
	private static String getcollectionjson;
	private static String putcollectionjson;
	private static String postcollectionjson;
	private static String deletecollectionjson;
	private static String flushcollectionjson;

	private static String getdocumentsjson;
	private static String documentiteratorjson;
	private static String getfulldocumentsjson;
	private static String fulldocumentiteratorjson;
	private static String getdocumentjson;
	private static String putdocumentjson;
	private static String postdocumentsjson;
	private static String deletedocumentjson;

	private static String gettaskjson;
	private static String puttaskjson;
	private static String posttaskjson;
	private static String deletetaskjson;

	private static String postlabelsjson;
	private static String deletelabeljson;

	private static String postannotationsjson;
	private static String deleteannotationjson;

	private static String getpredictionsjson;

	private static String getfeaturesjson;
	private static String deletefeaturejson;

	@BeforeClass
	public static void clientSetup() {
		client = new TestClient("sample-api-key");

		getcollectionsjson = ("{" + "`collections`: [" + "{"
				+ "`description`: `TestCollection1 Description`,"
				+ "`name`: `TestCollection1`," + "`uuid`: `collection-uuid-1`"
				+ "}," + "{" + "`description`: `TestCollection2 Description`,"
				+ "`name`: `TestCollection2`," + "`uuid`: `collection-uuid-2`"
				+ "}]" + "}").replace("`", "\"");

		getcollectionjson = ("{" + "`collection`:{" + "`config`:{" + "},"
				+ "`created_at`:`collection-creation-date-1`,"
				+ "`description`:`TestCollection1 Description`,"
				+ "`is_active`:true," + "`is_public`:false,"
				+ "`name`:`TestCollection1`,"
				+ "`subscriber_id`:`subscriber-id-1`,"
				+ "`updated_at`:`collection-updation-date-1`,"
				+ "`uuid`:`collection-uuid-1`," + "`tasks`:[" + "{"
				+ "`collection_id`:`collection-uuid-1`," + "`config`:{" + "},"
				+ "`created_at`:`task-creation-date-1`,"
				+ "`description`:`Task1 Description`," + "`is_active`:true,"
				+ "`name`:`Task1`," + "`scope`:`span`," + "`trainable`:true,"
				+ "`trained_at`:`task-training-date-1`,"
				+ "`updated_at`:`task-updation-date-1`,"
				+ "`uuid`:`task-uuid-1`," + "`labels`:[" + "{"
				+ "`created_at`:`label-creation-date-1`,"
				+ "`description`:null," + "`is_active`:true,"
				+ "`name`:`Label1`," + "`task_id`:`task-uuid-1`,"
				+ "`updated_at`:`label-updation-date-1`,"
				+ "`uuid`:`label-uuid-1`" + "}," + "{"
				+ "`created_at`:`label-creation-date-2`,"
				+ "`description`:`Label2 Description`," + "`is_active`:true,"
				+ "`name`:`Label2`," + "`task_id`:task-uuid-1`,"
				+ "`updated_at`:`label-updation-date-2`,"
				+ "`uuid`:`label-uuid-2`" + "}," + "{"
				+ "`created_at`:`label-creation-date-3`,"
				+ "`description`:null," + "`is_active`:true,"
				+ "`name`:`Label3`," + "`task_id`:`task-uuid-1`,"
				+ "`updated_at`:`label-updation-date-3`,"
				+ "`uuid`:`label-uuid-3`" + "}" + "]," + "`features`:[" + "{"
				+ "`created_at`:`feature-creation-date-1`,"
				+ "`is_active`:true," + "`name`:`Feature1`," + "`parameters`:{"
				+ "}," + "`significance`:null," + "`task_id`:`task-uuid-1`,"
				+ "`updated_at`:`feature-updation-date-1`,"
				+ "`uuid`:`feature-uuid-1`" + "}," + "{"
				+ "`created_at`:`feature-creation-date-2`,"
				+ "`is_active`:true," + "`name`:`Feature2`," + "`parameters`:{"
				+ "}," + "`significance`:null," + "`task_id`:`task-uuid-1`,"
				+ "`updated_at`:`feature-updation-date-2`,"
				+ "`uuid`:`feature-uuid-2`" + "}" + "]" + "}" + "]" + "}" + "}")
				.replace("`", "\"");

		putcollectionjson = ("{" + "`collection`:{" + "`config`:{" + "},"
				+ "`created_at`:`collection-creation-date-1`,"
				+ "`description`:`TestCollection1 Description`,"
				+ "`is_active`:true," + "`is_public`:false,"
				+ "`name`:`TestCollection1`,"
				+ "`subscriber_id`:`subscriber-id-1`,"
				+ "`updated_at`:`collection-updation-date-1`,"
				+ "`uuid`:`collection-uuid-1`" + "}" + "}").replace("`", "\"");

		postcollectionjson = ("{" + "`collection`:{" + "`config`:{" + "},"
				+ "`created_at`:`collection-creation-date-1`,"
				+ "`description`:`TestCollection1 Description Changed`,"
				+ "`is_active`:true," + "`is_public`:false,"
				+ "`name`:`TestCollection1`,"
				+ "`subscriber_id`:`subscriber-id-1`,"
				+ "`updated_at`:`collection-updation-date-1-new`,"
				+ "`uuid`:`collection-uuid-1`" + "}" + "}").replace("`", "\"");

		deletecollectionjson = ("{" + "`name`: `TestCollection1`,"
				+ "`deleted`: true" + "}").replace("`", "\"");

		flushcollectionjson = ("{" + "`flushed`: `collection-uuid-1`" + "}")
				.replace("`", "\"");

		getdocumentjson = ("{" + "`document`: {"
				+ "`collection_id`: `collection-uuid-1`,"
				+ "`content`: `TestDocument1 Content`,"
				+ "`created_at`: `document-creation-date-1`,"
				+ "`is_active`: false," + "`metadata`: {" + "},"
				+ "`mimetype`: null," + "`name`: `TestDocument1`,"
				+ "`size`: 0," + "`title`: null," + "`token_count`: null,"
				+ "`updated_at`: `collection-updation-date-1`,"
				+ "`uuid`: `document-uuid-1`" + "}" + "}").replace("`", "\"");

		getdocumentsjson = ("{" + "`total`: 2," + "`documents`: [" + "{"
				+ "`annotation_count`: 0,"
				+ "`content`: `TestDocument1 Content`,"
				+ "`created_at`: `document-creation-date-1`,"
				+ "`name`: `TestDocument1`," + "`size`: 21,"
				+ "`token_count`: 2" + "}," + "{" + "`annotation_count`: 0,"
				+ "`content`: `TestDocument2 Content `,"
				+ "`created_at`: `document-creation-date-2`,"
				+ "`name`: `TestDocument2`," + "`size`: 22,"
				+ "`token_count`: 2" + "}" + "]," + "`start`: 0,"
				+ "`count`: 1000," + "`sort`: `created_at`," + "`label`: null,"
				+ "`task`: null," + "`content`: null," + "`full`: `false`,"
				+ "`order`: `desc`" + "}").replace("`", "\"");

		getfulldocumentsjson = ("{" + "`total`: 2," + "`documents`: [" + "{"
				+ "`collection_id`: `collection-uuid-1`,"
				+ "`content`: `TestDocument1 Content`,"
				+ "`created_at`: `document-creation-date-1`,"
				+ "`is_active`: false," + "`metadata`: {" + "},"
				+ "`mimetype`: null," + "`name`: `TestDocument1`,"
				+ "`size`: 0," + "`title`: null," + "`token_count`: null,"
				+ "`tokens`: [" + "{" + "`offset`: 0," + "`length`: 20,"
				+ "`value`: `word`," + "`text`: `TestDocument1 Content`" + "}"
				+ "]," + "`updated_at`: `document-updation-date-1`,"
				+ "`uuid`: `document-uuid-1`" + "}," + "{"
				+ "`collection_id`: `collection-uuid-1`,"
				+ "`content`: `TestDocument2 Content`,"
				+ "`created_at`: `document-creation-date-2`,"
				+ "`is_active`: false," + "`metadata`: {" + "},"
				+ "`mimetype`: null," + "`name`: `TestDocument2`,"
				+ "`size`: 0," + "`title`: null," + "`token_count`: null,"
				+ "`tokens`: [" + "{" + "`offset`: 0," + "`length`: 20,"
				+ "`value`: `word`," + "`text`: `TestDocument2 Content`" + "}"
				+ "]," + "`updated_at`: `document-updation-date-2`,"
				+ "`uuid`: `document-uuid-2`" + "}" + "]," + "`start`: 0,"
				+ "`count`: 1000," + "`sort`: `created_at`," + "`label`: null,"
				+ "`task`: null," + "`content`: null," + "`full`: `true`,"
				+ "`order`: `desc`" + "}").replace("`", "\"");

		putdocumentjson = ("{" + "`document`: {" + "`id`: `document-uuid-1`,"
				+ "`name`: `TestDocument1`" + "}" + "}").replace("`", "\"");

		postdocumentsjson = ("{" + "`documents`: [" + "{"
				+ "`uuid`: `document-uuid-1`," + "`name`: `TestDocument1`"
				+ "}," + "{" + "`uuid`: `document-uuid-2`,"
				+ "`name`: `TestDocument2`" + "}" + "]" + "}").replace("`",
				"\"");

		deletedocumentjson = ("{" + "`name`: `TestDocument1`,"
				+ "`deleted`: true" + "}").replace("`", "\"");

		gettaskjson = ("{" + "`task`: {"
				+ "`collection_id`: `collection-uuid-1`," + "`config`: {"
				+ "}," + "`created_at`: `task-creation-date-1`,"
				+ "`description`: `TestTask1 Content`," + "`is_active`: true,"
				+ "`name`: `TestTask1`," + "`scope`: `span`,"
				+ "`trainable`: true,"
				+ "`trained_at`: `task-training-date-1`,"
				+ "`updated_at`: `task-updation-date-1`,"
				+ "`uuid`: `task-uuid-1`," + "`labels`: [" + "{"
				+ "`created_at`: `label-creation-date-1`,"
				+ "`description`: null," + "`is_active`: true,"
				+ "`name`: `Label1`," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `label-updation-date-1`,"
				+ "`uuid`: `label-uuid-1`" + "}," + "{"
				+ "`created_at`: `label-creation-date-2`,"
				+ "`description`: `Label2 Content`," + "`is_active`: true,"
				+ "`name`: `Label2`," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `label-updation-date-1`,"
				+ "`uuid`: `label-uuid-2`" + "}" + "]," + "`features`: [" + "{"
				+ "`created_at`: `feature-creation-date-1`,"
				+ "`is_active`: true," + "`name`: `Feature1`,"
				+ "`parameters`: {" + "`offset`: 3" + "},"
				+ "`significance`: null," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `feature-updation-date-1`,"
				+ "`uuid`: `feature-uuid-1`" + "}," + "{"
				+ "`created_at`: `feature-creation-date-2`,"
				+ "`is_active`: true," + "`name`: `Feature2`,"
				+ "`parameters`: {" + "`length`: 2" + "},"
				+ "`significance`: null," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `feature-updation-date-2`,"
				+ "`uuid`: `feature-uuid-2`" + "}" + "]" + "}" + "}").replace(
				"`", "\"");

		puttaskjson = ("{" + "task: {"
				+ "`collection_id`: `collection-uuid-1`," + "`config`: {"
				+ "}," + "`created_at`: `task-creation-date-1`,"
				+ "`description`: `TestTask1 Content`," + "`is_active`: true,"
				+ "`name`: `TestTask1`," + "`scope`: `span`,"
				+ "`trainable`: true,"
				+ "`trained_at`: `task-training-date-1`,"
				+ "`updated_at`: `task-updation-date-1`,"
				+ "`uuid`: `task-uuid-1`," + "`labels`: [" + "{"
				+ "`created_at`: `label-creation-date-1`,"
				+ "`description`: null," + "`is_active`: true,"
				+ "`name`: `Label1`," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `label-updation-date-1`,"
				+ "`uuid`: `label-uuid-1`" + "}," + "{"
				+ "`created_at`: `label-creation-date-2`,"
				+ "`description`: `Label2 Content`," + "`is_active`: true,"
				+ "`name`: `Label2`," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `label-updation-date-2`,"
				+ "`uuid`: `label-uuid-2`" + "}" + "]," + "`features`: [" + "]"
				+ "}" + "}").replace("`", "\"");

		posttaskjson = ("{" + "`task`: {"
				+ "`collection_id`: `collection-uuid-1`," + "`config`: {"
				+ "}," + "`created_at`: `task-creation-date-1`,"
				+ "`description`: `TestTask1 New Content`,"
				+ "`is_active`: true," + "`name`: `TestTask1`,"
				+ "`scope`: `document`," + "`trainable`: true,"
				+ "`trained_at`: `task-training-date-1`,"
				+ "`updated_at`: `task-updation-date-1`,"
				+ "`uuid`: `task-uuid-1`," + "`labels`: [" + "{"
				+ "`created_at`: `label-creation-date-1`,"
				+ "`description`: null," + "`is_active`: true,"
				+ "`name`: `Label1`," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `label-updation-date-1`,"
				+ "`uuid`: `label-uuid-1`" + "}," + "{"
				+ "`created_at`: `label-creation-date-2`,"
				+ "`description`: `Label2 Content`," + "`is_active`: true,"
				+ "`name`: `Label2`," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `label-updation-date-2`,"
				+ "`uuid`: `label-uuid-2`" + "}" + "]," + "`features`: [" + "{"
				+ "`created_at`: `feature-creation-date-1`,"
				+ "`is_active`: true," + "`name`: `Feature1`,"
				+ "`parameters`: {" + "`offset`: 3" + "},"
				+ "`significance`: null," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `feature-updation-date-1`,"
				+ "`uuid`: `feature-uuid-1`" + "}" + "]" + "}" + "}").replace(
				"`", "\"");

		deletetaskjson = ("{" + "`name`: `TestTask1`," + "`deleted`: true"
				+ "}").replace("`", "\"");

		postlabelsjson = ("{" + "`task`: {"
				+ "`collection_id`: `collection-uuid-1`," + "`config`: {"
				+ "}," + "`created_at`: `task-creation-date-1`,"
				+ "`description`: `TestTask1 Content`," + "`is_active`: true,"
				+ "`name`: `TestTask1`," + "`scope`: `span`,"
				+ "`trainable`: true,"
				+ "`trained_at`: `task-training-date-1`,"
				+ "`updated_at`: `task-updation-date-1`,"
				+ "`uuid`: `task-uuid-1`," + "`labels`: [" + "{"
				+ "`created_at`: `label-creation-date-1`,"
				+ "`description`: null," + "`is_active`: true,"
				+ "`name`: `Label1`," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `label-updation-date-1`,"
				+ "`uuid`: `label-uuid-1`" + "}," + "{"
				+ "`created_at`: `label-creation-date-2`,"
				+ "`description`: `Label2 Content`," + "`is_active`: true,"
				+ "`name`: `Label2`," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `label-updation-date-2`,"
				+ "`uuid`: `label-uuid-2`" + "}," + "{"
				+ "`created_at`: `label-creation-date-3`,"
				+ "`description`: `Label3 Content`," + "`is_active`: true,"
				+ "`name`: `Label3`," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `label-updation-date-3`,"
				+ "`uuid`: `label-uuid-3`" + "}" + "]," + "`features`: [" + "{"
				+ "`created_at`: `feature-creation-date-1`,"
				+ "`is_active`: true," + "`name`: `Feature1`,"
				+ "`parameters`: {" + "`min`: 3," + "`max`: 6" + "},"
				+ "`significance`: null," + "`task_id`: `task-uuid-1`,"
				+ "`updated_at`: `feature-updation-date-1`,"
				+ "`uuid`: `feature-uuid-1`" + "}" + "]" + "}" + "}").replace(
				"`", "\"");

		deletelabeljson = ("{" + "`name`: `TestLabel1`," + "`deleted`: true"
				+ "}").replace("`", "\"");

		postannotationsjson = ("{" + "`document`: {"
				+ "`collection_id`: `collection-uuid-1`,"
				+ "`content`: `TestDocument1 Content`,"
				+ "`created_at`: `2013-08-01T22:28:02+00:00`,"
				+ "`is_active`: false," + "`metadata`: {" + "},"
				+ "`mimetype`: null," + "`name`: `TestDocument1`,"
				+ "`size`: 0," + "`title`: null," + "`token_count`: null,"
				+ "`updated_at`: `document-updation-date-1`,"
				+ "`uuid`: `document-uuid-1`," + "`annotations`: [" + "{"
				+ "`boost`: 0.0," + "`confidence`: 0.0,"
				+ "`created_at`: `annotation-creation-date-1`,"
				+ "`document_id`: `document-uuid-1`," + "`importance`: null,"
				+ "`is_active`: false," + "`is_in_agreement`: false,"
				+ "`is_negated`: false," + "`is_trainable`: false,"
				+ "`label_id`: `label-uuid-1`," + "`length`: 4,"
				+ "`length2`: 0," + "`offset`: 3," + "`offset2`: 0,"
				+ "`pending_at`: null," + "`provenance`: null,"
				+ "`queued_at`: null," + "`requested_for`: null,"
				+ "`status`: null," + "`subject_id`: null,"
				+ "`task_id`: `task-uuid-1`," + "`text`: null,"
				+ "`text2`: null,"
				+ "`updated_at`: `annotation_updation-date-1`,"
				+ "`user_id`: null," + "`uuid`: `annotation-uuid-1`,"
				+ "`label`: {" + "`description`: `Label1 Description`,"
				+ "`name`: `Label1`" + "}," + "`task`: {"
				+ "`description`: `Task1 Description`," + "`name`: `Task1`,"
				+ "`scope`: `span`" + "}" + "}" + "]" + "}" + "}").replace("`",
				"\"");

		deleteannotationjson = ("{" + "`name`: `annotation-uuid-1`,"
				+ "`deleted`: true" + "}").replace("`", "\"");

		getfeaturesjson = ("{" + "`features`: {" + "`Feature1`: {" + "},"
				+ "`Feature2`: {" + "`param1`: {" + "`type`: `Integer`,"
				+ "`default`: 1," + "`required`: false" + "}," + "`param2`: {"
				+ "`type`: `Integer`," + "`default`: 3," + "`required`: false"
				+ "}" + "}," + "`Feature3`: {" + "`param2`: {"
				+ "`type`: `Integer`," + "`default`: null,"
				+ "`required`: false" + "}," + "`param3`: {"
				+ "`type`: `Integer`," + "`default`: null,"
				+ "`required`: false" + "}" + "}" + "}" + "}").replace("`",
				"\"");

		deletefeaturejson = ("{" + "`name`: `Feature1`," + "`deleted`: true"
				+ "}").replace("`", "\"");

		getpredictionsjson = ("[" + "{" + "`class`: `Label1`,"
				+ "`confidence`: 0.9," + "`classes`: {" + "`Label1`: 0.9,"
				+ "`Label2`: 0.053," + "`Label3`: 0.047" + "}," + "`scaled`: {"
				+ "`Label1`: 0.85," + "`Label2`: 0.09," + "`Label3`: 0.06"
				+ "}," + "`offset`: 11," + "`length`: 7,"
				+ "`text`: `TestText1`," + "`description`: `TestDescription1`"
				+ "}," + "{" + "`class`: `Label3`," + "`confidence`: 0.75,"
				+ "`classes`: {" + "`Label3`: 0.75," + "`Label1`: 0.15,"
				+ "`Label2`: 0.10" + "}," + "`scaled`: {" + "`Label3`: 0.7,"
				+ "`Label1`: 0.2," + "`Label2`: 0.1" + "}," + "`offset`: 35,"
				+ "`length`: 10," + "`text`: `TestText2`,"
				+ "`description`: `TestDescription2`" + "}" + "]").replace("`",
				"\"");

		documentiteratorjson = ("{" + "`total`: 3," + "`documents`: [" + "{"
				+ "`annotation_count`: 0,"
				+ "`content`: `TestDocument1 Content`,"
				+ "`created_at`: `document-creation-date-1`,"
				+ "`name`: `TestDocument1`," + "`size`: 21,"
				+ "`token_count`: 2" + "}," + "{" + "`annotation_count`: 0,"
				+ "`content`: `TestDocument2 Content `,"
				+ "`created_at`: `document-creation-date-2`,"
				+ "`name`: `TestDocument2`," + "`size`: 22,"
				+ "`token_count`: 2" + "}," + "{" + "`annotation_count`: 0,"
				+ "`content`: `TestDocument3 Content `,"
				+ "`created_at`: `document-creation-date-3`,"
				+ "`name`: `TestDocument3`," + "`size`: 50,"
				+ "`token_count`: 2" + "}" + "]," + "`start`: 0,"
				+ "`count`: 1000," + "`sort`: `created_at`," + "`label`: null,"
				+ "`task`: null," + "`content`: null," + "`full`: `false`,"
				+ "`order`: `desc`" + "}").replace("`", "\"");

		fulldocumentiteratorjson = ("{" + "`total`: 4," + "`documents`: ["
				+ "{" + "`collection_id`: `collection-uuid-1`,"
				+ "`content`: `TestDocument1 Content`,"
				+ "`created_at`: `document-creation-date-1`,"
				+ "`is_active`: false," + "`metadata`: {" + "},"
				+ "`mimetype`: null," + "`name`: `TestDocument1`,"
				+ "`size`: 0," + "`title`: null," + "`token_count`: null,"
				+ "`tokens`: [" + "{" + "`offset`: 0," + "`length`: 20,"
				+ "`value`: `word`," + "`text`: `TestDocument1 Content`" + "}"
				+ "]," + "`updated_at`: `document-updation-date-1`,"
				+ "`uuid`: `document-uuid-1`" + "}," + "{"
				+ "`collection_id`: `collection-uuid-1`,"
				+ "`content`: `TestDocument2 Content`,"
				+ "`created_at`: `document-creation-date-2`,"
				+ "`is_active`: false," + "`metadata`: {" + "},"
				+ "`mimetype`: null," + "`name`: `TestDocument2`,"
				+ "`size`: 0," + "`title`: null," + "`token_count`: null,"
				+ "`tokens`: [" + "{" + "`offset`: 0," + "`length`: 20,"
				+ "`value`: `word`," + "`text`: `TestDocument2 Content`" + "}"
				+ "]," + "`updated_at`: `document-updation-date-2`,"
				+ "`uuid`: `document-uuid-2`" + "}," + "{"
				+ "`collection_id`: `collection-uuid-1`,"
				+ "`content`: `TestDocument3 Content`,"
				+ "`created_at`: `document-creation-date-3`,"
				+ "`is_active`: false," + "`metadata`: {" + "},"
				+ "`mimetype`: null," + "`name`: `TestDocument3`,"
				+ "`size`: 0," + "`title`: null," + "`token_count`: null,"
				+ "`tokens`: [" + "{" + "`offset`: 0," + "`length`: 20,"
				+ "`value`: `word`," + "`text`: `TestDocument3 Content`" + "}"
				+ "]," + "`updated_at`: `document-updation-date-3`,"
				+ "`uuid`: `document-uuid-3`" + "}," + "{"
				+ "`collection_id`: `collection-uuid-1`,"
				+ "`content`: `TestDocument4 Content`,"
				+ "`created_at`: `document-creation-date-4`,"
				+ "`is_active`: false," + "`metadata`: {" + "},"
				+ "`mimetype`: null," + "`name`: `TestDocument4`,"
				+ "`size`: 0," + "`title`: null," + "`token_count`: null,"
				+ "`tokens`: [" + "{" + "`offset`: 0," + "`length`: 20,"
				+ "`value`: `word`," + "`text`: `TestDocument4 Content`" + "}"
				+ "]," + "`updated_at`: `document-updation-date-4`,"
				+ "`uuid`: `document-uuid-4`" + "}" + "]," + "`start`: 0,"
				+ "`count`: 1000," + "`sort`: `created_at`," + "`label`: null,"
				+ "`task`: null," + "`content`: null," + "`full`: `true`,"
				+ "`order`: `desc`" + "}").replace("`", "\"");
	}

	@Test
	public void testAPIKey() {
		assertNotNull("API key is null!", client.getAPIKey());
		assertEquals("API key doesn't seem right!", "sample-api-key",
				client.getAPIKey());
	}

	@Test
	public void testGetCollections() throws Exception {
		client.prepareResponse(200, getcollectionsjson);
		ArrayList<Collection> collections = client.getCollections();
		assertEquals("Number of collections returned are not the same", 2,
				collections.size());
		assertEquals("Collection names do not match", "TestCollection1",
				collections.get(0).getName());
		assertEquals("Collection descriptions do not match",
				"TestCollection2 Description", collections.get(1)
						.getDescription());
		assertEquals("Collection UUIDs do not match", "collection-uuid-2",
				collections.get(1).getUuid());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCollectionsError() throws Exception {
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		client.getCollections();
	}

	@Test
	public void testGetCollection() throws Exception {
		String collectionname = "TestCollection1";
		client.prepareResponse(200, getcollectionjson);
		Collection collection = client.getCollection(collectionname);
		assertEquals("Collection name does not match", collectionname,
				collection.getName());
		assertEquals("Collection description does not match",
				"TestCollection1 Description", collection.getDescription());
		assertEquals("Collection subscriber id does not match",
				"subscriber-id-1", collection.getSubscriberId());
		assertNotNull("Collection UUID found to be null", collection.getUuid());
		assertEquals("Collection UUID does not match", "collection-uuid-1",
				collection.getUuid());
		assertNotNull("Collection tasks found to be null",
				collection.getTasks());
		assertEquals("Collection task sizes do not match", 1, collection
				.getTasks().size());
		assertEquals("Collection task name does not match", "Task1", collection
				.getTasks().get(0).getName());
		assertEquals("Collection task description does not match",
				"Task1 Description", collection.getTasks().get(0)
						.getDescription());
		assertEquals("Collection task collection uuid does not match",
				"collection-uuid-1", collection.getTasks().get(0)
						.getCollectionId());
		assertEquals("Collection task scope does not match", "span", collection
				.getTasks().get(0).getScope());
		assertNotNull("Collection task uuid to be null", collection.getTasks()
				.get(0).getUuid());
		assertEquals("Collection task uuid does not match", "task-uuid-1",
				collection.getTasks().get(0).getUuid());
		assertNotNull("Collection labels found to be null", collection
				.getTasks().get(0).getLabels());
		assertEquals("Collection number of labels for task does not match", 3,
				collection.getTasks().get(0).getLabels().size());
		assertEquals("Collection label name for task does not match", "Label1",
				collection.getTasks().get(0).getLabels().get(0).getName());
		assertEquals("Collection label description for task does not match",
				"Label2 Description", collection.getTasks().get(0).getLabels()
						.get(1).getDescription());
		assertEquals("Collection label's task id does not match",
				"task-uuid-1", collection.getTasks().get(0).getLabels().get(2)
						.getTaskId());
		assertEquals("Collection label uuid does not match", "label-uuid-3",
				collection.getTasks().get(0).getLabels().get(2).getUuid());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCollectionError() throws Exception {
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		String collectionname = "TestCollection";
		client.prepareResponse(200, response);
		client.getCollection(collectionname);
	}

	@Test
	public void testPutCollection() throws Exception {
		String collectionname = "TestCollection1";
		client.prepareResponse(200, putcollectionjson);
		Collection putcollection = new Gson().fromJson(putcollectionjson,
				Collection.class);
		Collection collection = client.putCollection(collectionname,
				putcollection);
		assertEquals("Collection name does not match", collectionname,
				collection.getName());
		assertEquals("Collection description does not match",
				"TestCollection1 Description", collection.getDescription());
		assertEquals("Collection subscriber id does not match",
				"subscriber-id-1", collection.getSubscriberId());
		assertNotNull("Collection UUID found to be null", collection.getUuid());
		assertEquals("Collection uuid does not match", "collection-uuid-1",
				collection.getUuid());
	}

	@Test(expected = IllegalStateException.class)
	public void testPutCollectionError() throws Exception {
		String response = ("{`errors`:`ERROR: collection description not provided`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		Collection putcollection = new Gson().fromJson(putcollectionjson,
				Collection.class);
		client.putCollection(collectionname, putcollection);
	}

	@Test
	public void testPostCollection() throws Exception {
		String collectionname = "TestCollection1";
		client.prepareResponse(200, postcollectionjson);
		Collection postcollection = new Gson().fromJson(postcollectionjson,
				Collection.class);
		Collection collection = client.postCollection(collectionname,
				postcollection);
		assertEquals("Collection name does not match", collectionname,
				collection.getName());
		assertEquals("Collection description does not match",
				"TestCollection1 Description Changed",
				collection.getDescription());
		assertEquals("Collection subscriber id does not match",
				"subscriber-id-1", collection.getSubscriberId());
		assertNotNull("Collection UUID found to be null", collection.getUuid());
		assertEquals("Collection uuid does not match", "collection-uuid-1",
				collection.getUuid());
		assertEquals(
				"Collection was not updated! Updation Date does not match",
				"collection-updation-date-1-new", collection.getUpdatedAt());
	}

	@Test
	public void testPostCollectionNameInURL() throws Exception {
		String collectionname = "DifferentTestCollection";
		client.prepareResponse(200, postcollectionjson);
		Collection postcollection = new Gson().fromJson(postcollectionjson,
				Collection.class);
		Collection collection = client.postCollection(collectionname,
				postcollection);
		assertNotSame("Collection name does not match", collectionname,
				collection.getName());
	}

	@Test(expected = IllegalStateException.class)
	public void testPostCollectionError() throws Exception {
		String response = ("{`errors`:`ERROR: collection description not provided`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		Collection postcollection = new Gson().fromJson(postcollectionjson,
				Collection.class);
		client.postCollection(collectionname, postcollection);
	}

	@Test(expected = IllegalStateException.class)
	public void testPostNotExistingCollectionError() throws Exception {
		String response = ("{`errors`:`ERROR: Not authorized for this collection`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		Collection postcollection = new Gson().fromJson(postcollectionjson,
				Collection.class);
		client.postCollection(collectionname, postcollection);
	}

	@Test
	public void testDeleteCollection() throws Exception {
		String collectionname = "TestCollection1";
		client.prepareResponse(200, deletecollectionjson);
		DeletedObject deletedobj = client.deleteCollection(collectionname);
		assertEquals("Collection name does not match", collectionname,
				deletedobj.getName());
		assertEquals("Flag does not match", true, deletedobj.getIsDeleted());
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteCollectionError() throws Exception {
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		client.deleteCollection(collectionname);
	}

	@Test
	public void testFlushCollection() throws Exception {
		String collectionname = "TestCollection1";
		client.prepareResponse(200, flushcollectionjson);
		String id = client.flushCollection(collectionname);
		assertEquals("Collection ID does not match", "collection-uuid-1", id);
	}

	@Test(expected = IllegalStateException.class)
	public void testFlushCollectionError() throws Exception {
		String response = ("{`errors`:`undefined method 'table_name' for NilClass:Class`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		client.flushCollection(collectionname);
	}

	@Test
	public void testGetDocuments() throws Exception {
		String collectionname = "TestCollection1";
		DocumentSearchRequest request = new DocumentSearchRequest();
		client.prepareResponse(200, getdocumentsjson);
		DocumentSearchResponse documents = client.getDocuments(collectionname,
				request);
		assertEquals("Number of documents returned are not the same", 2,
				documents.getDocumentsAfterSearch().size());
		assertEquals("Document names do not match", "TestDocument1", documents
				.getDocumentsAfterSearch().get(0).getName());
		assertEquals("Document contents do not match",
				"TestDocument2 Content ", documents.getDocumentsAfterSearch()
						.get(1).getContent());
		assertEquals("Document content sizes do not match", 22, documents
				.getDocumentsAfterSearch().get(1).getSize());
		assertEquals("Start of document search does not match", 0,
				documents.getStart());
		assertEquals("Count of document search does not match", 1000,
				documents.getCount());
		assertEquals("Sort field of document search does not match",
				"created_at", documents.getSort());
		assertEquals("Sorting order of document search does not match", "desc",
				documents.getOrder());
		assertEquals("Full document option of document search does not match",
				false, documents.getFull());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetDocumentsNotFoundError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		client.getDocuments(collectionname, request);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetDocumentsSortFieldError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`ERROR: Invalid sort field: {:sort=>\'abcd\'}`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		client.getDocuments(collectionname, request);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetDocumentsSortOrderError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`ERROR: Invalid sort order: {:order=>\'abcd\'}`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		client.getDocuments(collectionname, request);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetDocumentsNoTaskError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`ERROR: Label provided with no task: {:label=>\'abcd\'}`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		client.getDocuments(collectionname, request);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetDocumentsNoLabelError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`undefined method 'id' for nil:NilClass`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		client.getDocuments(collectionname, request);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetDocumentsCountStringError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`comparison of Fixnum with String failed`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		client.getDocuments(collectionname, request);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetDocumentsStartNegativeError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`ActiveRecord::JDBCError: You have an error in "
				+ "your SQL syntax; check the manual that corresponds to your "
				+ "MySQL server version for the right syntax to use near '-10' "
				+ "at line 1: SELECT  documents.name, documents.size, "
				+ "documents.created_at, 0 AS token_count, 0 AS annotation_count,"
				+ " SUBSTR(documents.content, 1, 140) AS content FROM 'documents'"
				+ "  WHERE 'documents'.'collection_id' = x'b245d94504cb5071a866aae87a1fdd9b' "
				+ "ORDER BY documents.created_at desc, documents.uuid desc LIMIT 1000 OFFSET -10`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		client.getDocuments(collectionname, request);
	}

	@Test
	public void testDocumentIterator() throws Exception {
		String collectionname = "TestCollection";
		client.prepareResponse(200, documentiteratorjson);
		Iterator<DocumentReference> iter = client.documentIterator(
				collectionname, new DocumentSearchRequest());
		assertEquals("Next document not found using iterator", true,
				iter.hasNext());
		assertEquals("Document names do not match", "TestDocument1", iter
				.next().getName());
		assertEquals("Document contents do not match",
				"TestDocument2 Content ", iter.next().getContent());
		assertEquals("Document content sizes do not match", 50, iter.next()
				.getSize());
	}

	@Test
	public void testGetFullDocuments() throws Exception {
		String collectionname = "TestCollection1";
		DocumentSearchRequest request = new DocumentSearchRequest();
		client.prepareResponse(200, getfulldocumentsjson);
		assertTrue(client.getFullDocuments(collectionname,
				new DocumentSearchRequest()) instanceof FullDocumentSearchResponse);
		FullDocumentSearchResponse documents = client.getFullDocuments(
				collectionname, request);
		assertEquals("Number of documents returned are not the same", 2,
				documents.getDocumentsAfterSearch().size());
		assertEquals("Document collection uuid does not match",
				"collection-uuid-1", documents.getDocumentsAfterSearch().get(0)
						.getCollectionId());
		assertEquals("Document names do not match", "TestDocument1", documents
				.getDocumentsAfterSearch().get(0).getName());
		assertEquals("Document contents do not match", "TestDocument2 Content",
				documents.getDocumentsAfterSearch().get(1).getContent());
		assertEquals("Document content sizes do not match", 0, documents
				.getDocumentsAfterSearch().get(1).getSize());
		assertNotNull("Text in tokens found to be null", documents
				.getDocumentsAfterSearch().get(1).getTokens().get(0));
		assertNotNull("Document uuid null", documents.getDocumentsAfterSearch()
				.get(1).getUuid());
		assertEquals("Document uuid does not match", "document-uuid-2",
				documents.getDocumentsAfterSearch().get(1).getUuid());
		assertEquals("Start of document search does not match", 0,
				documents.getStart());
		assertEquals("Count of document search does not match", 1000,
				documents.getCount());
		assertEquals("Sort field of document search does not match",
				"created_at", documents.getSort());
		assertEquals("Sorting order of document search does not match", "desc",
				documents.getOrder());
		assertEquals("Full document option of document search does not match",
				true, documents.getFull());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetFullDocumentsNotFoundError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		client.getFullDocuments(collectionname, request);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetFullDocumentsSortFieldError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`ERROR: Invalid sort field: {:sort=>\'abcd\'}`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		client.getFullDocuments(collectionname, request);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetFullDocumentsSortOrderError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`ERROR: Invalid sort order: {:order=>\'abcd\'}`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		client.getFullDocuments(collectionname, request);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetFullDocumentsNoTaskError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`ERROR: Label provided with no task: {:label=>\'abcd\'}`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		client.getFullDocuments(collectionname, request);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetFullDocumentsNoLabelError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`undefined method 'id' for nil:NilClass`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		client.getFullDocuments(collectionname, request);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetFullDocumentsCountStringError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`comparison of Fixnum with String failed`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		client.getFullDocuments(collectionname, request);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetFullDocumentsStartNegativeError() throws Exception {
		String collectionname = "TestCollection";
		DocumentSearchRequest request = new DocumentSearchRequest();
		String response = ("{`errors`:`ActiveRecord::JDBCError: You have an error in "
				+ "your SQL syntax; check the manual that corresponds to your "
				+ "MySQL server version for the right syntax to use near '-10' "
				+ "at line 1: SELECT  documents.name, documents.size, "
				+ "documents.created_at, 0 AS token_count, 0 AS annotation_count,"
				+ " SUBSTR(documents.content, 1, 140) AS content FROM 'documents'"
				+ "  WHERE 'documents'.'collection_id' = x'b245d94504cb5071a866aae87a1fdd9b' "
				+ "ORDER BY documents.created_at desc, documents.uuid desc LIMIT 1000 OFFSET -10`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		client.getFullDocuments(collectionname, request);
	}

	@Test
	public void testFullDocumentIterator() throws Exception {
		String collectionname = "TestCollection";
		client.prepareResponse(200, fulldocumentiteratorjson);
		Iterator<Document> iter = client.fullDocumentIterator(collectionname,
				new DocumentSearchRequest());
		assertEquals("Next document not found using iterator", true,
				iter.hasNext());
		assertEquals("Document names do not match", "TestDocument1", iter
				.next().getName());
		assertEquals("Document contents do not match", "TestDocument2 Content",
				iter.next().getContent());
		assertEquals("Document content sizes do not match", 0, iter.next()
				.getSize());
		assertEquals("Document uuid does not match", "document-uuid-4", iter
				.next().getUuid());
	}

	@Test
	public void testGetDocument() throws Exception {
		String collectionname = "TestCollection1";
		String documentname = "TestDocument1";
		client.prepareResponse(200, getdocumentjson);
		Document document = client.getDocument(collectionname, documentname);
		assertEquals("Document name does not match", documentname,
				document.getName());
		assertEquals("Document content does not match",
				"TestDocument1 Content", document.getContent());
		assertEquals("Document collection id does not match",
				"collection-uuid-1", document.getCollectionId());
		assertNotNull("Document UUID found to be null", document.getUuid());
		assertEquals("Document UUID does not match", "document-uuid-1",
				document.getUuid());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetDocumentError() throws Exception {
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		String documentname = "TestDocument";
		String collectionname = "TestCollection";
		client.prepareResponse(200, response);
		client.getDocument(collectionname, documentname);
	}

	@Test
	public void testPutDocument() throws Exception {
		String documentname = "TestDocument1";
		String collectionname = "TestCollection1";
		client.prepareResponse(200, putdocumentjson);
		Document putdocument = new Gson().fromJson(putdocumentjson,
				Document.class);
		Document document = client.putDocument(collectionname, documentname,
				putdocument);
		assertEquals("Document name does not match", documentname,
				document.getName());
		assertEquals("Document UUID does not match", "document-uuid-1",
				document.getId());
	}

	@Test
	public void testPutDocumentNameInURL() throws Exception {
		String documentname = "DifferentTestDocument";
		String collectionname = "TestCollection";
		client.prepareResponse(200, putdocumentjson);
		Document putdocument = new Gson().fromJson(putdocumentjson,
				Document.class);
		Document document = client.putDocument(collectionname, documentname,
				putdocument);
		assertNotSame("Document name does not match", documentname,
				document.getName());
	}

	@Test(expected = IllegalStateException.class)
	public void testPutDocumentError() throws Exception {
		String response = ("{`errors`:{`content`:[`can't be blank`]}}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		String documentname = "TestDocument";
		String collectionname = "TestCollection";
		Document putdocument = new Gson().fromJson(putdocumentjson,
				Document.class);
		client.putDocument(collectionname, documentname, putdocument);
	}

	@Test
	public void testPostDocuments() throws Exception {
		String collectionname = "TestCollection1";
		client.prepareResponse(200, postdocumentsjson);
		ArrayList<Document> postdocument = new ArrayList<Document>();
		for (int i = 1; i < 3; i++) {
			Document doc = new Document();
			doc.setName("TestDocument" + i);
			doc.setContent("Content");
			postdocument.add(doc);
		}
		ArrayList<Document> document = client.postDocuments(collectionname,
				postdocument);
		assertEquals("Size of list of documents posted does not match", 2,
				document.size());
		assertEquals("Document name does not match", "TestDocument2", document
				.get(1).getName());
		assertEquals("Document UUID does not match", "document-uuid-2",
				document.get(1).getUuid());
	}

	@Test(expected = IllegalStateException.class)
	public void testPostDocumentsError() throws Exception {
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		ArrayList<Document> postdocument = new ArrayList<Document>();
		for (int i = 1; i < 3; i++) {
			Document doc = new Document();
			doc.setName("TestDocument" + i);
			doc.setContent("TestDocument Content");
			postdocument.add(doc);
		}
		client.postDocuments(collectionname, postdocument);
	}

	@Test(expected = IllegalStateException.class)
	public void testPostDocumentBlankListError() throws Exception {
		String response = ("{`errors`:{`content`:[`can't be blank`]}}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		ArrayList<Document> postdocument = new ArrayList<Document>();
		client.postDocuments(collectionname, postdocument);
	}

	@Test
	public void testDeleteDocument() throws Exception {
		String collectionname = "TestCollection1";
		String documentname = "TestDocument1";
		client.prepareResponse(200, deletedocumentjson);
		DeletedObject deletedobj = client.deleteDocument(collectionname,
				documentname);
		assertEquals("Deleted document name does not match", documentname,
				deletedobj.getName());
		assertEquals("Deleted document flag does not match", true,
				deletedobj.getIsDeleted());
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteDocumentError() throws Exception {
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		String documentname = "TestDocument";
		client.deleteDocument(collectionname, documentname);
	}

	@Test
	public void testGetTask() throws Exception {
		String collectionname = "TestCollection1";
		String taskname = "TestTask1";
		client.prepareResponse(200, gettaskjson);
		Task task = client.getTask(collectionname, taskname);
		assertEquals("Task name does not match", taskname, task.getName());
		assertEquals("Task description does not match", taskname + " Content",
				task.getDescription());
		assertEquals("Task scope does not match", "span", task.getScope());
		assertEquals("Task UUID does not match", "task-uuid-1", task.getUuid());
		assertEquals("Task collection id does not match", "collection-uuid-1",
				task.getCollectionId());
		assertEquals("Task labels size does not match", 2, task.getLabels()
				.size());
		assertEquals("Task label name does not match", "Label2", task
				.getLabels().get(1).getName());
		assertEquals("Task label description does not match", "Label2 Content",
				task.getLabels().get(1).getDescription());
		assertEquals("Task label's task id does not match", "task-uuid-1", task
				.getLabels().get(1).getTaskId());
		assertEquals("Task label's uuid does not match", "label-uuid-2", task
				.getLabels().get(1).getUuid());
		assertEquals("Task feature's size does not match", 2, task
				.getFeatures().size());
		assertEquals("Task feature name does not match", "Feature2", task
				.getFeatures().get(1).getName());
		assertEquals("Task feature task UUID does not match", "task-uuid-1",
				task.getFeatures().get(1).getTaskId());
		assertEquals("Task feature UUID does not match", "feature-uuid-2", task
				.getFeatures().get(1).getUuid());
		assertEquals("Task feature parameters does not match", "2", task
				.getFeatures().get(1).getParameters().get("length"));
		assertNull("Task feature parameters does not match", task.getFeatures()
				.get(0).getParameters().get("length"));
	}

	@Test(expected = IllegalStateException.class)
	public void testGetTaskError() throws Exception {
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		client.getTask(collectionname, taskname);
	}

	@Test
	public void testPutTask() throws Exception {
		String collectionname = "TestCollection1";
		String taskname = "TestTask1";
		client.prepareResponse(200, puttaskjson);
		Task puttask = new Gson().fromJson(puttaskjson, Task.class);
		Task task = client.putTask(collectionname, taskname, puttask);
		assertEquals("Task name does not match", taskname, task.getName());
		assertEquals("Task description does not match", taskname + " Content",
				task.getDescription());
		assertEquals("Task scope does not match", "span", task.getScope());
		assertEquals("Task UUID does not match", "task-uuid-1", task.getUuid());
		assertEquals("Task collection id does not match", "collection-uuid-1",
				task.getCollectionId());
		assertEquals("Task labels size does not match", 2, task.getLabels()
				.size());
		assertEquals("Task label name does not match", "Label2", task
				.getLabels().get(1).getName());
		assertEquals("Task label's task id does not match", "task-uuid-1", task
				.getLabels().get(1).getTaskId());
		assertEquals("Task label's uuid does not match", "label-uuid-2", task
				.getLabels().get(1).getUuid());
		assertEquals("Task feature's size does not match", 0, task
				.getFeatures().size());
	}

	@Test
	public void testPutTaskNameInURL() throws Exception {
		String collectionname = "TestCollection";
		String taskname = "DifferentTestTask";
		client.prepareResponse(200, puttaskjson);
		Task puttask = new Gson().fromJson(puttaskjson, Task.class);
		Task task = client.putTask(collectionname, taskname, puttask);
		assertNotSame("Task name does not match", taskname, task.getName());
	}

	@Test(expected = IllegalStateException.class)
	public void testPutTaskDescriptionError() throws Exception {
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		String response = ("{`errors`:`ERROR: task description not provided`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		Task puttask = new Gson().fromJson(puttaskjson, Task.class);
		client.putTask(collectionname, taskname, puttask);
	}

	@Test(expected = IllegalStateException.class)
	public void testPutTaskScopeError() throws Exception {
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		String response = ("{`errors`:`ERROR: valid task scope not provided`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		Task puttask = new Gson().fromJson(puttaskjson, Task.class);
		client.putTask(collectionname, taskname, puttask);
	}

	@Test
	public void testPostTask() throws Exception {
		String collectionname = "TestCollection1";
		String taskname = "TestTask1";
		client.prepareResponse(200, posttaskjson);
		Task posttask = new Gson().fromJson(posttaskjson, Task.class);
		Task task = client.postTask(collectionname, taskname, posttask);
		assertEquals("Task name does not match", taskname, task.getName());
		assertEquals("Task description does not match", taskname
				+ " New Content", task.getDescription());
		assertEquals("Task scope does not match", "document", task.getScope());
		assertEquals("Task UUID does not match", "task-uuid-1", task.getUuid());
		assertEquals("Task collection id does not match", "collection-uuid-1",
				task.getCollectionId());
		assertEquals("Task labels size does not match", 2, task.getLabels()
				.size());
		assertEquals("Task label name does not match", "Label2", task
				.getLabels().get(1).getName());
		assertEquals("Task label description does not match", "Label2 Content",
				task.getLabels().get(1).getDescription());
		assertEquals("Task label's task id does not match", "task-uuid-1", task
				.getLabels().get(1).getTaskId());
		assertEquals("Task label's uuid does not match", "label-uuid-2", task
				.getLabels().get(1).getUuid());
		assertEquals("Task feature's size does not match", 1, task
				.getFeatures().size());
		assertEquals("Task feature name does not match", "Feature1", task
				.getFeatures().get(0).getName());
		assertEquals("Task feature task UUID does not match", "task-uuid-1",
				task.getFeatures().get(0).getTaskId());
		assertEquals("Task feature UUID does not match", "feature-uuid-1", task
				.getFeatures().get(0).getUuid());
		assertEquals("Task feature parameters does not match", "3", task
				.getFeatures().get(0).getParameters().get("offset"));
		assertNull("Task feature parameters does not match", task.getFeatures()
				.get(0).getParameters().get("length"));
	}

	@Test(expected = IllegalStateException.class)
	public void testPostTaskError() throws Exception {
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		String response = ("{`errors`:`undefined method 'each' for nil:NilClass`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		Task posttask = new Gson().fromJson(posttaskjson, Task.class);
		client.postTask(collectionname, taskname, posttask);
	}

	@Test
	public void testDeleteTask() throws Exception {
		String collectionname = "TestCollection1";
		String taskname = "TestTask1";
		client.prepareResponse(200, deletetaskjson);
		DeletedObject deletedobj = client.deleteTask(collectionname, taskname);
		assertEquals("Deleted task name does not match", taskname,
				deletedobj.getName());
		assertEquals("Deleted task flag does not match", true,
				deletedobj.getIsDeleted());
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteTaskError() throws Exception {
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		client.deleteTask(collectionname, taskname);
	}

	@Test
	public void testPostLabels() throws Exception {
		String collectionname = "TestCollection1";
		String taskname = "TestTask1";
		client.prepareResponse(200, postlabelsjson);
		ArrayList<Label> postlabels = new ArrayList<Label>();
		Task task = client.postLabels(collectionname, taskname, postlabels);
		assertEquals("Task name does not match", taskname, task.getName());
		assertEquals("Task description does not match", taskname + " Content",
				task.getDescription());
		assertEquals("Task scope does not match", "span", task.getScope());
		assertEquals("Task UUID does not match", "task-uuid-1", task.getUuid());
		assertEquals("Task collection id does not match", "collection-uuid-1",
				task.getCollectionId());
		assertEquals("Task labels size does not match", 3, task.getLabels()
				.size());
		assertEquals("Task label name does not match", "Label3", task
				.getLabels().get(2).getName());
		assertEquals("Task label description does not match", "Label3 Content",
				task.getLabels().get(2).getDescription());
		assertEquals("Task label's task id does not match", "task-uuid-1", task
				.getLabels().get(2).getTaskId());
		assertEquals("Task label's uuid does not match", "label-uuid-3", task
				.getLabels().get(2).getUuid());
		assertEquals("Task feature's size does not match", 1, task
				.getFeatures().size());
		assertEquals("Task feature name does not match", "Feature1", task
				.getFeatures().get(0).getName());
		assertEquals("Task feature task UUID does not match", "task-uuid-1",
				task.getFeatures().get(0).getTaskId());
		assertEquals("Task feature UUID does not match", "feature-uuid-1", task
				.getFeatures().get(0).getUuid());
		assertEquals("Task feature parameters does not match", "3", task
				.getFeatures().get(0).getParameters().get("min"));
		assertNull("Task feature parameters does not match", task.getFeatures()
				.get(0).getParameters().get("length"));
	}

	@Test(expected = IllegalStateException.class)
	public void testPostLabelsError() throws Exception {
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		String response = ("{`errors`: `Failed to save label`}").replace("`",
				"\"");
		client.prepareResponse(200, response);
		ArrayList<Label> postlabels = new ArrayList<Label>();
		client.postLabels(collectionname, taskname, postlabels);
	}

	@Test
	public void testDeleteLabel() throws Exception {
		String collectionname = "TestCollection1";
		String taskname = "TestTask1";
		String labelname = "TestLabel1";
		client.prepareResponse(200, deletelabeljson);
		DeletedObject deletedobj = client.deleteLabel(collectionname, taskname,
				labelname);
		assertEquals("Deleted label name does not match", labelname,
				deletedobj.getName());
		assertEquals("Deleted label flag does not match", true,
				deletedobj.getIsDeleted());
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteLabelError() throws Exception {
		String response = ("{`errors`:`undefined method 'destroy' for nil:NilClass`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		String labelname = "TestLabel";
		client.deleteLabel(collectionname, taskname, labelname);
	}

	@Test
	public void testPostAnnotations() throws Exception {
		String collectionname = "TestCollection1";
		String documentname = "TestDocument1";
		client.prepareResponse(200, postannotationsjson);
		ArrayList<Annotation> postannotations = new ArrayList<Annotation>();
		Document document = client.postAnnotations(collectionname,
				documentname, postannotations);
		assertEquals("Document name does not match", documentname,
				document.getName());
		assertEquals("Document content does not match",
				"TestDocument1 Content", document.getContent());
		assertEquals("Document collection id does not match",
				"collection-uuid-1", document.getCollectionId());
		assertEquals("Document UUID does not match", "document-uuid-1",
				document.getUuid());
		assertEquals("Document annotation document id does not match",
				"document-uuid-1", document.getAnnotations().get(0)
						.getDocumentId());
		assertEquals("Document annotation task id does not match",
				"task-uuid-1", document.getAnnotations().get(0).getTaskId());
		assertEquals("Document annotation label id does not match",
				"label-uuid-1", document.getAnnotations().get(0).getLabelId());
		assertEquals("Document annotation UUID does not match",
				"annotation-uuid-1", document.getAnnotations().get(0).getUuid());
		assertEquals("Document UUID does not match", "document-uuid-1",
				document.getUuid());
		assertEquals("Document annotation task name does not match", "Task1",
				document.getAnnotations().get(0).getTask().getName());
		assertEquals("Document annotation task description does not match",
				"Task1 Description", document.getAnnotations().get(0).getTask()
						.getDescription());
		assertEquals("Document annotation task scope does not match", "span",
				document.getAnnotations().get(0).getTask().getScope());
		assertEquals("Document annotation label name does not match", "Label1",
				document.getAnnotations().get(0).getLabel().getName());
		assertEquals("Document annotation label description does not match",
				"Label1 Description", document.getAnnotations().get(0)
						.getLabel().getDescription());
		assertEquals("Document annotation length1 does not match", (long) 4,
				document.getAnnotations().get(0).getLength1());
		assertEquals("Document annotation offset1 does not match", (long) 3,
				document.getAnnotations().get(0).getOffset1());
	}

	@Test(expected = IllegalStateException.class)
	public void testPostAnnotationsNoTaskError() throws Exception {
		String response = ("{`errors`:`{'name':['can't be blank']}`}").replace(
				"`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		String documentname = "TestDocument";
		ArrayList<Annotation> postannotations = new ArrayList<Annotation>();
		client.postAnnotations(collectionname, documentname, postannotations);
	}

	@Test(expected = IllegalStateException.class)
	public void testPostAnnotationsDocNotFoundError() throws Exception {
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		String documentname = "TestDocument";
		ArrayList<Annotation> postannotations = new ArrayList<Annotation>();
		client.postAnnotations(collectionname, documentname, postannotations);
	}

	@Test
	public void testDeleteAnnotation() throws Exception {
		String collectionname = "TestCollection1";
		String documentname = "TestDocument1";
		client.prepareResponse(200, deleteannotationjson);
		DeletedObject deletedobj = client.deleteAnnotation(collectionname,
				documentname, "annotation-uuid-1");
		assertEquals("Deleted annotation id does not match",
				"annotation-uuid-1", deletedobj.getName());
		assertEquals("Deleted annotation flag does not match", true,
				deletedobj.getIsDeleted());
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteAnnotationNotFoundError() throws Exception {
		String response = ("{`errors`:`undefined method 'destroy' for nil:NilClass`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		String documentname = "TestDocument";
		client.deleteAnnotation(collectionname, documentname,
				"annotation-uuid-1");
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteAnnotationDocNotFoundError() throws Exception {
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		String documentname = "TestDocument";
		client.deleteAnnotation(collectionname, documentname,
				"annotation-uuid-1");
	}

	@Test
	public void testGetFeatures() throws Exception {
		client.prepareResponse(200, getfeaturesjson);
		HashMap<String, Feature> featuremap = client.getFeatures();
		assertEquals("Feature map size does not match", 3, featuremap.size());
		assertEquals("Feature map key does not match", true,
				featuremap.containsKey("Feature2"));
		assertEquals("Feature map feature name does not match", "Feature3",
				featuremap.get("Feature3").getName());
		assertEquals("Feature map parameters does not match", true, featuremap
				.get("Feature3").getParameters().containsKey("param2"));
		assertEquals("Feature map parameter count does not match", 2,
				featuremap.get("Feature3").getParameters().size());
		assertEquals("Feature map parameter type does not match", "Integer",
				featuremap.get("Feature3").getParameters().get("param2")
						.getType());
		assertEquals("Feature map parameter default value does not match",
				null, featuremap.get("Feature3").getParameters().get("param3")
						.getDefault());
		assertEquals("Feature map parameter required field does not match",
				false, featuremap.get("Feature3").getParameters().get("param2")
						.getRequired());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetFeaturesError() throws Exception {
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		client.getFeatures();
	}

	@Test
	public void testPostFeatures() throws Exception {
		String collectionname = "TestCollection1";
		String taskname = "TestTask1";
		client.prepareResponse(200, gettaskjson);
		ArrayList<HashMap<String, Object>> features = new ArrayList<HashMap<String, Object>>();
		Task task = client.postFeatures(collectionname, taskname, features);
		assertEquals("Task name does not match", taskname, task.getName());
		assertEquals("Task description does not match", taskname + " Content",
				task.getDescription());
		assertEquals("Task scope does not match", "span", task.getScope());
		assertEquals("Task UUID does not match", "task-uuid-1", task.getUuid());
		assertEquals("Task collection id does not match", "collection-uuid-1",
				task.getCollectionId());
		assertEquals("Task labels size does not match", 2, task.getLabels()
				.size());
		assertEquals("Task label name does not match", "Label2", task
				.getLabels().get(1).getName());
		assertEquals("Task label description does not match", "Label2 Content",
				task.getLabels().get(1).getDescription());
		assertEquals("Task label's task id does not match", "task-uuid-1", task
				.getLabels().get(1).getTaskId());
		assertEquals("Task label's uuid does not match", "label-uuid-2", task
				.getLabels().get(1).getUuid());
		assertEquals("Task feature's size does not match", 2, task
				.getFeatures().size());
		assertEquals("Task feature name does not match", "Feature2", task
				.getFeatures().get(1).getName());
		assertEquals("Task feature task UUID does not match", "task-uuid-1",
				task.getFeatures().get(1).getTaskId());
		assertEquals("Task feature UUID does not match", "feature-uuid-2", task
				.getFeatures().get(1).getUuid());
		assertEquals("Task feature parameters does not match", "2", task
				.getFeatures().get(1).getParameters().get("length"));
		assertNull("Task feature parameters does not match", task.getFeatures()
				.get(0).getParameters().get("length"));
	}

	@Test(expected = IllegalStateException.class)
	public void testPostFeaturesError() throws Exception {
		String response = ("{`errors`:`Unknown feature type: FeatureName`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		client.postFeatures(collectionname, taskname,
				new ArrayList<HashMap<String, Object>>());
	}

	@Test
	public void testDeleteFeature() throws Exception {
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		client.prepareResponse(200, deletefeaturejson);
		DeletedObject deletedobj = client.deleteFeature(collectionname,
				taskname, new HashMap<String, Object>());
		assertEquals("Deleted feature name does not match", "Feature1",
				deletedobj.getName());
		assertEquals("Deleted feature flag does not match", true,
				deletedobj.getIsDeleted());
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteFeatureNotFoundError() throws Exception {
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		client.deleteFeature(collectionname, taskname,
				new HashMap<String, Object>());
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteFeatureFeatureNotFoundError() throws Exception {
		String response = ("{`errors`:`can't convert Symbol into Integer`}")
				.replace("`", "\"");
		client.prepareResponse(200, response);
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		client.deleteFeature(collectionname, taskname,
				new HashMap<String, Object>());
	}

	@Test
	public void testPredictionForText() throws Exception {
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		client.prepareResponse(200, getpredictionsjson);
		ArrayList<HashMap<String, Object>> predictions = client
				.getPredictionsForText(collectionname, taskname,
						new HashMap<String, Object>());
		assertEquals("Number of predicted entities do not match", 2,
				predictions.size());
		assertEquals("Predicted class does not match", "Label1", predictions
				.get(0).get("class"));
		assertEquals("Predicted confidence does not match", 0.9, predictions
				.get(0).get("confidence"));
		assertEquals("Predicted word offset does not match", 11.0, predictions
				.get(0).get("offset"));
		assertEquals("Predicted word length does not match", 7.0, predictions
				.get(0).get("length"));
		assertEquals("Predicted word does not match", "TestText1", predictions
				.get(0).get("text"));
		assertEquals("Predicted description does not match",
				"TestDescription1", predictions.get(0).get("description"));
	}

	@Test
	public void testPredictionForDocument() throws Exception {
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		client.prepareResponse(200, getpredictionsjson);
		ArrayList<HashMap<String, Object>> predictions = client
				.getPredictionsForDocument(collectionname, taskname,
						"TestDocument");
		assertEquals("Number of predicted entities do not match", 2,
				predictions.size());
		assertEquals("Predicted class does not match", "Label3", predictions
				.get(1).get("class"));
		assertEquals("Predicted confidence does not match", 0.75, predictions
				.get(1).get("confidence"));
		assertEquals("Predicted word offset does not match", 35.0, predictions
				.get(1).get("offset"));
		assertEquals("Predicted word length does not match", 10.0, predictions
				.get(1).get("length"));
		assertEquals("Predicted word does not match", "TestText2", predictions
				.get(1).get("text"));
		assertEquals("Predicted description does not match",
				"TestDescription2", predictions.get(1).get("description"));
	}

	@Test(expected = IllegalStateException.class)
	public void testPredictionError() throws Exception {
		String collectionname = "TestCollection";
		String taskname = "TestTask";
		String response = ("{`errors`:`object not found`}").replace("`", "\"");
		client.prepareResponse(200, response);
		client.getPredictionsForDocument(collectionname, taskname, "");
	}

}

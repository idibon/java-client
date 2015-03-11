/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.Date;
import java.util.UUID;
import java.util.Collections;
import org.junit.*;
import javax.json.*;

import java.io.StringReader;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class AnnotationBuilderTest {

    @Test public void testUpdateAndJudgeAssignment() {
        Collection mockCollection = Collection.instance(null, "collection");
        Document mockDocument = mockCollection.document("document");
        Label mockLabel = mockCollection.task("task").label("label");
        UUID mockUUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID mockUserID = UUID.fromString("86753090-0000-0000-0000-000011110000");
        Date now = new Date(System.currentTimeMillis());

        Annotation.DocumentAssignment original =
            new Annotation.DocumentAssignment(mockDocument, mockUUID, true,
                mockLabel, Annotation.Provenance.Crowd, "final", false, true,
                Double.NaN, now, now, null,
                Collections.<JsonObject>emptyList());

        Annotation.Assignment updated = original.update()
            .provenance(Annotation.Provenance.Human)
            .is(AnnotationBuilder.Assignment.Status.Invalid)
            .by(mockUserID).build();

        assertThat(updated.uuid, is(mockUUID));
        assertThat(updated.provenance, is(Annotation.Provenance.Human));
        assertThat(updated.status, is("final"));
        assertThat(updated.trainable, is(false));
        assertThat(updated.userID, is(mockUserID));
        assertThat(updated.label, is(mockLabel));
        assertThat(updated.document, is(mockDocument));

        Annotation.Judgment judgment = original.addJudgment()
            .by(mockUserID).disagreesWithAssignment().build();

        assertThat(judgment.assignment, is((Annotation.Assignment)original));
        assertThat(judgment.disagreement, is(true));
        assertThat(judgment.active, is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidatesSameCollection() {
        Collection mockCollection = Collection.instance(null, "collection");
        Document mockDocument = mockCollection.document("document");
        Collection mockOtherCollection = Collection.instance(null, "collection2");
        Label mockLabel = mockOtherCollection.task("task").label("label");

        AnnotationBuilder.Assignment.on(mockDocument, mockLabel);
    }
}

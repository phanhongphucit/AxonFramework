/*
 * Copyright (c) 2010-2011. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.serializer.xml;

import org.axonframework.domain.StubDomainEvent;
import org.axonframework.serializer.Revision;
import org.axonframework.serializer.SerializedObject;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.*;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

/**
 * @author Allard Buijze
 */
public class XStreamSerializerTest {

    private XStreamSerializer testSubject;
    private static final String SPECIAL__CHAR__STRING = "Special chars: '\"&;\n\\<>/\n\t";

    @Before
    public void setUp() {
        this.testSubject = new XStreamSerializer();
    }

    @Test
    public void testSerializeAndDeserializeDomainEvent() {
        SerializedObject<byte[]> serializedEvent = testSubject.serialize(new TestEvent("Henk"), byte[].class);
        Object actualResult = testSubject.deserialize(serializedEvent);
        assertTrue(actualResult instanceof TestEvent);
        TestEvent actualEvent = (TestEvent) actualResult;
        assertEquals("Henk", actualEvent.getName());
    }

    @Test
    public void testPackageAlias() throws UnsupportedEncodingException {
        testSubject.addPackageAlias("axondomain", "org.axonframework.domain");
        testSubject.addPackageAlias("axon", "org.axonframework");

        SerializedObject<byte[]> serialized = testSubject.serialize(new StubDomainEvent(), byte[].class);
        String asString = new String(serialized.getData(), "UTF-8");
        assertFalse("Package name found in:" + asString, asString.contains("org.axonframework.domain"));
        StubDomainEvent deserialized = (StubDomainEvent) testSubject.deserialize(serialized);
        assertEquals(StubDomainEvent.class, deserialized.getClass());
        assertTrue(asString.contains("axondomain"));
    }

    @Test
    public void testRevisionNumber_FromSerialVersionUid() throws UnsupportedEncodingException {
        SerializedObject<byte[]> serialized = testSubject.serialize(new TestEvent("name"), byte[].class);
        assertNotNull(serialized);
        assertEquals("1", serialized.getType().getRevision());
        assertEquals(TestEvent.class.getName(), serialized.getType().getName());
    }

    @Test
    public void testAlias() throws UnsupportedEncodingException {
        testSubject.addAlias("stub", StubDomainEvent.class);

        SerializedObject<byte[]> serialized = testSubject.serialize(new StubDomainEvent(), byte[].class);
        String asString = new String(serialized.getData(), "UTF-8");
        assertFalse(asString.contains("org.axonframework.domain"));
        assertTrue(asString.contains("<stub"));
        StubDomainEvent deserialized = (StubDomainEvent) testSubject.deserialize(serialized);
        assertEquals(StubDomainEvent.class, deserialized.getClass());
    }

    @Test
    public void testFieldAlias() throws UnsupportedEncodingException {
        testSubject.addFieldAlias("relevantPeriod", TestEvent.class, "period");

        SerializedObject<byte[]> serialized = testSubject.serialize(new TestEvent("hello"), byte[].class);
        String asString = new String(serialized.getData(), "UTF-8");
        assertFalse(asString.contains("period"));
        assertTrue(asString.contains("<relevantPeriod"));
        TestEvent deserialized = (TestEvent) testSubject.deserialize(serialized);
        assertNotNull(deserialized);
    }

    @Test
    public void testRevisionNumber() throws UnsupportedEncodingException {
        SerializedObject<byte[]> serialized = testSubject.serialize(new RevisionSpecifiedEvent(), byte[].class);
        assertNotNull(serialized);
        assertEquals("2", serialized.getType().getRevision());
        assertEquals(RevisionSpecifiedEvent.class.getName(), serialized.getType().getName());
    }

    @Test
    public void testSerializedTypeUsesClassAlias() throws UnsupportedEncodingException {
        testSubject.addAlias("rse", RevisionSpecifiedEvent.class);
        SerializedObject<byte[]> serialized = testSubject.serialize(new RevisionSpecifiedEvent(), byte[].class);
        assertNotNull(serialized);
        assertEquals("2", serialized.getType().getRevision());
        assertEquals("rse", serialized.getType().getName());
    }

    /**
     * Tests the scenario as described in <a href="http://code.google.com/p/axonframework/issues/detail?id=150">issue
     * #150</a>.
     */
    @Test
    public void testSerializeWithSpecialCharacters_WithDom4JUpcasters() {
        SerializedObject<byte[]> serialized = testSubject.serialize(new TestEvent(SPECIAL__CHAR__STRING), byte[].class);
        TestEvent deserialized = (TestEvent) testSubject.deserialize(serialized);
        assertArrayEquals(SPECIAL__CHAR__STRING.getBytes(), deserialized.getName().getBytes());
    }

    /**
     * Tests the scenario as described in <a href="http://code.google.com/p/axonframework/issues/detail?id=150">issue
     * #150</a>.
     */
    @Test
    public void testSerializeWithSpecialCharacters_WithoutUpcasters() {
        SerializedObject<byte[]> serialized = testSubject.serialize(new TestEvent(SPECIAL__CHAR__STRING), byte[].class);
        TestEvent deserialized = (TestEvent) testSubject.deserialize(serialized);
        assertEquals(SPECIAL__CHAR__STRING, deserialized.getName());
    }

    @Revision("2")
    public static class RevisionSpecifiedEvent {
    }

    public static class TestEvent implements Serializable {

        private static final long serialVersionUID = 1L;
        private String name;
        private DateMidnight date;
        private DateTime dateTime;
        private Period period;

        public TestEvent(String name) {
            this.name = name;
            this.date = new DateMidnight();
            this.dateTime = new DateTime();
            this.period = new Period(100);
        }

        public String getName() {
            return name;
        }
    }
}

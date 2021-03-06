package org.axonframework.serializer.bson;

import org.axonframework.serializer.Revision;
import org.axonframework.serializer.SerializedObject;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.*;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Allard Buijze
 */
public class DBObjectXStreamSerializerTest {


    private DBObjectXStreamSerializer testSubject;
    private static final String SPECIAL__CHAR__STRING = "Special chars: '\"&;\n\\<>/\n\t";

    @Before
    public void setUp() {
        this.testSubject = new DBObjectXStreamSerializer();
    }

    @Test
    public void testSerializeAndDeserializeDomainEventWithListOfObjects() {
        List<Object> objectList = new ArrayList<Object>();
        objectList.add("a");
        objectList.add(1L);
        objectList.add("b");
        SerializedObject<String> serializedEvent = testSubject.serialize(new SecondTestEvent("eventName", objectList),
                                                                         String.class);

        Object actualResult = testSubject.deserialize(serializedEvent);
        assertTrue(actualResult instanceof SecondTestEvent);
        SecondTestEvent actualEvent = (SecondTestEvent) actualResult;
        assertEquals(objectList, actualEvent.getStrings());
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
        testSubject.addPackageAlias("test", "org.axonframework.serializer.bson");
        testSubject.addPackageAlias("axon", "org.axonframework");

        SerializedObject<String> serialized = testSubject.serialize(new StubDomainEvent(), String.class);
        String asString = serialized.getData();
        assertFalse("Package name found in:" + asString, asString.contains("org"));
        StubDomainEvent deserialized = (StubDomainEvent) testSubject.deserialize(serialized);
        assertEquals(StubDomainEvent.class, deserialized.getClass());
        assertTrue(asString.contains("test"));
    }

    @Test
    public void testAlias() throws UnsupportedEncodingException {
        testSubject.addAlias("stub", StubDomainEvent.class);

        SerializedObject<byte[]> serialized = testSubject.serialize(new StubDomainEvent(), byte[].class);
        String asString = new String(serialized.getData(), "UTF-8");
        assertFalse(asString.contains("org.axonframework.domain"));
        assertTrue(asString.contains("\"stub"));
        StubDomainEvent deserialized = (StubDomainEvent) testSubject.deserialize(serialized);
        assertEquals(StubDomainEvent.class, deserialized.getClass());
    }

    @Test
    public void testFieldAlias() throws UnsupportedEncodingException {
        testSubject.addFieldAlias("relevantPeriod", TestEvent.class, "period");

        SerializedObject<byte[]> serialized = testSubject.serialize(new TestEvent("hello"), byte[].class);
        String asString = new String(serialized.getData(), "UTF-8");
        assertFalse(asString.contains("period"));
        assertTrue(asString.contains("\"relevantPeriod"));
        TestEvent deserialized = (TestEvent) testSubject.deserialize(serialized);
        assertNotNull(deserialized);
    }

    @Test
    public void testRevisionNumber_FromAnnotation() throws UnsupportedEncodingException {
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
    public void testSerializeWithSpecialCharacters_WithoutUpcasters() {
        SerializedObject<byte[]> serialized = testSubject.serialize(new TestEvent(SPECIAL__CHAR__STRING), byte[].class);
        TestEvent deserialized = (TestEvent) testSubject.deserialize(serialized);
        assertEquals(SPECIAL__CHAR__STRING, deserialized.getName());
    }

    @Revision("2")
    public static class RevisionSpecifiedEvent {

    }

    public static class SecondTestEvent extends TestEvent {

        private List<Object> objects;

        public SecondTestEvent(String name, List<Object> objects) {
            super(name);
            this.objects = new ArrayList<Object>(objects);
        }

        public List<Object> getStrings() {
            return objects;
        }
    }

    public static class TestEvent implements Serializable {

        private static final long serialVersionUID = 1L;
        private String name;
        private List<String> someListOfString;
        private DateMidnight date;
        private DateTime dateTime;
        private Period period;

        public TestEvent(String name) {
            this.name = name;
            this.date = new DateMidnight();
            this.dateTime = new DateTime();
            this.period = new Period(100);
            this.someListOfString = new ArrayList<String>();
            someListOfString.add("First");
            someListOfString.add("Second");
        }

        public String getName() {
            return name;
        }
    }
}

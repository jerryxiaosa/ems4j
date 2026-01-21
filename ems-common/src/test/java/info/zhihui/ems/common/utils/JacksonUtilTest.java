package info.zhihui.ems.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.common.utils.testdata.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class JacksonUtilTest {

    private static final LocalDateTime TEST_DATE_TIME = LocalDateTime.parse("2023-10-10T10:10:10");

    @Test
    void testGetObjectMapper_ShouldReturnConfiguredObjectMapper() {
        ObjectMapper objectMapper = JacksonUtil.getObjectMapper();
        assertNotNull(objectMapper, "ObjectMapper should not be null");
    }

    @Test
    void testToJson_ShouldConvertObjectToJson() {
        String VALID_JSON = "{\"key\":\"test\",\"localDateTime\":\"2023-10-10 10:10:10\"}";
        TestClass testClass = new TestClass("test", TEST_DATE_TIME);
        String json = JacksonUtil.toJson(testClass);
        assertEquals(VALID_JSON, json);
    }

    @Test
    void testFromJson_ShouldConvertJsonToObject() {
        String VALID_JSON = "{\"key\":\"test\",\"localDateTime\":\"2023-10-10 10:10:10\"}";
        TestClass testClass = JacksonUtil.fromJson(VALID_JSON, TestClass.class);
        assertNotNull(testClass, "TestClass object should not be null");
        assertEquals(TEST_DATE_TIME, testClass.getLocalDateTime());
        assertEquals("test", testClass.getKey());
    }

    @Test
    void testFromJson_ShouldThrowExceptionForInvalidJson() {
        String INVALID_JSON = "{\"localDateTime\":\"invalid\"}";

        assertThrows(RuntimeException.class, () -> {
            JacksonUtil.fromJson(INVALID_JSON, TestClass.class);
        });
    }

    @Test
    void testFromJsonWithTypeReference() {
        List<TestClass> expect = new ArrayList<>();
        expect.add(new TestClass("test1", LocalDateTime.parse("2023-10-10T10:10:10")));
        expect.add(new TestClass("test2", LocalDateTime.parse("2023-11-11T10:10:10")));

        String jsonStr = "[{\"key\":\"test1\",\"localDateTime\":\"2023-10-10 10:10:10\"},{\"key\":\"test2\",\"localDateTime\":\"2023-11-11 10:10:10\"}]";
        List<TestClass> testClassList = JacksonUtil.fromJson(jsonStr, new TypeReference<>() {
        });

        assertEquals(expect, testClassList);
    }

    @Test
    void testRawJsonValue() {
        TestJsonValueClass testJsonValueClass = new TestJsonValueClass("{\"key\":\"some value of config string\"}", TEST_DATE_TIME);
        String jsonStr = JacksonUtil.toJson(testJsonValueClass);
        log.info(jsonStr);

        String str = "{\"jsonValue\":{\"key\":\"some value of config string\"},\"localDateTime\":\"2023-10-10 10:10:10\"}";
        TestJsonValueClass testJsonValueClassRes = JacksonUtil.fromJson(str, TestJsonValueClass.class);
        assertEquals(testJsonValueClass, testJsonValueClassRes);
    }

    @Test
    void testNestedObjectJson() {
        TestClass innerObject = new TestClass("inner", TEST_DATE_TIME);
        List<TestClass> innerList = new ArrayList<>();
        innerList.add(new TestClass("list1", TEST_DATE_TIME));
        innerList.add(new TestClass("list2", TEST_DATE_TIME));

        TestNestedClass nestedObject = new TestNestedClass("test", innerObject, innerList, TEST_DATE_TIME);
        String json = JacksonUtil.toJson(nestedObject);

        String expectedJson = "{\"name\":\"test\",\"innerObject\":{\"key\":\"inner\",\"localDateTime\":\"2023-10-10 10:10:10\"},\"innerList\":[{\"key\":\"list1\",\"localDateTime\":\"2023-10-10 10:10:10\"},{\"key\":\"list2\",\"localDateTime\":\"2023-10-10 10:10:10\"}],\"createTime\":\"2023-10-10 10:10:10\"}";
        assertEquals(expectedJson, json);

        TestNestedClass deserializedObject = JacksonUtil.fromJson(json, TestNestedClass.class);
        assertEquals(nestedObject, deserializedObject);
    }

    @Test
    void testLocalTime() {
        TestTimeClass t1 = new TestTimeClass().setType(1).setStart(LocalTime.of(10, 11, 32, 2));
        TestTimeClass t2 = new TestTimeClass().setType(2).setStart(LocalTime.of(15, 22, 48, 33));
        List<TestTimeClass> list = List.of(t1, t2);
        String jsonStr = JacksonUtil.toJson(list);
        log.info("jsonStr: {}", jsonStr);

        List<TestTimeClass> res = JacksonUtil.fromJson(jsonStr, new TypeReference<>() {});
        log.info("res :{}", res);
        assertEquals(t1, res.get(0));
        assertEquals(t2, res.get(1));
    }

    @Test
    void testNestedGenericDeserialization() {
        // 创建测试数据
        TestClass testContent = new TestClass("test-content", TEST_DATE_TIME);
        GenericData<TestClass> genericData = new GenericData<>("data-1", testContent);

        List<GenericData<TestClass>> dataList = new ArrayList<>();
        dataList.add(new GenericData<>("list-1", new TestClass("list-content-1", TEST_DATE_TIME)));
        dataList.add(new GenericData<>("list-2", new TestClass("list-content-2", TEST_DATE_TIME)));

        GenericWrapper<TestClass> wrapper = new GenericWrapper<>("test-wrapper", genericData, dataList);

        // 转换为JSON
        String json = JacksonUtil.toJson(wrapper);
        log.info("嵌套泛型JSON: {}", json);

        // 使用TypeReference反序列化
        GenericWrapper<TestClass> deserializedWrapper = JacksonUtil.fromJson(json,
                new TypeReference<GenericWrapper<TestClass>>() {
                });

        // 验证反序列化结果
        assertNotNull(deserializedWrapper, "反序列化结果不应为空");
        assertEquals("test-wrapper", deserializedWrapper.getName());

        // 验证嵌套泛型对象
        GenericData<TestClass> deserializedData = deserializedWrapper.getData();
        assertNotNull(deserializedData, "嵌套的GenericData不应为空");
        assertEquals("data-1", deserializedData.getId());

        // 验证嵌套泛型对象中的内容
        TestClass deserializedContent = deserializedData.getContent();
        assertNotNull(deserializedContent, "嵌套的TestClass不应为空");
        assertEquals("test-content", deserializedContent.getKey());
        assertEquals(TEST_DATE_TIME, deserializedContent.getLocalDateTime());

        // 验证嵌套泛型列表
        List<GenericData<TestClass>> deserializedList = deserializedWrapper.getDataList();
        assertNotNull(deserializedList, "嵌套的GenericData列表不应为空");
        assertEquals(2, deserializedList.size());

        // 验证列表中的第一个元素
        GenericData<TestClass> firstItem = deserializedList.get(0);
        assertEquals("list-1", firstItem.getId());
        assertEquals("list-content-1", firstItem.getContent().getKey());

        // 验证列表中的第二个元素
        GenericData<TestClass> secondItem = deserializedList.get(1);
        assertEquals("list-2", secondItem.getId());
        assertEquals("list-content-2", secondItem.getContent().getKey());
    }

    @Test
    void testComplexNestedGenericDeserialization() {
        // 创建测试数据 - 模拟类似Notification<Event<T>>的结构
        TestClass eventData1 = new TestClass("event-data-1", TEST_DATE_TIME);
        TestClass eventData2 = new TestClass("event-data-2", TEST_DATE_TIME);

        // 创建事件列表
        List<ComplexGenericWrapper.ComplexEvent<TestClass>> events = new ArrayList<>();
        events.add(new ComplexGenericWrapper.ComplexEvent<>("event-001", "ACCESS", eventData1));
        events.add(new ComplexGenericWrapper.ComplexEvent<>("event-002", "ALARM", eventData2));

        // 创建参数对象
        ComplexGenericWrapper.ComplexParams<TestClass> params =
                new ComplexGenericWrapper.ComplexParams<>("notification", events);

        // 创建完整的包装对象
        ComplexGenericWrapper<TestClass> wrapper = new ComplexGenericWrapper<>("eventNotify", params);

        // 转换为JSON
        String json = JacksonUtil.toJson(wrapper);
        log.info("复杂嵌套泛型JSON: {}", json);

        // 使用TypeReference反序列化
        ComplexGenericWrapper<TestClass> deserializedWrapper = JacksonUtil.fromJson(json,
                new TypeReference<>() {
                });

        // 验证反序列化结果
        assertNotNull(deserializedWrapper, "反序列化结果不应为空");
        assertEquals("eventNotify", deserializedWrapper.getMethod());

        // 验证参数对象
        ComplexGenericWrapper.ComplexParams<TestClass> deserializedParams = deserializedWrapper.getParams();
        assertNotNull(deserializedParams, "参数对象不应为空");
        assertEquals("notification", deserializedParams.getAbility());

        // 验证事件列表
        List<ComplexGenericWrapper.ComplexEvent<TestClass>> deserializedEvents = deserializedParams.getEvents();
        assertNotNull(deserializedEvents, "事件列表不应为空");
        assertEquals(2, deserializedEvents.size());

        // 验证第一个事件
        ComplexGenericWrapper.ComplexEvent<TestClass> firstEvent = deserializedEvents.get(0);
        assertEquals("event-001", firstEvent.getEventId());
        assertEquals("ACCESS", firstEvent.getEventType());
        assertEquals("event-data-1", firstEvent.getData().getKey());
        assertEquals(TEST_DATE_TIME, firstEvent.getData().getLocalDateTime());

        // 验证第二个事件
        ComplexGenericWrapper.ComplexEvent<TestClass> secondEvent = deserializedEvents.get(1);
        assertEquals("event-002", secondEvent.getEventId());
        assertEquals("ALARM", secondEvent.getEventType());
        assertEquals("event-data-2", secondEvent.getData().getKey());
        assertEquals(TEST_DATE_TIME, secondEvent.getData().getLocalDateTime());
    }

}

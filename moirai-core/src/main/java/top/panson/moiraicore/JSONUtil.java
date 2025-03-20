package top.panson.moiraicore;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Collections;
import java.util.List;

/**
 * JSON util.
 */
public class JSONUtil {

    private static final JsonFacade JSON_FACADE = new JacksonHandler();

    public static String toJSONString(Object object) {
        if (object == null) {
            return null;
        }
        return JSON_FACADE.toJSONString(object);
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StringUtil.isBlank(text)) {
            return null;
        }
        return JSON_FACADE.parseObject(text, clazz);
    }

    public static <T> T parseObject(String text, TypeReference<T> valueTypeRef) {
        if (StringUtil.isBlank(text)) {
            return null;
        }
        return JSON_FACADE.parseObject(text, valueTypeRef);
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (StringUtil.isBlank(text)) {
            return Collections.emptyList();
        }
        return JSON_FACADE.parseArray(text, clazz);
    }

    public static boolean isJson(String json) {
        if (StringUtil.isBlank(json)) {
            return false;
        }
        return JSON_FACADE.isJson(json);
    }
}
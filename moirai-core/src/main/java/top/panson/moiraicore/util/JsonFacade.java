package top.panson.moiraicore.util;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Json facade.
 */
public interface JsonFacade {

    /**
     * To JSON string.
     *
     * @param object
     * @return
     */
    String toJSONString(Object object);

    /**
     * Parse object.
     *
     * @param text
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T parseObject(String text, Class<T> clazz);

    /**
     * Parse object.
     *
     * @param text
     * @param valueTypeRef
     * @param <T>
     * @return
     */
    <T> T parseObject(String text, TypeReference<T> valueTypeRef);

    /**
     * Parse array.
     *
     * @param text
     * @param clazz
     * @param <T>
     * @return
     */
    <T> List<T> parseArray(String text, Class<T> clazz);

    /**
     * Validate json.
     *
     * @param text
     * @return
     */
    boolean isJson(String text);
}

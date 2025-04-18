package top.panson.common.toolkit;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Map util.
 */
public class MapUtil {

    /**
     * Null-safe check if the specified Dictionary is empty.
     *
     * <p>Null returns true.
     *
     * @param map the collection to check, may be null
     * @return true if empty or null
     */
    public static boolean isEmpty(Map map) {
        return (map == null || map.isEmpty());
    }

    /**
     * Null-safe check if the specified Dictionary is empty.
     *
     * <p>Null returns true.
     *
     * @param coll the collection to check, may be null
     * @return true if empty or null
     */
    public static boolean isEmpty(Dictionary coll) {
        return (coll == null || coll.isEmpty());
    }

    /**
     * Null-safe check if the specified Dictionary is not empty.
     *
     * <p>Null returns false.
     *
     * @param map the collection to check, may be null
     * @return true if non-null and non-empty
     */
    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }

    /**
     * Null-safe check if the specified Dictionary is not empty.
     *
     * <p>Null returns false.
     *
     * @param coll the collection to check, may be null
     * @return true if non-null and non-empty
     */
    public static boolean isNotEmpty(Dictionary coll) {
        return !isEmpty(coll);
    }

    /**
     * Put into map if value is not null.
     *
     * @param target target map
     * @param key    key
     * @param value  value
     */
    public static void putIfValNoNull(Map target, Object key, Object value) {
        Objects.requireNonNull(key, "key");
        if (value != null) {
            target.put(key, value);
        }
    }

    /**
     * Put into map if value is not empty.
     *
     * @param target target map
     * @param key    key
     * @param value  value
     */
    public static void putIfValNoEmpty(Map target, Object key, Object value) {
        Objects.requireNonNull(key, "key");
        if (value instanceof String) {
            if (StringUtil.isNotEmpty((String) value)) {
                target.put(key, value);
            }
            return;
        }
        if (value instanceof Collection) {
            if (CollectionUtil.isNotEmpty((Collection) value)) {
                target.put(key, value);
            }
            return;
        }
        if (value instanceof Map) {
            if (isNotEmpty((Map) value)) {
                target.put(key, value);
            }
            return;
        }
        if (value instanceof Dictionary) {
            if (isNotEmpty((Dictionary) value)) {
                target.put(key, value);
            }
        }
    }

    /**
     * ComputeIfAbsent lazy load.
     *
     * @param target          target Map data.
     * @param key             map key.
     * @param mappingFunction function which is need to be executed.
     * @param param1          function's parameter value1.
     * @param param2          function's parameter value1.
     * @return
     */
    public static <K, C, V, T> V computeIfAbsent(Map<K, V> target, K key, BiFunction<C, T, V> mappingFunction, C param1,
                                                 T param2) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(mappingFunction, "mappingFunction");
        Objects.requireNonNull(param1, "param1");
        Objects.requireNonNull(param2, "param2");
        V val = target.get(key);
        if (val == null) {
            V ret = mappingFunction.apply(param1, param2);
            target.put(key, ret);
            return ret;
        }
        return val;
    }

    /**
     * Fuzzy matching based on Key.
     *
     * @param sourceMap
     * @param filters
     * @return
     */
    public static List<String> parseMapForFilter(Map<String, ?> sourceMap, String filters) {
        List<String> resultList = new ArrayList<>();
        if (CollectionUtil.isEmpty(sourceMap)) {
            return resultList;
        }
        sourceMap.forEach((key, val) -> {
            if (checkKey(key, filters)) {
                resultList.add(key);
            }
        });
        return resultList;
    }

    /**
     * Match the characters you want to query.
     *
     * @param key
     * @param filters
     * @return
     */
    private static boolean checkKey(String key, String filters) {
        if (key.indexOf(filters) > -1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * remove value, Thread safety depends on whether the Map is a thread-safe Map.
     *
     * @param map         map
     * @param key         key
     * @param removeJudge judge this key can be remove
     * @param <K>         key type
     * @param <V>         value type
     * @return value
     */
    public static <K, V> V removeKey(Map<K, V> map, K key, Predicate<V> removeJudge) {
        return map.computeIfPresent(key, (k, v) -> removeJudge.test(v) ? null : v);
    }
}

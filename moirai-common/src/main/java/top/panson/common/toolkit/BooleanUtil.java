package top.panson.common.toolkit;

import java.util.HashSet;
import java.util.Set;

/**
 * Boolean util.
 */
public class BooleanUtil {

    private static final Set<String> TREE_SET = new HashSet(3);

    static {
        TREE_SET.add("true");
        TREE_SET.add("yes");
        TREE_SET.add("1");
    }

    /**
     * To boolean.
     *
     * @param valueStr
     * @return
     */
    public static boolean toBoolean(String valueStr) {
        if (StringUtil.isNotBlank(valueStr)) {
            valueStr = valueStr.trim().toLowerCase();
            return TREE_SET.contains(valueStr);
        }
        return false;
    }

    /**
     * Is true.
     *
     * @param bool
     * @return
     */
    public static boolean isTrue(Boolean bool) {
        return Boolean.TRUE.equals(bool);
    }
}


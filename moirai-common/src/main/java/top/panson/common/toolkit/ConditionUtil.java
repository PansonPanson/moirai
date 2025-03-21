package top.panson.common.toolkit;

import top.panson.common.function.NoArgsConsumer;

/**
 * Condition util.
 */
public class ConditionUtil {

    public static void condition(boolean condition, NoArgsConsumer trueConsumer, NoArgsConsumer falseConsumer) {
        if (condition) {
            trueConsumer.accept();
        } else {
            falseConsumer.accept();
        }
    }
}
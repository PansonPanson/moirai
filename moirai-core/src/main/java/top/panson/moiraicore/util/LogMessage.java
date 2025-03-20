package top.panson.moiraicore.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Log message.
 *
 * @author Rongzhen Yan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogMessage {

    private Map<String, Object> kvs = new ConcurrentHashMap<>();

    private String msg = "";

    public static LogMessage getInstance() {
        return new LogMessage();
    }

    public LogMessage setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String msg(String msg, Object... args) {
        LogMessage l = new LogMessage();
        l.kvs = this.kvs;
        return l.setMsgString(msg, args);
    }

    public LogMessage setMsg(String msg, Object... args) {
        FormattingTuple ft = MessageFormatter.arrayFormat(msg, args);
        this.msg = ft.getThrowable() == null ? ft.getMessage() : ft.getMessage() + "||_fmt_throw=" + ft.getThrowable();
        return this;
    }

    public String setMsgString(String msg, Object... args) {
        FormattingTuple ft = MessageFormatter.arrayFormat(msg, args);
        this.msg = ft.getThrowable() == null ? ft.getMessage() : ft.getMessage() + "||_fmt_throw=" + ft.getThrowable();
        return toString();
    }

    public LogMessage kv(String k, Object v) {
        this.kvs.put(k, v == null ? "" : v);
        return this;
    }

    public String kv2String(String k, Object v) {
        this.kvs.put(k, v == null ? "" : v);
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (StringUtil.isNotEmpty(msg)) {
            sb.append(msg);
        }
        int tempCount = 0;
        for (Map.Entry<String, Object> kv : kvs.entrySet()) {
            tempCount++;
            Object value = kv.getValue();
            if (value != null) {
                if (value instanceof String && StringUtil.isEmpty((String) value)) {
                    continue;
                }
                sb.append(kv.getKey() + "=").append(kv.getValue());
                if (tempCount != kvs.size()) {
                    sb.append("||");
                }
            }
        }
        return sb.toString();
    }
}

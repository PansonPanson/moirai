package top.panson.moiraicore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class PluginRuntime {

    /**
     * Plugin id
     */
    private final String pluginId;

    /**
     * Runtime info
     */
    private final List<Info> infoList = new ArrayList<>();

    /**
     * Add a runtime info item.
     *
     * @param name  name
     * @param value value
     * @return runtime info item
     */
    public PluginRuntime addInfo(String name, Object value) {
        infoList.add(new Info(name, value));
        return this;
    }

    /**
     * Plugin runtime info.
     */
    @Getter
    @RequiredArgsConstructor
    public static class Info {

        /**
         * Name
         */
        private final String name;

        /**
         * Value
         */
        private final Object value;
    }
}
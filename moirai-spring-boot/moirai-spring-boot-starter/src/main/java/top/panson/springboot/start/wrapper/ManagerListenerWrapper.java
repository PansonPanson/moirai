package top.panson.springboot.start.wrapper;

import top.panson.springboot.start.core.Listener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerListenerWrapper {

    /**
     * Last call md5
     */
    private String lastCallMd5;

    /**
     * Listener
     */
    private Listener listener;
}
package top.panson.core.config;

/**
 * Bootstrap properties interface.
 */
public interface BootstrapPropertiesInterface {

    /**
     * Get enable.
     *
     * @return
     */
    default Boolean getEnable() {
        return null;
    }

    /**
     * Get username.
     *
     * @return
     */
    default String getUsername() {
        return null;
    }

    /**
     * Get password.
     *
     * @return
     */
    default String getPassword() {
        return null;
    }

    /**
     * Get namespace.
     *
     * @return
     */
    default String getNamespace() {
        return null;
    }

    /**
     * Get item id.
     *
     * @return
     */
    default String getItemId() {
        return null;
    }

    /**
     * Get server addr.
     *
     * @return
     */
    default String getServerAddr() {
        return null;
    }

    /**
     * Get banner.
     *
     * @return
     */
    default Boolean getBanner() {
        return null;
    }
}

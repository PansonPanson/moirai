package top.panson.common.enums;

/**
 * Del enum.
 */
public enum DelEnum {

    /**
     * Normal state
     */
    NORMAL("0"),

    /**
     * Deleted state
     */
    DELETE("1");

    private final String statusCode;

    DelEnum(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getCode() {
        return this.statusCode;
    }

    public Integer getIntCode() {
        return Integer.parseInt(this.statusCode);
    }

    @Override
    public String toString() {
        return statusCode;
    }
}

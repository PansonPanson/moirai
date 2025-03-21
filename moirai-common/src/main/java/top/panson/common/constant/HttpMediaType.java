package top.panson.common.constant;

import top.panson.common.toolkit.StringUtil;

/**
 * Http media type.
 *
 * @author Rongzhen Yan
 */
public final class HttpMediaType {

    public static final String APPLICATION_ATOM_XML = "application/atom+xml";

    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded;charset=UTF-8";

    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    public static final String APPLICATION_SVG_XML = "application/svg+xml";

    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";

    public static final String APPLICATION_XML = "application/xml;charset=UTF-8";

    public static final String APPLICATION_JSON = "application/json;charset=UTF-8";

    public static final String MULTIPART_FORM_DATA = "multipart/form-data;charset=UTF-8";

    public static final String TEXT_HTML = "text/html;charset=UTF-8";

    public static final String TEXT_PLAIN = "text/plain;charset=UTF-8";

    private HttpMediaType(String type, String charset) {
        this.type = type;
        this.charset = charset;
    }

    /**
     * content type.
     */
    private final String type;

    /**
     * content type charset.
     */
    private final String charset;

    /**
     * Parse the given String contentType into a {@code MediaType} object.
     *
     * @param contentType mediaType
     * @return MediaType
     */
    public static HttpMediaType valueOf(String contentType) {
        if (StringUtil.isEmpty(contentType)) {
            throw new IllegalArgumentException("MediaType must not be empty");
        }
        String[] values = contentType.split(";");
        String charset = Constants.ENCODE;
        for (String value : values) {
            if (value.startsWith("charset=")) {
                charset = value.substring("charset=".length());
            }
        }
        return new HttpMediaType(values[0], charset);
    }

    /**
     * Use the given contentType and charset to assemble into a {@code MediaType} object.
     *
     * @param contentType contentType
     * @param charset     charset
     * @return MediaType
     */
    public static HttpMediaType valueOf(String contentType, String charset) {
        if (StringUtil.isEmpty(contentType)) {
            throw new IllegalArgumentException("MediaType must not be empty");
        }
        String[] values = contentType.split(";");
        return new HttpMediaType(values[0], StringUtil.isEmpty(charset) ? Constants.ENCODE : charset);
    }

    public String getType() {
        return type;
    }

    public String getCharset() {
        return charset;
    }

    @Override
    public String toString() {
        return type + ";charset=" + charset;
    }
}

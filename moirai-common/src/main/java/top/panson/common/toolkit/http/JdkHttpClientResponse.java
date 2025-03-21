package top.panson.common.toolkit.http;

import top.panson.common.constant.Constants;
import top.panson.common.constant.HttpHeaderConstants;
import top.panson.common.toolkit.IoUtil;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Represents a client-side HTTP response with JDK implementation
 *
 * @author Rongzhen Yan
 */
public class JdkHttpClientResponse implements HttpClientResponse {

    private final HttpURLConnection conn;

    private InputStream responseStream;

    private Header responseHeader;

    private static final String CONTENT_ENCODING = "gzip";

    public JdkHttpClientResponse(HttpURLConnection conn) {
        this.conn = conn;
    }

    @Override
    public Header getHeaders() {
        if (this.responseHeader == null) {
            this.responseHeader = Header.newInstance();
        }
        for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
            this.responseHeader.addOriginalResponseHeader(entry.getKey(), entry.getValue());
        }
        return this.responseHeader;
    }

    @Override
    @SneakyThrows
    public InputStream getBody() {
        Header headers = getHeaders();
        InputStream errorStream = this.conn.getErrorStream();
        this.responseStream = (errorStream != null ? errorStream : this.conn.getInputStream());
        String contentEncoding = headers.getValue(HttpHeaderConstants.CONTENT_ENCODING);
        // Used to process http content_encoding, when content_encoding is GZIP, use GZIPInputStream
        if (CONTENT_ENCODING.equals(contentEncoding)) {
            byte[] bytes = IoUtil.tryDecompress(this.responseStream);
            return new ByteArrayInputStream(bytes);
        }
        return this.responseStream;
    }

    @Override
    @SneakyThrows
    public int getStatusCode() {
        return this.conn.getResponseCode();
    }

    @Override
    @SneakyThrows
    public String getStatusText() {
        return this.conn.getResponseMessage();
    }

    @Override
    public String getBodyString() {
        return IoUtil.toString(this.getBody(), Constants.ENCODE);
    }

    @Override
    public void close() {
        IoUtil.closeQuietly(this.responseStream);
    }
}

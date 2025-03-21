package top.panson.moiraicore.util.net;

import java.io.Closeable;
import java.io.InputStream;

/**
 * Represents a client-side HTTP response.
 *
 * @author mai.jh
 */
public interface HttpClientResponse extends Closeable {

    /**
     * Return the headers of this message.
     *
     * @return a corresponding HttpHeaders object (never {@code null})
     */
    Header getHeaders();

    /**
     * Return the body of the message as an input stream.
     *
     * @return String response body
     */
    InputStream getBody();

    /**
     * Return the HTTP status code.
     *
     * @return the HTTP status as an integer
     */
    int getStatusCode();

    /**
     * Return the HTTP status text of the response.
     *
     * @return the HTTP status text
     */
    String getStatusText();

    /**
     * Return the body As string.
     *
     * @return
     */
    String getBodyString();

    /**
     * close response InputStream.
     */
    @Override
    void close();
}

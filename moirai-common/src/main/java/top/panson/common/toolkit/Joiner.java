package top.panson.common.toolkit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 * reference google guava<br>
 * com.google.common.base.Joiner
 */
public final class Joiner {

    private final String separator;

    private Joiner(String separator) {
        this.separator = Objects.requireNonNull(separator);
    }

    /**
     * Returns a joiner which automatically places {@code separator} between consecutive elements.
     */
    public static Joiner on(String separator) {
        return new Joiner(separator);
    }

    /**
     * Returns a string containing the string representation of each of {@code parts}, using the
     * previously configured separator between each.
     */
    public String join(Object[] parts) {
        return join(Arrays.asList(parts));
    }

    public String join(Iterable<?> parts) {
        return join(parts.iterator());
    }

    /**
     * Returns a string containing the string representation of each of {@code parts}, using the
     * previously configured separator between each.
     */
    public String join(Iterator<?> parts) {
        return appendTo(new StringBuilder(), parts).toString();
    }

    public StringBuilder appendTo(StringBuilder builder, Iterator<?> parts) {
        try {
            appendTo((Appendable) builder, parts);
        } catch (IOException impossible) {
            throw new AssertionError(impossible);
        }
        return builder;
    }

    public <A extends Appendable> A appendTo(A appendable, Iterator<?> parts) throws IOException {
        Objects.requireNonNull(appendable);
        if (parts.hasNext()) {
            appendable.append(toString(parts.next()));
            while (parts.hasNext()) {
                appendable.append(separator);
                appendable.append(toString(parts.next()));
            }
        }
        return appendable;
    }

    CharSequence toString(Object part) {
        Objects.requireNonNull(part);
        return (part instanceof CharSequence) ? (CharSequence) part : part.toString();
    }
}

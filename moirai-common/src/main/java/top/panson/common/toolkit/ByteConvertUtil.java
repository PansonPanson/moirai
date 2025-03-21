package top.panson.common.toolkit;

import java.text.DecimalFormat;

/**
 * Byte conversion tool class
 */
public class ByteConvertUtil {

    public static final int KB_SIZE = 2 << 9;

    public static final int MB_SIZE = 2 << 19;

    public static final int GB_SIZE = 2 << 29;

    public static String getPrintSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        if (size < KB_SIZE) {
            return size + "B";
        } else if (size < MB_SIZE) {
            return df.format((double) size / KB_SIZE) + "KB";
        } else if (size < GB_SIZE) {
            return df.format((double) size / MB_SIZE) + "MB";
        } else {
            return df.format((double) size / GB_SIZE) + "GB";
        }
    }
}
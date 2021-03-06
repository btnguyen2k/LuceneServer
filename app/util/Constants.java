package util;

import java.nio.charset.Charset;

public class Constants {

    public static final Charset UTF8 = Charset.forName("UTF-8");
    public static final byte[] EMPTY = new byte[0];

    public static final String RESPONSE_FIELD_STATUS = "status";
    public static final String RESPONSE_FIELD_MESSAGE = "message";

    public final static int DEFAULT_PAGE_SIZE = 10;

}

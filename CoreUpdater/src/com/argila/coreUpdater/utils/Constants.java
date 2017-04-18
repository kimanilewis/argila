package com.argila.coreUpdater.utils;

/**
 * Constants class.
 *
 * @author lewis Kimani
 */
@SuppressWarnings({"FinalClass", "ClassWithoutLogger"})
public final class Constants {

    /**
     * Private constructor.
     */
    private Constants() {
    }
    /**
     * Maximum size of a log file.
     */
    public static final String MAX_LOG_FILE_SIZE = "500GB";
    /**
     * Maximum number of log files.
     */
    public static final int MAX_NUM_LOGFILES = 4;
    public static final int PING_FAILED = 101;
    public static final int PING_SUCCESS = 100;
    public static final String FAILED_QUERIES_FILE = "FAILED_QUERIES.TXT";
    public static final String FAILED_ACK_FILE = "FAILED_ACK.TXT";
    public static final int UPDATE_RECON_SUCCESS = 102;
    public static final int UPDATE_RECON_FAILED = 103;
    public static final int DAEMON_RUNNING = 1005;
    public static final int DAEMON_INTERRUPTED = 1006;
    public static final int DAEMON_RESUMING = 1007;
    public static final int SINGLE_INSTANCE = 1009;
    public static final int MULTI_INSTANCE = 1010;
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String UPDATE_ID = "update";
    public static final String ACTION_START = "sessionData:start";
    public static final String ACTION_STOP = "sessionData:stop";
    public static final String ACTION_FAILED = "sessionData:failed";
    public static final int RETRY_COUNT = 1;
    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;
}

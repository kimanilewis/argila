package com.cellulant.CoreBroadcastProcessor;

import com.cellulant.CoreBroadcastProcessor.db.MySQL;
import com.cellulant.CoreBroadcastProcessor.utils.Logging;
import com.cellulant.CoreBroadcastProcessor.utils.Props;
import com.cellulant.CoreBroadcastProcessor.utils.Utilities;

import java.sql.SQLException;

/**
 * <p>Java UNIX daemon test file.</p>
 * <p>Title: TestBroadcastProcessor.java</p>
 * <p>Description: This class is used to test the functionality of the Java
 * Daemon.</p>
 * <p>Created on 21 March 2012, 10:48</p>
 * <hr />
 *
 * @since 1.0
 * @author <a href="brian.ngure@cellulant.com">Brian Ngure</a>
 * @version Version 1.0
 */
@SuppressWarnings({"ClassWithoutLogger", "FinalClass"})
public final class TestBroadcastProcessor {
    /**
     * Logger for this application.
     */
    private static Logging log;
    /**
     * Loads system properties.
     */
    private static Props props;
    /**
     * The main run class.
     */
    private static BroadcastProcessor paymentPusher;
    /**
     * Initializes the MySQL connection pool.
     */
    private static MySQL mysql;

    /**
     * Private constructor.
     */
    private TestBroadcastProcessor() {
    }

    /**
     * Test init().
     */
    public static void init() {
        try {
            props = new Props();
            log = new Logging(props);
            log.info("Host: " + props.getDbHost() + " Port: "
                    + props.getDbPort() + " Name: " + props.getDbName()
                    + " Username: " + props.getDbUserName() + " Password: **** "
                    + "Pool: " + props.getDbPoolName());
            mysql = new MySQL(props.getDbHost(), props.getDbPort(),
                    props.getDbName(), props.getDbUserName(),
                    props.getDbPassword(), props.getDbPoolName(),
                    props.getDbMaxConnections());
            log.info(" Initializing Push Payment Status daemon...");

            paymentPusher = new BroadcastProcessor(props, log, mysql);
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | SQLException ex) {
            log.fatal("Exception caught during initialization: " + ex);
            System.exit(-1);
        }
    }

    /**
     * Main method.
     *
     * @param args command line arguments
     */
    @SuppressWarnings({"SleepWhileInLoop", "UseOfSystemOutOrSystemErr"})
    public static void main(final String[] args) {
//        System.exit(-1);

        init();

        log.info("Initialization Complete");
        while (true) {

            log.info("");

            try {
                paymentPusher.executeTasks();
            } catch (Exception ex) {
                log.fatal(Utilities.getLogPreString() + "PaymentPusherEntry | "
                        + "Error occured: " + ex.getMessage());
            }

            try {
                Thread.sleep(props.getSleepTime());
            } catch (InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}

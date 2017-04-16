        /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.argila.pc;

import com.argila.pc.utils.Logging;
import com.argila.pc.utils.Props;
import com.argila.pc.db.MySQL;
import java.sql.SQLException;

/**
 * 2
 *
 * @author lewie
 */
public class TestPC {

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
    private static FetchAccounts fetchAccounts;
    /**
     * Initializes the MySQL connection pool.
     */
    private static MySQL mysql;

    /**
     * Private constructor.
     */
    private TestPC() {
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
            log.info(" Initializing bill fetcher daemon...");

            fetchAccounts = new FetchAccounts(props, log, mysql);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
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
        init();

        log.info("Initialization Complete");
        while (true) {

          //  log.info("oooooooooooooooooooooooooooooo");

            try {
                fetchAccounts.runDaemon();
                Thread.sleep(props.getSleepTime());
            } catch (InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}

        /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.argila.pc;

import com.argila.pc.utils.Logging;
import com.argila.pc.utils.Props;
import com.argila.pc.db.MySQL;

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
    private static ServerPool serverPool;
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
        ServerPool server = new ServerPool(9000,props,log);
        new Thread(server).start();
        props = new Props();
        log = new Logging(props);
        try {
            Thread.sleep(props.getSleepTime());
        } catch (InterruptedException ex) {

            log.fatal("Exception caught during initialization: " + ex);
            System.exit(-1);
            server.stop();
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

            log.info(" Started ....");

            try {
                 log.info(" Started ..123..");
                serverPool.run();
                log.info(" Started ..456..");
            } catch (Exception ex) {
                log.fatal("Error occured: " + ex.getMessage());
            }

            try {
                
                Thread.sleep(props.getSleepTime());
            } catch (InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}

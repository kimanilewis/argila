/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.argila.pc;

import com.argila.pc.utils.Logging;
import com.argila.pc.utils.Props;
import com.argila.pc.utils.CoreUtils;
import com.argila.pc.db.MySQL;
import java.sql.SQLException;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

/**
 * 
 * @author Lewis Kimani < kimanilewi@gmail.com>
 */
public final class UpdatePCDaemon implements Daemon, Runnable {

    /**
     * The worker thread that does all the work.
     */
    private Thread worker;
    /**
     * Flag to check if the worker thread should run.
     */
    private boolean working = false;
    /**
     * Logger for this application.
     */
    private Logging log;
    /**
     * The main run class.
     */
    private FetchAccounts fetchAccounts;
    /**
     * Properties instance.
     */
    private Props props;
    /**
     * Initializes the MySQL connection pool.
     */
    private MySQL mysql;

    /**
     * Used to read configuration files, create a trace file, create
     * ServerSockets, Threads, etc.
     *
     * @param context the DaemonContext
     *
     * @throws DaemonInitException on error
     */
    @Override
    public void init(final DaemonContext context) throws DaemonInitException {
        try {
            worker = new Thread(this);
            props = new Props();
            log = new Logging(props);
            log.info(CoreUtils.getLogPreString() + "init() |"
                    + " Initializing PC Updater Application...");

            mysql = new MySQL(props.getDbHost(), props.getDbPort(),
                    props.getDbName(), props.getDbUserName(),
                    props.getDbPassword(), props.getDbPoolName(),
                    props.getDbMaxConnections());

            fetchAccounts = new FetchAccounts(props, log, mysql);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            log.fatal(CoreUtils.getLogPreString() + "init() | "
                    + "Exception caught during initialization: "
                    + ex.getMessage());
            System.exit(1);
        } catch (SQLException ex) {
            log.fatal(CoreUtils.getLogPreString() + "init() | SQL Exception "
                    + "Thrown = " + ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Starts the daemon.
     */
    @Override
    public void start() {
        working = true;
        worker.start();
        log.info("Starting PC Updater Application....");
    }

    /**
     * Stops the daemon. Informs the thread to terminate the run().
     */
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void stop() {
        log.info("Stopping PC Updater Application....");

        working = false;

        while (!fetchAccounts.getIsCurrentPoolShutDown()) {
            log.info("Waiting for current thread pool to complete tasks...");
            try {
                Thread.sleep(props.getSleepTime());
            } catch (InterruptedException ex) {
                log.error("InterruptedException occured while waiting for "
                        + "tasks to complete: " + ex.getMessage());
            }
        }

        log.info("Completed tasks in current thread pool, continuing daemon "
                + "shutdown");

        log.info("BillFetcher Daemon stopped.");
    }

    /**
     * Destroys the daemon. Destroys any object created in init().
     */
    @Override
    public void destroy() {
        log.info("Destroying PC Updater Application....");
        log.info("Exiting...");
    }

    /**
     * Runs the thread. The application runs inside an "infinite" loop.
     */
    @Override
    @SuppressWarnings({"SleepWhileHoldingLock", "SleepWhileInLoop"})
    public void run() {
        while (working) {

            try {
                fetchAccounts.runDaemon();
            } catch (Exception ex) {
                log.fatal("Error occured: " + ex.getMessage());
            }

            try {
                Thread.sleep(props.getSleepTime());
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
            }
        }
    }
}

package com.cellulant.CoreBroadcastProcessor;

import com.cellulant.CoreBroadcastProcessor.db.MySQL;
import com.cellulant.CoreBroadcastProcessor.utils.Logging;
import com.cellulant.CoreBroadcastProcessor.utils.Props;
import com.cellulant.CoreBroadcastProcessor.utils.Utilities;

import java.sql.SQLException;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

/**
 * <p>
 * BroadcastProcessor daemon.</p>
 * <p>
 * Title: broadcastPusher.java</p>
 * <p>
 * Description: This class implements the following methods to enable the Java
 * Daemon:<br /><br />
 * <ul>
 * <li>void init(String[] arguments): Here open configuration files, create a
 * trace file, create ServerSockets, Threads, etc</li>
 * <li>void start(): Start the Thread, accept incoming connections, etc</li>
 * <li>void stop(): Inform the Thread to terminate the run(), close the
 * ServerSockets, db connections, etc</li>
 * <li>void destroy(): Destroy any object created in init()</li>
 * </ul>
 * </p>
 * <p>
 * Created on 16 August 2010, 23:48</p>
 * <p>
 * Copyright: Copyright (c) 2012,
 * <a href="mailto:brian@pixie.co.ke">Brian Ngure</a></p>
 * <hr />
 *
 * @since 1.0
 * @author <a href="brian@pixie.co.ke">Brian Ngure</a>
 * @version Version 1.0
 */
@SuppressWarnings({"ClassWithoutLogger", "FinalClass"})
public final class BroadcastProcessorEntry implements Daemon, Runnable {

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
    private BroadcastProcessor broadcastPusher;
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
            log.info(Utilities.getLogPreString() + "init() |"
                    + " Initializing Broadcast Processor daemon...");

            mysql = new MySQL(props.getDbHost(), props.getDbPort(),
                    props.getDbName(), props.getDbUserName(),
                    props.getDbPassword(), props.getDbPoolName(),
                    props.getDbMaxConnections());

            broadcastPusher = new BroadcastProcessor(props, log, mysql);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            log.fatal(Utilities.getLogPreString() + "init() | "
                    + "Exception caught during initialization: "
                    + ex.getMessage());
            System.exit(1);
        } catch (SQLException ex) {
            log.fatal(Utilities.getLogPreString() + "init() | SQL Exception "
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
        log.info("Starting Broadcast Processor daemon...");
    }

    /**
     * Stops the daemon. Informs the thread to terminate the run().
     */
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void stop() {
        log.info("Stopping Broadcast Processor daemon...");

        working = false;

        while (!broadcastPusher.getIsCurrentPoolShutDown()) {
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

        log.info("Broadcast Processor Daemon stopped.");
    }

    /**
     * Destroys the daemon. Destroys any object created in init().
     */
    @Override
    public void destroy() {
        log.info("Destroying Broadcast Processor daemon...");
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
                broadcastPusher.executeTasks();
            } catch (Exception ex) {
                log.fatal(Utilities.getLogPreString() + "BroadcastProcessorEntry | "
                        + "Run | Error occured: " + ex.getMessage());
            }

            try {
                Thread.sleep(props.getSleepTime());
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
            }
        }
    }
}

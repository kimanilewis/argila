/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.argila.pc;

import com.argila.pc.db.MySQL;
import com.argila.pc.utils.Constants;
import com.argila.pc.utils.CoreUtils;
import com.argila.pc.utils.Logging;
import com.argila.pc.utils.Props;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author lewie
 */
@SuppressWarnings({"ClassWithoutLogger", "FinalClass"})
public final class ClientTask {

    /**
     * System properties class instance.
     */
    private final Props props;
    /**
     * Log class instance.
     */
    private final Logging logging;
    /**
     * Flag to check if current pool is completed.
     */
    private transient boolean isCurrentPoolShutDown = false;
    /**
     * The MySQL data source.
     */
    private final transient MySQL mysql;
    /**
     * server socket
     */
    protected ServerSocket serverSocket = null;

    /**
     * Constructor. Checks for any errors while loading system properties,
     * creates the thread pool and resets partially processed records.
     *
     * @param log the logger class used to log information and errors
     * @param properties the loaded system properties
     * @param mySQL the MySQL connection object
     */
    public ClientTask(final Props properties, final Logging log,
            final MySQL mySQL) {
        props = properties;
        logging = log;
        mysql = mySQL;

        // Get the list of errors found when loading system properties
        List<String> loadErrors = properties.getLoadErrors();
        int sz = loadErrors.size();

        if (sz > 0) {
            log.info(CoreUtils.getLogPreString() + "There were exactly "
                    + sz + " error(s) during the load operation...");

            for (String err : loadErrors) {
                log.fatal(CoreUtils.getLogPreString() + err);
            }

            log.info(CoreUtils.getLogPreString() + "Unable to start daemon "
                    + "because " + sz + " error(s) occured during load.");
            System.exit(1);
        } else {
            log.info(CoreUtils.getLogPreString()
                    + "All required properties were loaded successfully");
        }
    }

    private void openServerSocket() {
        try {
            logging.info(CoreUtils.getLogPreString()
                    + "Opening socket ..."
                    + "\n ");
            serverSocket = new ServerSocket(9000);
        } catch (IOException | RuntimeException e) {
            logging.info(CoreUtils.getLogPreString()
                    + "Unable to open socket ..."
                    + "\n ");
        }
    }

    /**
     * Method <i>getTasks</i> gets a bucket of unprocessed tasks and processes
     * them.
     */
    public synchronized void TCPPool() {
        ExecutorService executor;

        while (!getIsCurrentPoolShutDown()) {
            logging.info(CoreUtils.getLogPreString()
                    + "Socket opened .. opened connection ..."
                    + "\n ");
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException | RuntimeException e) {
                if (getIsCurrentPoolShutDown()) {
                    logging.info(CoreUtils.getLogPreString()
                            + "No session accounts records were fetched "
                            + "\n Server Stopped...");
                    System.out.println("Server Stopped.");
                    break;
                }
                logging.info(CoreUtils.getLogPreString()
                        + "Error accepting client connection.. closing connection ..."
                        + "\n ");
            }
             
        }
        System.out.println("Server Stopped.");

        isCurrentPoolShutDown = false;

        executor = Executors
                .newFixedThreadPool(props.getNumOfChildren());



        /*
         * This will make the executor accept no new threads and
         * finish all existing threads in the queue.
         */
        shutdownAndAwaitTermination(executor);

        isCurrentPoolShutDown = true;

    }
 
    /**
     * Creates a simple Runnable that holds a Job object thats worked on by the
     * child threads.
     *
     * @param accounts the accounts available data
     *
     * @return a new customer accounts processing Job task
     */
    private synchronized Runnable accountsProcessingTask(
            final AccountsData accounts) {
        logging.info(CoreUtils.getLogPreString() + "accountsProcessingTask() | "
                + "Creating a task for record with clientProfileAccount : "
                + accounts.getCustomerProfileAccountID());
        return null;
    }

    /**
     * Sleep for an amount of time.
     *
     * @param t the time to sleep
     */
    private void doWait(final long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException ex) {
            logging.info(CoreUtils.getLogPreString() + " doWait() | "
                    + "Thread could not sleep for the specified time");
        }
    }

    /**
     * Process Records.
     */
    public void runDaemon() {
        // First check for any failed post profile ACKs or debits
        if (this.rollbackSystem() > 0) {
            return;
        }
        openServerSocket();
        TCPPool();

    }

    /**
     * Update successful transactions on Hub that were not updated.
     */
    private int rollbackSystem() {
        String logPreString = CoreUtils.getLogPreString()
                + "rollbackSystem() | -1 | ";

        List<String> failedQueries = checkForFailedQueries(
                Constants.FAILED_QUERIES_FILE);

        int failures = failedQueries.size();
        int recon = 0;

        if (failures > 0) {
            logging.info(logPreString + "I found " + failures
                    + " failed update queries in file: "
                    + Constants.FAILED_QUERIES_FILE
                    + ", rolling back transactions...");

            do {
                String reconQuery = failedQueries.get(recon);
                doRecon(reconQuery, Constants.RETRY_COUNT);
                recon++;
            } while (recon != failures);

            logging.info(logPreString + "I have finished performing rollback");
        }
        return failures;
    }

    /**
     * Loads a file with selected queries and re-runs them internally.
     *
     * @param file the file to check for failed queries
     *
     * @return the found failed queries
     */
    private List<String> checkForFailedQueries(final String file) {
        String logPreString = CoreUtils.getLogPreString()
                + "checkForFailedQueries() | -1 | ";

        List<String> queries = new ArrayList<>(0);

        try (FileInputStream fin = new FileInputStream(file);
                DataInputStream in = new DataInputStream(fin);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in))) {
            String data;
            while ((data = br.readLine()) != null) {
                if (!queries.contains(data)) {
                    queries.add(data);
                }
            }
        } catch (FileNotFoundException e) {
            logging.info(logPreString
                    + " FAILED_QUERIES.TXT File not found: "
                    + e.getMessage());
        } catch (IOException e) {
            logging.info(logPreString
                    + " The are no queries since "
                    + "the FAILED_QUERIES.TXT does not exist: "
                    + e.getMessage());
        }

        return queries;
    }

    /**
     * This function determines how the queries will be re-executed i.e. whether
     * SELECT or UPDATE.
     *
     * @param query the query to re-execute
     * @param tries the number of times to retry
     */
    private void doRecon(final String query, final int tries) {
        String logPreString = CoreUtils.getLogPreString() + "doRecon() | -1 | ";
        int maxRetry = props.getMaxFailedQueryRetries();

        if (query.toLowerCase().startsWith(Constants.UPDATE_ID)) {
            int qstate = runUpdateRecon(query);

            if (qstate == Constants.UPDATE_RECON_SUCCESS) {
                logging.info(logPreString + "Re-executed this query: "
                        + query + " successfully, deleting it from file...");

                deleteQuery(Constants.FAILED_QUERIES_FILE, query);
            } else if (qstate == Constants.UPDATE_RECON_FAILED) {
                logging.info(logPreString + "Failed to re-execute failed "
                        + "query: " + query + " [Try " + tries + " out of "
                        + maxRetry + "]");

                int currTry = tries + 1;

                if (tries >= maxRetry) {
                    logging.info(logPreString
                            + "Tried to re-execute failed query "
                            + props.getMaxFailedQueryRetries()
                            + " times but still failed, exiting...");
                } else {
                    logging.info(logPreString + "Retrying in "
                            + (props.getSleepTime() / 1000) + " sec(s) ");

                    doWait(props.getSleepTime());
                    doRecon(query, currTry);
                }
            }
        }
    }

    /**
     * Re-executes the specified query.
     *
     * @param query the query to run
     *
     * @return the status of the update (success or fail)
     */
    private int runUpdateRecon(final String query) {
        String logPreString = CoreUtils.getLogPreString()
                + "runUpdateRecon() | -1 | ";

        int result;

        try (Connection conn = mysql.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);

            logging.info(logPreString + "I have just successfully "
                    + "re-executed this failed query: " + query);

            result = Constants.UPDATE_RECON_SUCCESS;
        } catch (SQLException e) {
            logging.error(logPreString + "SQLException: " + e.getMessage());
            result = Constants.UPDATE_RECON_FAILED;
        }

        return result;
    }

    /**
     * Delete a query from the failed queries file after a successful
     * reconciliation.
     *
     * @param queryfile the failed queries file
     * @param query the query to delete
     */
    private void deleteQuery(final String queryfile, final String query) {
        String logPreString = CoreUtils.getLogPreString()
                + "deleteQuery() | -1 | ";

        ArrayList<String> queries = new ArrayList<>(0);

        try (FileInputStream fin = new FileInputStream(queryfile);
                DataInputStream in = new DataInputStream(fin);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in)); PrintWriter pout = new PrintWriter(
                        new FileOutputStream(queryfile, false))) {
            String data;
            while ((data = br.readLine()) != null) {
                queries.add(data);
            }

            // Find a match to the query
            logging.info(logPreString + "About to remove this query: " + query
                    + " from file: " + queryfile);

            if (queries.contains(query)) {
                queries.remove(query);
                logging.info(logPreString + "I have removed this query: "
                        + query + " from file: " + queryfile);
            }

            // Now save the file
            for (String newQueries : queries) {
                pout.println(newQueries);
            }
        } catch (IOException e) {
            /**
             * If we fail to open it then, the file has not been created yet'
             * This is good because it means that no error(s) have been
             * experienced yet
             */
            logging.info(logPreString
                    + " The are no queries since"
                    + " the FAILED_QUERIES.TXT does not exist: "
                    + e.getMessage());
        }
    }

    /**
     * The following method shuts down an ExecutorService in two phases, first
     * by calling shutdown to reject incoming tasks, and then calling
     * shutdownNow, if necessary, to cancel any lingering tasks (after 6
     * minutes).
     *
     * @param pool the executor service pool
     */
    private void shutdownAndAwaitTermination(final ExecutorService pool) {
        logging.info(CoreUtils.getLogPreString()
                + "shutdownAndAwaitTermination() |"
                + "Executor pool waiting for tasks to complete");
        pool.shutdown(); // Disable new tasks from being submitted

        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(6, TimeUnit.MINUTES)) {
                logging.error(CoreUtils.getLogPreString()
                        + "shutdownAndAwaitTermination() |"
                        + "Executor pool  terminated with tasks "
                        + "unfinished. Unfinished tasks will be retried.");
                pool.shutdownNow(); // Cancel currently executing tasks

                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(6, TimeUnit.MINUTES)) {
                    logging.error(CoreUtils.getLogPreString()
                            + "shutdownAndAwaitTermination() |"
                            + "Executor pool terminated with tasks "
                            + "unfinished. Unfinished tasks will be retried.");
                }
            } else {
                logging.info(CoreUtils.getLogPreString()
                        + "shutdownAndAwaitTermination() |"
                        + "Executor pool completed all tasks and has shut "
                        + "down normally");
            }
        } catch (InterruptedException ie) {
            logging.error(CoreUtils.getLogPreString()
                    + "shutdownAndAwaitTermination() |"
                    + "Executor pool shutdown error: " + ie.getMessage());
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();

            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }

        isCurrentPoolShutDown = true;
    }

    /**
     * Gets whether the current pool has been shut down.
     *
     * @return whether the current pool has been shut down
     */
    public boolean getIsCurrentPoolShutDown() {
        return isCurrentPoolShutDown;
    }
}

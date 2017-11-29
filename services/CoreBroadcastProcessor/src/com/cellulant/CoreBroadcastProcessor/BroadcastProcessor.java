package com.cellulant.CoreBroadcastProcessor;

import com.cellulant.CoreBroadcastProcessor.db.MySQL;
import com.cellulant.CoreBroadcastProcessor.utils.Logging;
import com.cellulant.CoreBroadcastProcessor.utils.PaymentPusherConstants;
import com.cellulant.CoreBroadcastProcessor.utils.Props;
import com.cellulant.CoreBroadcastProcessor.utils.Utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

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
import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Processor class.
 *
 * @author kim
 */
@SuppressWarnings({"ClassWithoutLogger", "FinalClass"})
public final class BroadcastProcessor {

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
     * Constructor. Checks for any errors while loading system properties,
     * creates the thread pool and resets partially processed records.
     *
     * @param log the logger class used to log information and errors
     * @param properties the loaded system properties
     * @param mySQL the MySQL connection object
     */
    public BroadcastProcessor(final Props properties, final Logging log,
            final MySQL mySQL) {
        props = properties;
        logging = log;
        mysql = mySQL;

        // Get the list of errors found when loading system properties
        List<String> loadErrors = properties.getLoadErrors();
        int sz = loadErrors.size();

        if (sz > 0) {
            log.info(Utilities.getLogPreString() + "There were exactly "
                    + sz + " error(s) during the load operation...");

            for (String err : loadErrors) {
                log.fatal(Utilities.getLogPreString() + err);
            }

            log.info(Utilities.getLogPreString() + "Unable to start daemon "
                    + "because " + sz + " error(s) occured during load.");
            System.exit(1);
        } else {
            log.info(Utilities.getLogPreString()
                    + "All required properties were loaded successfully");
        }
    }

    /**
     * Method <i>getTasks</i> gets a bucket of unprocessed tasks and processes
     * them.
     */
    public synchronized void executeTasks() {
        ExecutorService executor;
        String query;
        String logQuery = "";
        ResultSet rs = null;
        PreparedStatement stmt = null;

        String preLogString = Utilities.getLogPreString() + "executeTasks() | ";
        try (Connection conn = mysql.getConnection()) {

            logging.info(preLogString + "Fetching records to be processed ... ");

            /**
             * Query to fetch the payments.
             *
             * Payment that can be fetched have to meet the following
             * expectations:
             *
             * General: - Push status is in unprocessed status - Time of fetch
             * should be more than the next send time - Should be for the
             * specific service being fetched
             *
             * New payments status (139): - The date the payment created should
             * be older than the pre-configured expiry period - Number of sends
             * is not more than the pre-configured value
             *
             * Payments being reprocessed: - The date the payment information
             * was updated created should be older than the pre-configured
             * expiry period - The updatedBy field should have the user id of
             * the person that updated it
             */
            query = "SELECT MSISDN, account_number,account_name, account_balance, "
                    + "account_status ,time_available ,created_at,modified_at "
                    + "FROM user "
                    + "WHERE account_status  = ? AND beepTransactionID IS NULL "
                    + "AND dateCreated >= DATE_SUB(NOW(), INTERVAL ? MINUTE) "
                    + "ORDER BY rewardRecipientID ASC LIMIT ? ";

            stmt = conn.prepareStatement(query);
            stmt.setInt(1, props.getUnprocessedStatus());
            stmt.setInt(2, props.getPostPaymentPeriod());
            stmt.setInt(3, props.getBucketSize());

            String[] params = {
                String.valueOf(props.getUnprocessedStatus()),
                String.valueOf(props.getPostPaymentPeriod()),
                String.valueOf(props.getBucketSize())
            };

            logQuery = Utilities.prepareSqlString(query, params, 0);

            rs = stmt.executeQuery();
            int size = 0;
            if (rs.last()) {
                size = rs.getRow();
                rs.beforeFirst();
            }
            if (size > 0) {
                logging.info(preLogString + "Fetched " + size
                        + " record(s)...");
                isCurrentPoolShutDown = false;

                if (size <= props.getNumOfChildren()) {
                    executor = Executors.newFixedThreadPool(size);
                } else {
                    executor = Executors
                            .newFixedThreadPool(props.getNumOfChildren());
                }

                while (rs.next()) {
                    /**
                     * create a new payment data object to be used for
                     * processing
                     */
                    BroadcastData paymentData = new BroadcastData();
                    /**
                     * set all the values required for payment processing within
                     * the application and when they are pusher out to the
                     * wrappers
                     */

                    paymentData.setServiceID(rs.getInt("HUBServiceID"));
                    paymentData.setRewardRecipientID(rs
                            .getInt("rewardRecipientID"));
                    paymentData.setNumberOfSends(rs
                            .getInt("numberOfSends"));
                    paymentData.setAmount(rs
                            .getString("rewardAmount"));
                    paymentData.setMSISDN(rs
                            .getString("MSISDN"));
                    paymentData.setAccountNumber(rs
                            .getString("accountNumber"));
                    paymentData.setCurrencyCode(rs
                            .getString("currencyCode"));
                    paymentData.setDateCreated(rs
                            .getString("dateCreated"));
                    // Create a runnable task and submit it
                    Runnable task = createTask(paymentData);
                    //execute the tast to be pushed out
                    executor.execute(task);
                }

                /**
                 * close all open resources
                 */
                rs.close();
                stmt.close();

                /*
                 * This will make the executor accept no new threads and
                 * finish all existing threads in the queue.
                 */
                shutdownAndAwaitTermination(executor);
            } else {
                logging.info(preLogString + "No records were fetched "
                        + "from the DB for processing...");
            }
        } catch (SQLException e) {
            logging.error(preLogString + "Failed to fetch Bucket: "
                    + "Select Query: " + logQuery
                    + " Error Message :" + e.getMessage());
        } finally {
            isCurrentPoolShutDown = true;
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlex) {
                    logging.error(preLogString + "Error closing statement: "
                            + sqlex.getMessage());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlex) {
                    logging.error(preLogString
                            + "Failed to close statement: "
                            + sqlex.getMessage());
                }
            }
        }

    }

    /**
     * Creates a simple Runnable that holds a Job object thats worked on by the
     * child threads.
     *
     * @param paymentData the payment data
     *
     * @return a new BroadcastProcessorJob task
     */
    private synchronized Runnable createTask(final BroadcastData paymentData) {
        logging.info(Utilities.getLogPreString() + "createTask() | "
                + "Creating a task for message with Reward Recipient ID: "
                + paymentData.getRewardRecipientID());

        return new BroadcastProcessorJob(logging, props, mysql, paymentData);
    }

    /**
     * Update successful transactions that were not updated.
     *
     * @param serviceID the service ID
     * @param serviceCode the service code
     *
     * @return the status
     */
    private int rollbackSystem(final int serviceID, final String serviceCode) {
        String fileName = serviceCode + "_" + serviceID + "_"
                + PaymentPusherConstants.FAILED_QUERIES_FILE;

        List<String> failedQueries = checkForFailedQueries(
                fileName);
        int failures = failedQueries.size();
        int recon = 0;

        if (failures > 0) {
            logging.info(Utilities.getLogPreString()
                    + "rollbackSystem() | I found " + failures
                    + " failed update queries in file: "
                    + fileName + ", rolling back transactions...");

            do {
                String reconQuery = failedQueries.get(recon);
                doRecon(reconQuery, PaymentPusherConstants.RETRY_COUNT,
                        fileName);

                recon++;
            } while (recon != failures);

            logging.info(Utilities.getLogPreString() + "rollbackSystem() | "
                    + "I have finished performing rollback...");
        }

        return failures;
    }

    /**
     * Loads a file with selected queries and re-runs them internally.
     *
     * @param file the file to check for failed queries
     *
     * @return the failed queries
     */
    @SuppressWarnings("NestedAssignment")
    private List<String> checkForFailedQueries(final String file) {
        List<String> queries = new ArrayList<>(0);

        try (FileInputStream fin = new FileInputStream(file);
                DataInputStream in = new DataInputStream(fin);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in))) {
            /*
             * If we fail to open the file, then the file has not been created
             * yet. This is good because it means that there is no error.
             */
            if (new File(file).exists()) {
                String data;
                while ((data = br.readLine()) != null) {
                    if (!queries.contains(data)) {
                        queries.add(data);
                    }
                }
            }
        } catch (IOException e) {

            /*
             * If we fail to open it then, the file has not been created yet'.
             * This is good because it means that there is no error.
             */
            if (file.contains("FAILED_QUERIES")) {
                logging.info(Utilities.getLogPreString()
                        + "CheckForFailedQueries() | "
                        + "No failed queries found, continuing...");
            } else if (file.contains("FAILED_ACK")) {
                logging.info(Utilities.getLogPreString()
                        + "CheckForFailedQueries() | "
                        + "No failed acknowledgements, continuing...");
            } else {
                logging.error(Utilities.getLogPreString()
                        + "CheckForFailedQueries() | '" + file
                        + "' not found, continuing...");
            }
        }

        return queries;
    }

    /**
     * This function determines how the queries will be re-executed i.e. whether
     * SELECT or UPDATE.
     *
     * @param query the query to re-execute
     * @param tries the number of times to retry
     * @param file the failed queries file
     */
    private void doRecon(final String query, final int tries,
            final String file) {
        int maxRetry = props.getMaxFailedQueryRetries();

        if (query.toLowerCase().startsWith(PaymentPusherConstants.UPDATE_ID)) {
            int qstate = runUpdateRecon(query);

            if (qstate == PaymentPusherConstants.UPDATE_RECON_SUCCESS) {
                logging.info(Utilities.getLogPreString() + "doRecon() | "
                        + "Re-executed this query: "
                        + query + " successfully, deleting it from file...");
                deleteQuery(file, query);
            } else if (qstate == PaymentPusherConstants.UPDATE_RECON_FAILED) {
                logging.info(Utilities.getLogPreString() + "doRecon() | "
                        + "Failed to re-execute failed query: " + query
                        + "[Try " + tries + " out of  " + maxRetry + "]");
                int currTry = tries + 1;
                if (tries >= maxRetry) {
                    logging.info(Utilities.getLogPreString() + "doRecon() | "
                            + "Tried to re-execute failed query "
                            + props.getMaxFailedQueryRetries()
                            + " times but still failed, exiting...");
                } else {
                    logging.info(Utilities.getLogPreString() + "doRecon() | "
                            + "Retrying in "
                            + (props.getSleepTime() / 1000) + " sec(s) ");
                    doWait(props.getSleepTime());
                    doRecon(query, currTry, file);
                }
            }
        }
    }

    /**
     * Re-executes the specified query.
     *
     * @param query the query to run
     *
     * @return the status of the update
     */
    private int runUpdateRecon(final String query) {
        int result;

        try (Connection conn = mysql.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            logging.info(Utilities.getLogPreString() + "runUpdateRecon() | "
                    + "I have just successfully "
                    + "re-executed this failed query: " + query);
            result = PaymentPusherConstants.UPDATE_RECON_SUCCESS;
        } catch (SQLException e) {
            logging.error(Utilities.getLogPreString() + "runUpdateRecon() | "
                    + "SQLException: " + e.getMessage());
            result = PaymentPusherConstants.UPDATE_RECON_FAILED;
        }

        return result;
    }

    /**
     * Delete a query from the failed_queries file after a successful recon.
     *
     * @param queryfile the query file
     * @param query the query to delete
     */
    private void deleteQuery(final String queryfile, final String query) {
        List<String> queries = new ArrayList<>(0);

        try (FileInputStream fin = new FileInputStream(queryfile);
                DataInputStream in = new DataInputStream(fin);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in))) {
            String data;
            while ((data = br.readLine()) != null) {
                queries.add(data);
            }

            // Find a match to the query
            logging.info(Utilities.getLogPreString() + "deleteQuery() | "
                    + "About to remove this query: "
                    + query + " from file: " + queryfile);

            if (queries.contains(query)) {
                queries.remove(query);
                logging.info(Utilities.getLogPreString() + "deleteQuery() | "
                        + "I have removed this query: "
                        + query + " from file: " + queryfile);
            }

            // Now save the file
            try (PrintWriter pout = new PrintWriter(
                    new FileOutputStream(queryfile, false))) {
                for (String newQueries : queries) {
                    pout.println(newQueries);
                }
                pout.flush();
            }
        } catch (IOException e) {
            /**
             * If we fail to open it then, the file has not been created yet
             * This is good because it means that no error(s) have been
             * experienced yet
             */
            logging.info(Utilities.getLogPreString() + "deleteQuery() | "
                    + "No errors reported " + queryfile + e.getMessage());
        }
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
            logging.info(Utilities.getLogPreString() + " doWait() | "
                    + "Thread could not sleep for the specified time");
        }
    }

    /**
     * Retry log transactions that were not updated.
     *
     * @param serviceID the service ID
     * @param serviceCode the service code
     *
     * @return the number of failed log transactions found
     */
    private int retryFailedLogs(final int serviceID, final String serviceCode) {
        String failureFile = serviceCode + "_" + serviceID + "_"
                + PaymentPusherConstants.FAILED_ACK_FILE;
        List<String> failedLogs = checkForFailedLogsOrACKs(failureFile,
                serviceID, serviceCode);
        int failures = failedLogs.size();
        if (failures == 0) {
            return failures;
        }

        logging.info(Utilities.getLogPreString() + " retryFailedLogs() | "
                + "| I found "
                + failures + " failed logs, trying to update...");

        for (int i = 0; i < failures; i++) {
            String reconLog = failedLogs.get(i);
            logging.info(Utilities.getLogPreString() + " retryFailedLogs() | "
                    + "Re-executing failed log with data: "
                    + reconLog);

            boolean updated = false;
            JSONObject updatedObj;
            try {
                updatedObj = new JSONObject(reconLog);
                JSONObject payload = new JSONObject(
                        updatedObj.getString("payload"));
                JSONArray packet = payload.getJSONArray("packet");
                int beepTransactionID = packet.getJSONObject(0)
                        .getInt("beepTransactionID");
                int statusCode = packet.getJSONObject(0).getInt("statusCode");
                updated = Utilities.updateTransaction(logging, props,
                        updatedObj, beepTransactionID, statusCode, true,
                        serviceID, serviceCode);
            } catch (JSONException ex) {
                logging.info(Utilities.getLogPreString()
                        + " retryFailedLogs() | "
                        + "A json exception was caught while retrying a "
                        + "record in the " + failureFile
                        + " file. Failed to update please check, Error: "
                        + ex.getMessage());
            }

            if (updated) {
                logging.info(Utilities.getLogPreString()
                        + " retryFailedLogs() | "
                        + "Updated successfully, about to delete it from "
                        + "file...");
                deleteQuery(failureFile, reconLog);
            } else {
                logging.info(Utilities.getLogPreString()
                        + " retryFailedLogs() | "
                        + "Update Failed, will not be deleted from file...");
            }
        }

        logging.info(Utilities.getLogPreString()
                + " retryFailedLogs() "
                + "| Finished logs");

        return failures;
    }

    /**
     * Loads a file containing failed queries or post payment ACKs to be re-run.
     *
     * @param file the file name
     * @param serviceID the service ID
     * @param serviceCode the service code
     *
     * @return a List containing the queries or ACKs
     */
    @SuppressWarnings("NestedAssignment")
    private List<String> checkForFailedLogsOrACKs(final String file,
            final int serviceID, final String serviceCode) {
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
        } catch (FileNotFoundException ex) {
            String queryFile = serviceCode + "_" + serviceID + "_"
                    + PaymentPusherConstants.FAILED_ACK_FILE;
            /*
             * If we fail to open it then, the file has not been created yet'.
             * This is good because it means that there is no error.
             */
            if (queryFile.equals(file)) {
                logging.debug(Utilities.getLogPreString()
                        + "CheckForFailedLogsOrACKs() | "
                        + "No failed save queries, continuing...");
            } else {
                logging.debug(Utilities.getLogPreString()
                        + "CheckForFailedLogsOrACKs() | "
                        + "No failed post payment ACKs, continuing...");
            }
        } catch (IOException ex) {
            logging.fatal(Utilities.getLogPreString()
                    + "CheckForFailedLogsOrACKs() | "
                    + "Unable to read failed queries file. Error message: "
                    + ex.getMessage());
        }

        return queries;
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
        logging.info(Utilities.getLogPreString()
                + " shutdownAndAwaitTermination() |"
                + "Executor pool waiting for tasks to complete");
        pool.shutdown(); // Disable new tasks from being submitted

        try {

            do {
                pool.awaitTermination(60, TimeUnit.SECONDS);
                logging.info(Utilities.getLogPreString()
                        + "Waiting for the transaction to finish processing."
                        + "Pool : " + pool.hashCode());

            } while (!pool.isTerminated());

            pool.shutdownNow();

            logging.info(Utilities.getLogPreString()
                    + "Executor pool terminated successfully"
                    + "Pool : " + pool.hashCode());

        } catch (InterruptedException ie) {
            logging.error(Utilities.getLogPreString()
                    + " shutdownAndAwaitTermination() |"
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

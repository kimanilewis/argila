/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templatess
 * and open the template in the editor.
 */
package com.argila.pc;

import com.argila.pc.db.MySQL;
import com.argila.pc.utils.Constants;
import com.argila.pc.utils.CoreUtils;
import com.argila.pc.utils.Logging;
import com.argila.pc.utils.Props;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
//import sun.org.mozilla.javascript.internal.regexp.SubString;

/**
 *
 * @author lewie
 */
public class ServerJob implements Runnable {

    /**
     * Log class instance.
     */
    private final Logging logging;
    private final Props props;
    /**
     * The MySQL data source.
     */
    private final transient MySQL mysql;
    /**
     * Flag to check if current pool is completed.
     */
    private transient boolean isCurrentPoolShutDown = false;

    protected Socket clientSocket = null;
    protected String serverText = null;
    private final AccountsData accountsData;

    public ServerJob(Socket ClientSocket, final MySQL mySQL, final Logging log, final Props Props) {
        clientSocket = ClientSocket;
        logging = log;
        mysql = mySQL;
        props = Props;
        accountsData = new AccountsData();
    }

    @Override
    public void run() {
        try {
            String messageString = "";
            String accountNumber;
            String batteryLevel;
            String outPutString;
//            try (InputStream input = clientSocket.getInputStream();
//                    OutputStream output = clientSocket.getOutputStream()) {
            try (BufferedReader input
                    = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());) {
                //  messageByte[0] = (byte) input.read();

                messageString = input.readLine();
                logging.info(CoreUtils.getLogPreString() + "init() |"
                        + " Request was processed: well String is.." + messageString);

                accountNumber = messageString.substring(0, messageString.length() - 3);

                logging.info(CoreUtils.getLogPreString() + "init() |"
                        + " Request was processed: AccountNumber received is.." + accountNumber);
                batteryLevel = messageString.substring(messageString.length() - 3);
                logging.info(CoreUtils.getLogPreString() + "init() |"
                        + " Request was processed: Battery level received is.." + batteryLevel);
                int response = checkAccountValidity(accountNumber);
                if (response < 0) {
                    triggerStopSession();
                    outPutString = "SC9999Z";
                } else if (response > props.getMaxTime() && props.getMaxTime() < 10) {

                    accountsData.setAvailableTime(String.valueOf(props.getMaxTime()));
                    updateProcessingState();
                    outPutString = "SC000" + String.valueOf(props.getMaxTime() + "Z");
                    updateProcessingState();

                } else if (response > props.getMaxTime() && props.getMaxTime() > 9) {
                    accountsData.setAvailableTime(String.valueOf(props.getMaxTime()));
                    updateProcessingState();
                    outPutString = "SC00" + String.valueOf(props.getMaxTime() + "Z");
                    updateProcessingState();

                } else if (response < props.getMaxTime()
                        && response > props.getMinTime() && response > 9) {
                    accountsData.setAvailableTime(String.valueOf(props.getMinTime()));
                    updateProcessingState();

                    outPutString = "SC00" + String.valueOf(response + "Z");
                } else if (response < props.getMaxTime()
                        && response > props.getMinTime() && response < 9) {
                    accountsData.setAvailableTime(String.valueOf(props.getMinTime()));
                    updateProcessingState();

                    outPutString = "SC000" + String.valueOf(response + "Z");
                } else {
                    outPutString = "SC0000Z";
                }
                try (
                        Writer w = new OutputStreamWriter(output, "UTF-8")) {
                    w.write(outPutString);
                    w.flush();
//                    w.close();
                    logging.info(CoreUtils.getLogPreString() + "init() |"
                            + " Request was processed successfully");

                }

                logging.info(CoreUtils.getLogPreString()
                        + "init() | Response sent to POS: " + outPutString);
                output.close();
                output.flush();

            }

        } catch (IOException | IndexOutOfBoundsException ex) {
            logging.info(CoreUtils.getLogPreString() + "init() |"
                    + " Request was not processed: .." + ex);
        }
    }

    /**
     *
     * @param accountNumber
     * @return
     */
    private int checkAccountValidity(String accountNumber) {
        ResultSet rs = null;
        ExecutorService executor;
        PreparedStatement stmt = null;
        String[] params = {
            String.valueOf(accountNumber)
        };
        int time = 0;
        int status = 4;

        String query = "SELECT accountNumber, cpa.expiryTime, "
                + " cpa.customerProfileAccountID, cpa.availableTime,"
                + " cpa.processingStatus, amountBalance, cp.customerProfileID "
                + " FROM customerProfiles cp "
                + " INNER JOIN customerProfileAccounts cpa "
                + " on cp.customerProfileID = cpa.customerProfileID"
                + " WHERE accountNumber =  ? ";

        try (Connection conn = mysql.getConnection();) {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);

            logging.info(CoreUtils.getLogPreString()
                    + "Updating Record with Accounts "
                    + query);
            // stmt.executeQuery();
            rs = stmt.executeQuery();
            int size = 0;
            if (rs.last()) {
                size = rs.getRow();
                rs.beforeFirst();
            }
            if (size > 0) {
                logging.info(CoreUtils.getLogPreString()
                        + "Fetched " + size + " customer accounts record(s)..."
                        + query);
//                isCurrentPoolShutDown = false;

                if (size <= props.getNumOfChildren()) {
                    executor = Executors.newFixedThreadPool(size);
                } else {
                    executor = Executors
                            .newFixedThreadPool(props.getNumOfChildren());
                }

                logging.info(CoreUtils.getLogPreString()
                        + "Record processed. AccountNumber exists ");
                while (rs.next()) {

                    accountsData.setAccountNumber(rs.getString("accountNumber"));
                    accountsData.setCustomerProfileAccountID(rs.getInt("customerProfileAccountID"));
                    accountsData.setAmount(rs.getDouble("amountBalance"));
                    time = rs.getInt("availableTime");
                    accountsData.setAvailableTime(String.valueOf(time));
                    status = rs.getInt("processingStatus");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
                    // todayDate = new Date();
                    Date todayDate = sdf.parse(sdf.format(new Date()));
                    Date sessionExpiryTime;
                    try {
                        sessionExpiryTime = sdf.parse("0000-00-00 00:00:00");

                        if (rs.getString("expiryTime") != null
                                && !rs.getString("expiryTime").isEmpty()
                                && !rs.getString("expiryTime").startsWith("0000")) {

                            sessionExpiryTime = sdf.parse(rs.getString("expiryTime"));
                            if (todayDate.before(sessionExpiryTime)) {
                                time = 0;
                                time = -1;
                            }
                        }
                    } catch (ParseException ex) {
                        logging.info(CoreUtils.getLogPreString()
                                + "ParseException caught while parsing date"
                                + "" + ex.getMessage());
                    }

                    //  Runnable task = accountsProcessingTask(accountsData);
                    //execute the tast to be pushed out
//                    executor.execute(task);
                }
                rs.close();
                stmt.close();
                /*
                 * This will make the executor accept no new threads and
                 * finish all existing threads in the queue.
                 */
                shutdownAndAwaitTermination(executor);
            } else {
                logging.info(CoreUtils.getLogPreString()
                        + "No client profile accounts records were fetched "
                        + "from the DB for processing...");
            }

        } catch (SQLException ex) {

            logging.error(CoreUtils.getLogPreString()
                    + "An " + ex.getClass().getName() + " occured while "
                    + "updating the customerProfile record | "
                    + "amount balance"
                    + ". Error: "
                    + ex.getMessage());
            String trueStoreQuery = CoreUtils.prepareSqlString(query, params,
                    0);
            CoreUtils.updateFile(logging,
                    Constants.FAILED_QUERIES_FILE,
                    trueStoreQuery);
        } catch (ParseException ex) {
            Logger.getLogger(ServerJob.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            isCurrentPoolShutDown = true;
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlex) {
                    logging.error(CoreUtils.getLogPreString()
                            + "Error closing statement: "
                            + sqlex.getMessage());
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlex) {
                    logging.error(CoreUtils.getLogPreString()
                            + "Failed to close statement: "
                            + sqlex.getMessage());
                }
            }
        }
        return time;

    }

    /**
     * Update the processing state of the Account.
     *
     * @param timeAvailable
     */
    public void updateProcessingState() {
        String logPreString = CoreUtils.getLogPreString() + "updateTransaction() | "
                + accountsData.getAccountNumber() + " | ";
        String query;
        PreparedStatement stmt = null;
        Connection conn = null;

        String trueQuery;

        logging.info(logPreString
                + "Updating transaction with customer Profile Account ID: "
                + accountsData.getCustomerProfileAccountID());

        query = "UPDATE customerProfileAccounts "
                + " SET "
                + " startTime = NOW(), processingStatus = ?,"
                + " expiryTime = DATE_ADD( NOW(), INTERVAL ? MINUTE)"
                + " WHERE customerProfileAccountID = ? ";
        String[] params = {
            String.valueOf(props.getProcessingStatus()),
            accountsData.getAvailableTime(),
            String.valueOf(accountsData.getCustomerProfileAccountID())
        };
        trueQuery = CoreUtils.prepareSqlString(query, params, 0);

        try {

            conn = mysql.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, props.getProcessingStatus());
            stmt.setString(2, accountsData.getAvailableTime());
            stmt.setInt(3, accountsData.getCustomerProfileAccountID());

            logging.info(logPreString
                    + "Updated Record with account number: "
                    + accountsData.getAccountNumber());
            stmt.executeUpdate();

            logging.info(logPreString
                    + "Record processed. Account numsber: "
                    + accountsData.getAccountNumber() + trueQuery);
//
            stmt.close();
            stmt = null;
            conn.close();
            conn = null;
        } catch (Exception ex) {

            logging.error(logPreString
                    + "An " + ex.getClass().getName() + " occured while "
                    + "updating the record with "
                    + "Account Number: "
                    + accountsData.getAccountNumber() + ". Error: "
                    + ex.getMessage());
            query = "UPDATE `customerProfileAccounts` "
                    + "SET availableTime = \"?\", "
                    + "startTime = NOW(), "
                    + "`expiryTime` = DATE_ADD( NOW(), INTERVAL ? MINUTES)"
                    + " WHERE `customerProfileAccountID` = ?";

            String trueStoreQuery = CoreUtils.prepareSqlString(query, params, 0);
            CoreUtils.updateFile(logging,
                    props.getFailedQueryFile(),
                    trueStoreQuery);
        }

    }

    /**
     * Update the processing state of the Account.
     *
     */
    public void triggerStopSession() {
        String logPreString = CoreUtils.getLogPreString() + "updateTransaction() | "
                + accountsData.getAccountNumber() + " | ";
        String query;
        PreparedStatement stmt = null;
        Connection conn = null;

        String trueQuery;

        logging.info(logPreString
                + "Updating transaction with customer Profile Account ID: "
                + accountsData.getCustomerProfileAccountID()
                + " with the following values: ");

        query = "UPDATE customerProfileAccounts set processingStatus = ?,"
                + " expiryTime = NOW() "
                + " WHERE customerProfileAccountID = ? ";
        String[] params = {
            String.valueOf(props.getProcessedStatus()),
            String.valueOf(accountsData.getCustomerProfileAccountID())
        };
        trueQuery = CoreUtils.prepareSqlString(query, params, 0);

        try {

            conn = mysql.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, props.getProcessedStatus());
            stmt.setInt(2, accountsData.getCustomerProfileAccountID());

            logging.info(logPreString
                    + "Updating Record with account number: "
                    + accountsData.getAccountNumber()
                    + " using query:" + trueQuery);
            stmt.executeUpdate();

            logging.info(logPreString
                    + "Record processed. Account numsber: "
                    + accountsData.getAccountNumber());

            stmt.close();
            stmt = null;
            conn.close();
            conn = null;
        } catch (Exception ex) {

            logging.error(logPreString
                    + "An " + ex.getClass().getName() + " occured while "
                    + "updating the record with "
                    + "Account Number: "
                    + accountsData.getAccountNumber() + ". Error: "
                    + ex.getMessage());
            query = "UPDATE `customerProfileAccounts` "
                    + "SET processingStatus =?, "
                    + "startTime = NOW(), "
                    + "`expiryTime` = DATE_ADD( NOW(), INTERVAL ? MINUTES)"
                    + " WHERE `customerProfileAccountID` = ?";

            String trueStoreQuery = CoreUtils.prepareSqlString(query, params, 0);
            CoreUtils.updateFile(logging,
                    props.getFailedQueryFile(),
                    trueStoreQuery);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                    stmt = null;
                } catch (Exception ex) {
                    logging.error(logPreString
                            + "Failed to close Statement object: "
                            + ex.getMessage());
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                    conn = null;
                } catch (Exception ex) {
                    logging.error(logPreString
                            + "Failed to close connection object: "
                            + ex.getMessage());
                }
            }
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

}

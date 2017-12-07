/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.argila.pc;

import com.argila.pc.db.MySQL;
import com.argila.pc.utils.CoreUtils;
import com.argila.pc.utils.Logging;
import com.argila.pc.utils.Props;
import com.argila.pc.utils.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author lewie
 */
public final class SessionManagerJob implements Runnable {

    /**
     * The MySQL connection object.
     */
    private final MySQL mysql;
    /**
     * Log class instance.
     */
    private final Logging logging;
    /**
     * Properties class instance.
     */
    private final Props props;
    /**
     * Profile data object.
     */
    private final AccountsData accounts;
    private final String logPreString;

    /**
     * Class Constructor.
     *
     * @param log log class instance
     * @param prop properties class instance
     * @param mySQL mySQL pool class instance
     * @param data profile data
     */
    public SessionManagerJob(final Logging log, final Props prop,
            final MySQL mySQL, final AccountsData data) {
        logging = log;
        props = prop;
        mysql = mySQL;
        accounts = data;
        logPreString = CoreUtils.getLogPreString() + "SessionManager | ";
    }

    /**
     * Process profile to the client.
     */
    private void processRequest() {
        logging.info(logPreString
                + "Starting a job with customerProfileID: "
                + accounts.getCustomerProfileID());
        calculateTimeSpent();
        insertSessionData();
        updateTransaction();

        logging.info(logPreString
                + "Closing a job with customerProfileID: "
                + accounts.getCustomerProfileID());

    }

    private void updateTransaction() {
        int status;
        String[] params = {
            String.valueOf(accounts.getAmountBalance()),
            String.valueOf(props.getFinishedProcessedStatus()),
            String.valueOf(props.getFinishedProcessedStatus()),
            String.valueOf(accounts.getExpiryDate()),
            String.valueOf(accounts.getCustomerProfileID())
        };
        if (accounts.getTimeSpent() == 0) {
            status = props.getProcessedStatus();
        } else {
            status = props.getFinishedProcessedStatus();
        }

        String query = "UPDATE customerProfiles cp "
                + " INNER JOIN customerProfileAccounts cpa "
                + " ON cp.customerProfileID = cpa.customerProfileID "
                + " SET cpa.amountBalance = ?, cp.statusCode = ? , "
                + " cpa.processingStatus = ?, cpa.expiryDate =?"
                + " WHERE cp.customerProfileID = ? ";

        try (Connection conn = mysql.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, accounts.getAmountBalance());
            stmt.setInt(2, props.getFinishedProcessedStatus());
            stmt.setInt(3, status);
            stmt.setString(4, accounts.getExpiryDate());
            stmt.setInt(5, accounts.getCustomerProfileID());

            logging.info(logPreString
                    + "Updating Record with Accounts "
                    + accounts.getAccountNumber()
                    + query);
            stmt.executeUpdate();

            logging.info(logPreString
                    + "Record processed. Client AccountNumber: "
                    + accounts.getAccountNumber());
        } catch (SQLException ex) {
            logging.error(logPreString
                    + "An " + ex.getClass().getName() + " occured while "
                    + "updating the customerProfile record | "
                    + accounts.getAccountNumber() + "| with "
                    + "amount balance"
                    + accounts.getAmountBalance() + ". Error: "
                    + ex.getMessage());
            String trueStoreQuery = CoreUtils.prepareSqlString(query, params,
                    0);
            CoreUtils.updateFile(logging,
                    Constants.FAILED_QUERIES_FILE,
                    trueStoreQuery);
        }
    }

    private void updateSessionData(int statusCode) {

        String[] params = {
            String.valueOf(accounts.getSessionDataID()),
            String.valueOf(statusCode)
        };

        String query = "UPDATE sessionData set syncStatus = ? "
                + " WHERE sessionDataID = ?";

        try (Connection conn = mysql.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, statusCode);
            stmt.setInt(2, accounts.getSessionDataID());

            logging.info(logPreString
                    + "Updating Record with Accounts "
                    + accounts.getAccountNumber()
                    + query);
            stmt.executeUpdate();

            logging.info(logPreString
                    + "Record processed. Client AccountNumber: "
                    + accounts.getAccountNumber());
        } catch (SQLException ex) {
            logging.error(logPreString
                    + "An " + ex.getClass().getName() + " occured while "
                    + "updating the customerProfile record | "
                    + accounts.getAccountNumber() + "| with "
                    + "amount balance"
                    + accounts.getAmountBalance() + ". Error: "
                    + ex.getMessage());
            String trueStoreQuery = CoreUtils.prepareSqlString(query, params,
                    0);
            CoreUtils.updateFile(logging,
                    Constants.FAILED_QUERIES_FILE,
                    trueStoreQuery);
        }
    }

    private void insertSessionData() {

        String[] params = {
            String.valueOf(accounts.getCustomerProfileAccountID()),
            String.valueOf(accounts.getTimeSpent()),
            String.valueOf(accounts.getAmountSpent()),
            String.valueOf(accounts.getAmountBalance())
        };

        String query = "INSERT INTO sessionData(customerProfileAccountID,"
                + "timeSpent,amountSpent, amountBalance,dateCreated) "
                + "values (?,?,?,?,NOW())";

        try (Connection conn = mysql.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, accounts.getCustomerProfileAccountID());
            stmt.setLong(2, accounts.getTimeSpent());
            stmt.setDouble(3, accounts.getAmountSpent());
            stmt.setDouble(4, accounts.getAmountBalance());

            logging.info(logPreString
                    + "Updating Record with Accounts "
                    + accounts.getAccountNumber()
                    + query);
            stmt.executeUpdate();

            logging.info(logPreString
                    + "Record processed. Client AccountNumber: "
                    + accounts.getAccountNumber());
        } catch (SQLException ex) {
            logging.error(logPreString
                    + "An " + ex.getClass().getName() + " occured while "
                    + "updating the clientProfileAccounts record with "
                    + "client Account number: "
                    + accounts.getAccountNumber() + ". Error: "
                    + ex.getMessage());
            query = "INSERT INTO sessionData(customerProfileAccountID,"
                    + "timeSpent,amountSpent, amountBalance,dateCreated,) "
                    + "values (?,?,?,?,NOW())";
            String trueStoreQuery = CoreUtils.prepareSqlString(query, params,
                    0);
            CoreUtils.updateFile(logging,
                    Constants.FAILED_QUERIES_FILE,
                    trueStoreQuery);
        }
    }

    /**
     * Runs the accounts.
     */
    @Override
    public void run() {
        processRequest();
    }

    /**
     * calculates time spent.
     *
     */
    private void calculateTimeSpent() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        try {
            Date sessionStartTime = sdf.parse("0000-00-00 00:00:00");
            if (accounts.getStartTime() != null
                    && !accounts.getStartTime().isEmpty()
                    && !accounts.getStartTime().startsWith("0000")) {
                sessionStartTime = sdf.parse(accounts.getStartTime());
            }
            Date sessionExpiryTime = sdf.parse("0000-00-00 00:00:00");
            if (accounts.getExpiryTime() != null
                    && !accounts.getExpiryTime().isEmpty()
                    && !accounts.getExpiryTime().startsWith("0000")) {
                sessionExpiryTime = sdf.parse(accounts.getExpiryTime());
            }
            if (accounts.getProfileStatus() == props.getProcessedStatus()) {
                long timeDiff = sessionExpiryTime.getTime() - sessionStartTime.getTime();
                /*get time in minutes*/
                long minutesSpent = TimeUnit.MILLISECONDS.toSeconds(timeDiff);
                accounts.setTimeSpent(minutesSpent);
//                double amountSpent = minutesSpent * (props.getCostOfTimePerMinute() / 60);
                accounts.setExpiryDate((String) accounts.getExpiryDate());

//                accounts.setAmountSpent(amountSpent);
//                double balance = accounts.getAmountBalance() - amountSpent;
//                accounts.setAmountBalance(balance);
            } else if (accounts.getProfileStatus() == props.getProcessingStatus()) {
                accounts.setTimeSpent(0);
                accounts.setAmountSpent(0);

            } else {
                logging.error(logPreString
                        + "Could not set time spent: " + accounts.toString());
            }
        } catch (ParseException ex) {
            logging.error(logPreString
                    + "Could not parse the date "
                    + "provided: exception: " + ex.getMessage());
        }
    }

}

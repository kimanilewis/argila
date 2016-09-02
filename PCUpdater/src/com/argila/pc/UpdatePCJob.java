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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

/**
 *
 * @author lewie
 */
public final class UpdatePCJob implements Runnable {

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
    public UpdatePCJob(final Logging log, final Props prop,
            final MySQL mySQL, final AccountsData data) {
        logging = log;
        props = prop;
        mysql = mySQL;
        accounts = data;
        logPreString = CoreUtils.getLogPreString() + "UpdatePCJob | ";
    }

    /**
     * Process profile to the client.
     */
    private void processRequest() {
        logging.info(logPreString
                + "Starting a job with customer Acount Number : "
                + accounts.getAccountNumber());
        if (!checkAccount()) {
            createCustomerProfiles();

        } else if (updateCustomerProfiles()) {
            updateCustomerProfileAccount();
        } else {

            logging.info(logPreString
                    + "Request not identified for customer Acount Number : "
                    + accounts.getAccountNumber());
        }

        logging.info(logPreString
                + "Closing a job with customer Acount Number : "
                + accounts.getAccountNumber());

    }

    private boolean checkAccount() {
        String cpaQuery;
        String logQuery = "";
        boolean accountStored = false;
        /**
         * Query to fetch active transacting records if any exists.
         */
        cpaQuery = "SELECT accountNumber,customerProfileID "
                + " FROM customerProfiles WHERE accountNumber = ? ";

        try (Connection conn = mysql.getConnection();
                PreparedStatement stmt = conn.prepareStatement(cpaQuery);) {
            stmt.setString(1, accounts.getAccountNumber());
            String[] params = {
                accounts.getAccountNumber()
            };

            logQuery = CoreUtils.prepareSqlString(cpaQuery, params, 0);

            try (ResultSet rs = stmt.executeQuery()) {
                int size = 0;
                if (rs.last()) {
                    size = rs.getRow();
                    rs.beforeFirst();
                }
                if (size > 0) {
                    logging.info(logPreString
                            + "Fetched " + size + " customer accounts record(s)..."
                            + logQuery);

                    while (rs.next()) {
                        accounts.setCustomerProfileID(rs.getInt("customerProfileID"));
                        accountStored = true;
                    }
                    rs.close();
                    stmt.close();
                } else {
                    logging.info(logPreString
                            + "No previous records was fetched "
                            + "from the DB for processing...");
                }
            } catch (SQLException e) {
                logging.error(logPreString + "Failed to "
                        + "fetch Bucket: Select Query: "
                        + logQuery + " Error Message :" + e.getMessage());
            }
        } catch (SQLException e) {
            logging.error(logPreString + "Failed to "
                    + "fetch Bucket: Select Query: "
                    + logQuery + " Error Message :" + e.getMessage());
        }
        return accountStored;
    }

    private boolean updateCustomerProfiles() {
        String[] params = {
            String.valueOf(accounts.getAccountBalance()),
            String.valueOf(accounts.getAccountNumber())
        };
        boolean response = true;

        String query = "UPDATE customerProfiles set amount = ? "
                + " WHERE accountNumber = ?";

        try (Connection conn = mysql.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, accounts.getAccountBalance());
            stmt.setString(2, accounts.getAccountNumber());

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
                    + accounts.getAccountBalance() + ". Error: "
                    + ex.getMessage());
            String trueStoreQuery = CoreUtils.prepareSqlString(query, params,
                    0);
            CoreUtils.updateFile(logging,
                    Constants.FAILED_QUERIES_FILE,
                    trueStoreQuery);
            updateTransaction(props.getUnProcessedStatus());
            response = false;
        }
        return response;

    }

    private void createCustomerProfiles() {
        int customerProfileID = 0;
        String[] params = {
            String.valueOf(accounts.getAccountNumber()),
            String.valueOf(accounts.getAccountBalance())
        };
        String queryString = "";
        if (accounts.getMSISDN() > 1) {
            queryString = ",MSISDN =" + accounts.getMSISDN();
        }

        String query = "INSERT INTO customerProfiles(accountNumber,amount, dateCreated "
                + queryString + ") VALUES (?,?,NOW()) ";

        try (Connection conn = mysql.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, accounts.getAccountNumber());
            stmt.setDouble(2, accounts.getAccountBalance());

            logging.info(logPreString
                    + "Updating Record with Accounts "
                    + accounts.getAccountNumber()
                    + query);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                customerProfileID = rs.next() ? (int) rs.getInt(1)
                        : customerProfileID;
                accounts.setCustomerProfileID(customerProfileID);
                createCustomerProfileAccount();
            } catch (SQLException sqle) {
                logging.info(logPreString
                        + "SQLException caught while creating customerProfile"
                        + sqle.getMessage());
            }
            logging.info(logPreString
                    + "Record processed. Client AccountNumber: "
                    + accounts.getAccountNumber());
        } catch (SQLException ex) {
            logging.error(logPreString
                    + "An " + ex.getClass().getName() + " occured while "
                    + "updating the customerProfile record | "
                    + accounts.getAccountNumber() + "| with "
                    + "amount balance"
                    + accounts.getAccountBalance() + ". Error: "
                    + ex.getMessage());
            String trueStoreQuery = CoreUtils.prepareSqlString(query, params,
                    0);
            CoreUtils.updateFile(logging,
                    Constants.FAILED_QUERIES_FILE,
                    trueStoreQuery);
        }

    }

    private void createCustomerProfileAccount() {
        int customerProfileID = 0;
        String[] params = {
            String.valueOf(accounts.getCustomerProfileID()),
            String.valueOf(accounts.getAccountBalance()),
            accounts.getAvailableTime()
        };

        String query = "INSERT INTO customerProfileAccounts(customerProfileID, "
                + "amountBalance, availableTime)"
                + " VALUES (?,?,?) ";

        try (Connection conn = mysql.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, accounts.getCustomerProfileID());
            stmt.setDouble(2, accounts.getAccountBalance());
            stmt.setString(3, accounts.getAvailableTime());
            logging.info(logPreString
                    + "Updating Record with Accounts "
                    + accounts.getAccountNumber()
                    + query);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                customerProfileID = rs.next() ? (int) rs.getInt(1)
                        : customerProfileID;
                accounts.setCustomerProfileID(customerProfileID);
            } catch (SQLException sqle) {
                logging.info(logPreString
                        + "SQLException caught while creating customerProfile"
                        + sqle.getMessage());
            }
            logging.info(logPreString
                    + "Record processed. Client AccountNumber: "
                    + accounts.getAccountNumber());
        } catch (SQLException ex) {
            logging.error(logPreString
                    + "An " + ex.getClass().getName() + " occured while "
                    + "updating the customerProfile record | "
                    + accounts.getAccountNumber() + "| with "
                    + "amount balance"
                    + accounts.getAccountBalance() + ". Error: "
                    + ex.getMessage());
            String trueStoreQuery = CoreUtils.prepareSqlString(query, params,
                    0);
            CoreUtils.updateFile(logging,
                    Constants.FAILED_QUERIES_FILE,
                    trueStoreQuery);
            return;
        }
        updateTransaction(props.getProcessedStatus());
    }

    private void updateCustomerProfileAccount() {
        int customerProfileID = 0;
        String[] params = {
            String.valueOf(accounts.getAvailableTime()),
            String.valueOf(accounts.getAccountBalance()),
            String.valueOf(accounts.getCustomerProfileID()),};

        String query = "UPDATE customerProfileAccounts "
                + "SET availableTime = ?, amountBalance = ? "
                + " WHERE customerProfileID = ? ";

        try (Connection conn = mysql.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, accounts.getAvailableTime());
            stmt.setDouble(2, accounts.getAccountBalance());
            stmt.setInt(3, accounts.getCustomerProfileID());
            logging.info(logPreString
                    + "Updating Record with Accounts "
                    + accounts.getAccountNumber()
                    + query);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                customerProfileID = rs.next() ? (int) rs.getInt(1)
                        : customerProfileID;
                accounts.setCustomerProfileID(customerProfileID);
            } catch (SQLException sqle) {
                logging.info(logPreString
                        + "SQLException caught while creating customerProfile"
                        + sqle.getMessage());
            }
            logging.info(logPreString
                    + "Record processed. Client AccountNumber: "
                    + accounts.getAccountNumber());
        } catch (SQLException ex) {
            logging.error(logPreString
                    + "An " + ex.getClass().getName() + " occured while "
                    + "updating the customerProfile record | "
                    + accounts.getAccountNumber() + "| with "
                    + "amount balance"
                    + accounts.getAccountBalance() + ". Error: "
                    + ex.getMessage());
            String trueStoreQuery = CoreUtils.prepareSqlString(query, params,
                    0);
            CoreUtils.updateFile(logging,
                    Constants.FAILED_QUERIES_FILE,
                    trueStoreQuery);
            return;
        }
        updateTransaction(props.getProcessedStatus());
    }

    private void updateTransaction(int statusCode) {

        String[] params = {
            String.valueOf(statusCode),
            String.valueOf(accounts.getCoreRequestID())
        };

        String query = "UPDATE coreRequests set status =  ? "
                + "WHERE coreRequestID = ?";

        try (Connection conn = mysql.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, statusCode);
            stmt.setInt(2, accounts.getCoreRequestID());

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
                    + accounts.getAccountBalance() + ". Error: "
                    + ex.getMessage());
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

}

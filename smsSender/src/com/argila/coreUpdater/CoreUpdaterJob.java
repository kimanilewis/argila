/**
 * To change this license header, choose License Headers in Project Properties
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 *
 * @auther Lewis Kimani <kimanilewi@gmail.com>
 */
package com.argila.coreUpdater;

import com.argila.coreUpdater.db.MySQL;
import com.argila.coreUpdater.utils.AfricasTalkingGateway;
import com.argila.coreUpdater.utils.CoreUtils;
import com.argila.coreUpdater.utils.Logging;
import com.argila.coreUpdater.utils.Props;
import com.argila.coreUpdater.utils.Constants;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import static org.apache.http.params.CoreProtocolPNames.USER_AGENT;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;

/**
 *
 * @author lewie
 */
public final class CoreUpdaterJob implements Runnable {

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
    private final String USER_AGENT = "Mozilla/5.0";

    /**
     * Class Constructor.
     *
     * @param log log class instance
     * @param prop properties class instance
     * @param mySQL mySQL pool class instance
     * @param data profile data
     */
    public CoreUpdaterJob(final Logging log, final Props prop,
            final MySQL mySQL, final AccountsData data) {
        logging = log;
        props = prop;
        mysql = mySQL;
        accounts = data;
        logPreString = CoreUtils.getLogPreString() + "CoreUpdaterJob | ";
    }

    /**
     * Process profile to the client.
     */
    private void processRequest() {
        logging.info(logPreString
                + "Starting a job with Mobile number: "
                + accounts.getMsisdn());
        //calculateTimeSpent();
        postAccounts();
        logging.info(logPreString
                + "Closing a job with mobile number "
                + accounts.getMsisdn());

    }

    /**
     * Post session data to CORE.
     */
    @SuppressWarnings("null")
    private void postAccounts() {

        /**
         * The JSON Reply from Hub.
         */
        String jsonReply = "";
        /**
         * The status code to update the record.
         */
        int statusCode = 3;
        /**
         * Status Description to update for the record.
         */
        String statusDescription = "";
        /**
         * The status code from Hub.
         */
        int coreStatCode = 0;

        /**
         * The status description from CORE API.
         */
        String coreStatDescription = "";

        HttpPost httppost;
        HttpParams httpParams;
        HttpClient httpclient;
        HttpResponse response;
        String messageResponse = "";

        try {

            logging.info(logPreString + "Formulating post to API "
                    + "Parameters - "
                    + "  Mobile number " + accounts.getMsisdn()
                    + ", smsID " + accounts.getSmsID()
                    + ", message: " + accounts.getMessage());

            String username = "argila";
            String apiKey = "3c8d27d51601c87bdb90756a17dabe2e2da59a72728ba5cd5aa81832888d090c";
            String message = "";
            // Create a new instance of our awesome gateway class
            AfricasTalkingGateway gateway = new AfricasTalkingGateway(username, apiKey);

            logging.info(logPreString + "Response from the API: "
                    + gateway.toString());

            JSONArray results = gateway.sendMessage(accounts.getMsisdn(), accounts.getMessage());
            for (int i = 0; i < results.length(); ++i) {
                JSONObject result = results.getJSONObject(i);
                logging.info(logPreString + "Response from the API: "
                        + result.toString());
                if ((result.getString("status").compareToIgnoreCase("success")) == 0) {
                    logging.info(logPreString + "Response from the API was a succes.: "
                            + result.toString());
                    statusCode = props.getProcessedStatus();
                }
                //statusCode = props.getRetryStatus();
//                    System.out.print(result.getString("status") + ","); // status is either "Success" or "error message"
//                    System.out.print(result.getString("number") + ",");
//                    System.out.print(result.getString("messageId") + ",");
//                    System.out.println(result.getString("cost"));
            }
        } catch (ParseException ex) {
            logging.error(logPreString
                    + "A ParseException has been caught while decoding the "
                    + "reply." + jsonReply + " Error Message: "
                    + ex.getMessage());

            statusDescription = "A ParseException has been caught. Response "
                    + "from CORE: " + ex.getMessage();
        } catch (Exception e) {
            logging.error(logPreString + "Response from the API: " + e.getMessage());
        }
        logging.info(logPreString
                + "Record processed,"
                + " Mobile Number : "
                + accounts.getMsisdn());
        updateTransaction(statusCode, statusDescription);
    }

    private void updateTransaction(int statusCode, String statusDescription) {

        String[] params = {
            String.valueOf(statusCode),
            statusDescription,
            String.valueOf(accounts.getSmsID())
        };

        String query = "UPDATE sms_requests "
                + " SET status =  ? ,statusDesription = ?"
                + " WHERE smsID = ?";

        try (Connection conn = mysql.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, statusCode);
            stmt.setString(2, statusDescription);
            stmt.setInt(3, accounts.getSmsID());

            logging.info(logPreString
                    + "Updating Record with Accounts "
                    + accounts.getMsisdn()
                    + query);
            stmt.executeUpdate();

            logging.info(logPreString
                    + "Record processed. Mobile number: "
                    + accounts.getMsisdn());
        } catch (SQLException ex) {
            logging.error(logPreString
                    + "An " + ex.getClass().getName() + " occured while "
                    + "updating the customerProfile record | "
                    + accounts.getMsisdn() + ". Error: "
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

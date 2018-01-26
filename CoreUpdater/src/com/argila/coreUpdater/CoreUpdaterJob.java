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
                + "Starting a job with customer account number: "
                + accounts.getAccountNumber());
        //calculateTimeSpent();
        postAccounts();
        logging.info(logPreString
                + "Closing a job with customer Account number "
                + accounts.getAccountNumber());

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
        int statusCode = 0;
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

            String payload = generatePayload(accounts
            );

            logging.info(logPreString + "Formulating post to API "
                    + "Parameters - "
                    + "  AccountNumber " + accounts.getAccountNumber()
                    + ", TimeSpent: " + accounts.getTimeSpent()
                    + ", AmountSpent: " + accounts.getAmountSpent()
                    + ", AmountBalance: " + accounts.getAmountBalance()
                    + ", Payload: ");

            if (payload.isEmpty() || payload == null) {
                logging.info(logPreString + "Action stop trigged."
                        + " No message will be sent: Exiting ...");
                return;
            }

            String username = "argila";
            String apiKey = "3c8d27d51601c87bdb90756a17dabe2e2da59a72728ba5cd5aa81832888d090c";
            String message = "";
            if (accounts.getTimeSpent() == 0 || accounts.getTimeSpent() < 0) {
                message = "Dear customer, you have started a session at Big Sqaure-Karen. Thank you for choosing Tap&Charge. Deal of the week! 1/4Chicken+Chips for only 550/=";
                // payloadString = "https://api.africastalking.com/restless/send?username=argila&Apikey=3c8d27d51601c87bdb90756a17dabe2e2da59a72728ba5cd5aa81832888d090c&to="
                //         + accounts.getMsisdn() + "&message=" + message;
            } else {
                logging.info(logPreString + "Action stop trigged."
                        + " No message will be sent: Exiting ...");
                return;
            }
            // Create a new instance of our awesome gateway class
            AfricasTalkingGateway gateway = new AfricasTalkingGateway(username, apiKey);

            logging.info(logPreString + "Response from the API: "
                    + gateway.toString());

            JSONArray results = gateway.sendMessage(accounts.getMsisdn(), message);
            for (int i = 0; i < results.length(); ++i) {
                JSONObject result = results.getJSONObject(i);
                logging.info(logPreString + "Response from the API: "
                        + result.toString());
                if ((result.getString("status").compareToIgnoreCase("success")) == 0) {
                    logging.info(logPreString + "Response from the API was a succes.: "
                            + result.toString());
                    statusCode = 1;
                }
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
                + " Account Number : "
                + accounts.getAccountNumber());
        updateTransaction(statusCode, statusDescription);
    }

    private void updateTransaction(int statusCode, String statusDescription) {

        String[] params = {
            String.valueOf(statusCode),
            statusDescription,
            String.valueOf(accounts.getSessionDataID())
        };

        String query = "UPDATE sessionData "
                + " SET syncStatus =  ? , syncStatusDescription = ?"
                + " WHERE sessionDataID = ?";

        try (Connection conn = mysql.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, statusCode);
            stmt.setString(2, statusDescription);
            stmt.setInt(3, accounts.getSessionDataID());

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

    /**
     * Generate the post payload.
     *
     * @param accounts object holding data
     * @param username
     * @param password
     * @return Payload
     */
    private String generatePayload(AccountsData accounts) throws ParseException {
        String payload = "";
        String payloadString = null;
        Map<String, Object> packet = new HashMap<>();

        Map<String, Object> fullPayload = new HashMap<>();
        Map<String, Object> finalPayload = new HashMap<>();
        packet.put("timeSpent", accounts.getTimeSpent());
        packet.put("amountSpent", accounts.getAmountSpent());
        packet.put("amountBalance", accounts.getAmountBalance());

        packet.put("accountNumber",
                accounts.getAccountNumber());
        if (accounts.getTimeSpent() == 0 || accounts.getTimeSpent() < 0) {
            packet.put("action", Constants.ACTION_START);
        } else {
            packet.put("action", Constants.ACTION_STOP);
        }

        Object[] params = new Object[]{packet};
        fullPayload.put("packet", params);

        JSONObject payloadObj = new JSONObject(fullPayload);
        finalPayload.put("payload", String.valueOf(payloadObj));
        payload = String.valueOf(packet);

        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        SimpleDateFormat sDFormat
                = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss:sss");
        sDFormat.setTimeZone(timeZone);
        String timestampDateString = sDFormat.format(calendar.getTime());

        String action;
        if (accounts.getTimeSpent() == 0 || accounts.getTimeSpent() < 0) {
            action = Constants.ACTION_START;
            String url = props.getCoreAPI();
            String message = "Dear customer, you have started a session at KFC-TheHub. At " + timestampDateString + ". Thank you for choosing Tap&Charge";
            payloadString = "https://api.africastalking.com/restless/send?username=argila&Apikey=3c8d27d51601c87bdb90756a17dabe2e2da59a72728ba5cd5aa81832888d090c&to="
                    + accounts.getMsisdn() + "&message=" + message;
        } else {
            action = Constants.ACTION_STOP;
        }

        logging.info(logPreString
                + "Message Type " + action);
        /*temp fix
         String url = props.getCoreAPI();
         String payloadString = "?accountNumber="
         + accounts.getAccountNumber() + "&amountBalance="
         + accounts.getAmountBalance()
         + "&amountSpent=" + accounts.getAmountSpent()
         + "&timeSpent=" + accounts.getTimeSpent()
         + "&timeStamp=" + timestampDateString
         + "&location=" + accounts.getLocationName()
         + "&action=" + action;
         url += payloadString;
         **/
        return payloadString;
    }

    /**
     * Runs the accounts.
     */
    @Override
    public void run() {
        processRequest();
    }

}

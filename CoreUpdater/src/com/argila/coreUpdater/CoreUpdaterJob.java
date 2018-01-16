/**
 * To change this license header, choose License Headers in Project Properties
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 *
 * @auther Lewis Kimani <kimanilewi@gmail.com>
 */
package com.argila.coreUpdater;

import com.argila.coreUpdater.db.MySQL;
import com.argila.coreUpdater.utils.CoreUtils;
import com.argila.coreUpdater.utils.Logging;
import com.argila.coreUpdater.utils.Props;
import com.argila.coreUpdater.utils.Constants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

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
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
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

            httppost = new HttpPost(payload);
            httppost.setHeader("Authorization", props.getAuthorizationKey());
            httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams,
                    props.getConnectionTimeout());
            HttpConnectionParams.setSoTimeout(httpParams,
                    props.getReplyTimeout());
            httpclient = new DefaultHttpClient(httpParams);

            logging.info(logPreString + "Formulating post to API: "
                    + "URL to invoke - " + props.getCoreAPI());
            // Execute and get the response
            response = httpclient.execute(httppost);

            if (response != null) {

                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(
                                response.getEntity().getContent()));
                StringBuilder content = new StringBuilder(0);
                String line;
                while ((line = rd.readLine()) != null) {
                    content.append(line);
                }
                jsonReply = content.toString();

                logging.info(logPreString + "Response from the API: "
                        + content.toString());

                if (!jsonReply.isEmpty()) {
                    JSONObject jsonResp = new JSONObject(
                            content.toString());
                    if (jsonResp.has("msg")) {
                        coreStatDescription = jsonResp.getString("msg");
                    }
                    if (jsonResp.has("status")) {
                        coreStatCode = jsonResp.getInt("status");
                    }
                    if (jsonResp.has("message")) {
                        messageResponse = jsonResp.getString("message");
                    }

                    /*
                     * Check if the message was processed successfully
                     * and it is the message that was sent.
                     */
                    if (coreStatCode == props.getAuthSuccessCode()) {

                        logging.info(
                                logPreString
                                + "The API invocation was successful. Params returned:: "
                                + ",  Status Code:" + coreStatCode
                                + ",  Core Status Description Status : " + coreStatDescription
                        );
                        statusDescription = "Processed Successfully ";

                        statusCode = props.getFinishedProcessingStatus();

                    } else {

                        statusDescription = "There was an error "
                                + "processing this message on CORE "
                                + "Status Description: " + messageResponse;

                        logging.info(logPreString
                                + "There was an error "
                                + "processing this message on CORE. "
                                + "Status Description: " + statusDescription);
                        statusCode = props.getProcessingStatus();
                    }
                } else {
                    logging.info(logPreString
                            + "The API invocation returned a "
                            + "response but was empty.");
                    statusDescription = "Received an empty "
                            + "response from the API";
                }
            } else {
                statusDescription = "No response received from the CORE API";

                logging.error(logPreString
                        + "The API invocation failed. No response received from the CORE "
                        + "response.");
            }
        } catch (ClientProtocolException ex) {
            logging.error(logPreString
                    + "An ClientProtocolException has been caught while "
                    + "invoking the HUB API. Error Message: "
                    + ex.getMessage());

            statusDescription = "Error setting the client protocol"
                    + ex.getMessage();

        } catch (UnsupportedEncodingException ex) {
            logging.error(logPreString
                    + "An UnsupportedEncodingException has been caught "
                    + "while invoking the CORE API. Error Message: "
                    + ex.getMessage());

            statusDescription = "Error Encoding the message to send"
                    + ex.getMessage();

        } catch (IOException ex) {
            logging.error(logPreString
                    + "An IOException has been caught while invoking the "
                    + "CORE API. Error Message: " + ex.getMessage());

            statusDescription = "IOException caught while processing"
                    + ex.getMessage();

        } catch (JSONException ex) {
            logging.error(logPreString
                    + "A JSONException has been caught while decoding the "
                    + "reply." + jsonReply + " Error Message: "
                    + ex.getMessage());

            statusDescription = "A JSONException has been caught. Response "
                    + "from CORE: " + ex.getMessage();

        } catch (ParseException ex) {
            logging.error(logPreString
                    + "A ParseException has been caught while decoding the "
                    + "reply." + jsonReply + " Error Message: "
                    + ex.getMessage());

            statusDescription = "A ParseException has been caught. Response "
                    + "from CORE: " + ex.getMessage();
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
        String url = props.getCoreAPI();
        String message = "Dear customer, you have started a session at KFC-TheHub. At " + timestampDateString + ". Thank you for choosing Tap&Charge";
        String payloadString = "https://api.africastalking.com/restless/send?username=argila&Apikey=3c8d27d51601c87bdb90756a17dabe2e2da59a72728ba5cd5aa81832888d090c&to="
                + accounts.getMsisdn() + "&message=" + message;
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

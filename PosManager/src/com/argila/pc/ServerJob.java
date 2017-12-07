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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.params.HttpParams;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
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
            String locationID;
//            try (InputStream input = clientSocket.getInputStream();
//                    OutputStream output = clientSocket.getOutputStream()) {
            try (BufferedReader input
                    = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());) {
                //  messageByte[0] = (byte) input.read();

                messageString = input.readLine();
                logging.info(CoreUtils.getLogPreString() + "init() |"
                        + " Request was processed: well String is.." + messageString);

                String[] incomingMessage = messageString.split("\\|");
                logging.info(CoreUtils.getLogPreString() + "init() |"
                        + " Request was processed: AccountNumber received is.." + Arrays.toString(incomingMessage));
                accountNumber = incomingMessage[0]; //account number;
                batteryLevel = incomingMessage[1];
                locationID = incomingMessage[2];
                accountsData.setLocationID(locationID);
                logging.info(CoreUtils.getLogPreString() + "init() |"
                        + " Request was processed: AccountNumber received is.." + accountNumber);
                logging.info(CoreUtils.getLogPreString() + "init() |"
                        + " Request was processed: Battery level received is.." + batteryLevel);
                logging.info(CoreUtils.getLogPreString() + "init() |"
                        + " Request was processed: Location ID received is.." + locationID);
                int responseTime = checkAccountValidity(accountNumber);
                int response = responseTime / 60;
                int maxTime = props.getMaxTime() / 60;
                int minTime = props.getMinTime() / 60;
                logging.info(CoreUtils.getLogPreString() + "init() |"
                        + " Response is.." + response
                        + " maxTime is.." + maxTime
                        + " minTime is.." + minTime
                );
                //initiateCheckout();
                if (responseTime == -1) {
                    triggerStopSession();
                    outPutString = "SC9999Z";
                } else if (response > maxTime && maxTime < 10) {

                    accountsData.setExpiryTime(props.getMaxTime());
                    updateProcessingState();
                    outPutString = "SC000" + String.valueOf(maxTime + "Z");
                    updateProcessingState();

                } else if (response >= maxTime && maxTime > 9) {
                    accountsData.setExpiryTime(props.getMaxTime());
                    updateProcessingState();
                    outPutString = "SC00" + String.valueOf(maxTime + "Z");
                    updateProcessingState();

                } else if (response < maxTime
                        && response >= minTime && response > 9) {
                    accountsData.setExpiryTime(response * 60);
                    updateProcessingState();

                    outPutString = "SC00" + String.valueOf(response + "Z");
                } else if (response < maxTime
                        && response >= minTime && response < 9) {
                    accountsData.setExpiryTime(response * 60);
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

        String query = "SELECT accountNumber, "
                + "TIMESTAMPDIFF(SECOND,NOW(),cpa.expiryDate) as expiryTime, "
                + " cpa.customerProfileAccountID, cpa.expiryDate,"
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
                    accountsData.setExpiryDate(rs.getString("expiryDate"));
                    status = rs.getInt("processingStatus");
                    int expiryTime = rs.getInt("expiryTime");

                    if (expiryTime > 0 && status != props.getProcessingStatus()) {
                        time = expiryTime;
                    } else if (expiryTime > 0 && status == props.getProcessingStatus()) {
                        time = 0;
                        time = -1;

                    }

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
        } finally {
            isCurrentPoolShutDown = true;
        }
        return time;

    }

    /**
     * Update the processing state of the Account.
     *
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
                + " expiryTime = DATE_ADD( NOW(), INTERVAL ? MINUTE), locationID=? "
                + " WHERE customerProfileAccountID = ? ";
        String[] params = {
            String.valueOf(props.getProcessingStatus()),
            String.valueOf(accountsData.getExpiryTime()),
            String.valueOf(accountsData.getLocationID()),
            String.valueOf(accountsData.getCustomerProfileAccountID())
        };
        trueQuery = CoreUtils.prepareSqlString(query, params, 0);

        try {

            conn = mysql.getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, props.getProcessingStatus());
            stmt.setInt(2, accountsData.getExpiryTime());
            stmt.setString(3, accountsData.getLocationID());
            stmt.setInt(4, accountsData.getCustomerProfileAccountID());

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
                    + "startTime = NOW(), "
                    + "`expiryTime` = DATE_ADD( NOW(), INTERVAL ? MINUTE)"
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

    private String generatePayload(AccountsData accounts) throws ParseException {
        String payload = "";
        Map<String, Object> packet = new HashMap<>();

        Map<String, Object> fullPayload = new HashMap<>();
        Map<String, Object> finalPayload = new HashMap<>();
        packet.put("transaction_id", "254706604938".concat(String.valueOf(System.currentTimeMillis())));
        packet.put("reference_id", "1234"); // accounts.getCustomerProfileID());
        packet.put("amount", "10");//accounts.getAmount());
        packet.put("phone", "254706604938");
        packet.put("callback", "http://69.64.82.36:9005/");

        Object[] params = new Object[]{packet};
        fullPayload.put("packet", params);

        JSONObject payloadObj = new JSONObject(packet);
        finalPayload.put("payload", String.valueOf(payloadObj));
        payload = String.valueOf(payloadObj);
        return payload;
    }

    /**
     * Post session data to CORE.
     */
    @SuppressWarnings("null")
    private void initiateCheckout() {
        String logPreString = " initiateCheckout() | ";
        logging.info(logPreString + "Formulating post to Checkout "
                + "Parameters - "
                + "  AccountNumber " + accountsData.getAccountNumber()
                + ", Payload: ");
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

            String payload = generatePayload(accountsData);

            logging.info(logPreString + "Formulating post to Checkout "
                    + "Parameters - "
                    + "  AccountNumber " + payload
                    + ", Payload: ");
            httppost = new HttpPost(props.getMpesaCheckoutAPI());
            StringEntity entity = new StringEntity(payload);
            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json;charset=UTF-8"));
            httppost.setHeader("Accept", "application/json");
            httppost.setEntity(entity);

            httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams,
                    props.getConnectionTimeout());
            HttpConnectionParams.setSoTimeout(httpParams,
                    props.getReplyTimeout());

            httpclient = new DefaultHttpClient(httpParams);
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
                    logging.info(
                            logPreString
                            + "The API invocation was successful."
                            + " Params returned:: " + jsonResp.toString());
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

                        statusCode = props.getProcessedStatus();

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
                + accountsData.getAccountNumber());
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

package com.cellulant.CoreBroadcastProcessor;

import com.cellulant.CoreBroadcastProcessor.db.MySQL;
import com.cellulant.CoreBroadcastProcessor.utils.Logging;
import com.cellulant.CoreBroadcastProcessor.utils.PaymentPusherConstants;
import com.cellulant.CoreBroadcastProcessor.utils.Props;
import com.cellulant.CoreBroadcastProcessor.utils.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;

import org.apache.http.client.entity.UrlEncodedFormEntity;

import org.apache.http.client.methods.HttpPost;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import org.apache.http.message.BasicNameValuePair;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Job thread class.
 *
 * @author Brian Ngure
 */
@SuppressWarnings({"FinalClass", "ClassWithoutLogger"})
public final class BroadcastProcessorJob implements Runnable {

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
     * Payment data object.
     */
    private final BroadcastData paymentData;

    /**
     * Class Constructor.
     *
     * @param log log class instance
     * @param prop properties class instance
     * @param mySQL mysql pool class instance
     * @param data payment data
     */
    public BroadcastProcessorJob(final Logging log, final Props prop,
            final MySQL mySQL, final BroadcastData data) {
        logging = log;
        props = prop;
        mysql = mySQL;
        paymentData = data;
    }

    /**
     * Process payment to the client.
     */
    private void processRequest() {
        logging.info(Utilities.getLogPreString()
                + "processRequest() | "
                + "Starting a job with reward Recipient ID : "
                + paymentData.getRewardRecipientID());

        postPaymentPayment();

        logging.info(Utilities.getLogPreString()
                + "postPaymentPayment() | "
                + paymentData.getBeepTransactionID() + " | "
                + "Closing the job with reward Recipient ID : "
                + paymentData.getRewardRecipientID());
    }

    /**
     * Post the payment to the Wrapper scripts.
     */
    @SuppressWarnings("null")
    private void postPaymentPayment() {
        /**
         * The JSON Reply from Hub.
         */
        String jsonReply = "";
        /**
         * The HTTP status code returned.
         */
        int httpStatusCode;
        /**
         * The status description to be saved.
         */
        String statusDescription = "";
        String authStatusDescription = "";
        /**
         * Status code for the payment pusher processing state.
         */
        int processStatusCode = 0;

        /**
         * The status code from wrappers.
         */
        int retStatusCode = 0;
        /**
         * The BeepTransactionID returned from the wrappers.
         */
        int retRequestLogID;
        /**
         * The payerTransactionID returned from wrappers.
         */
        String retPayerTID;
        /**
         * The status description from Pusher wrappers.
         */
        String retStatusDesc;
        /**
         * The receipt Number returned.
         */
        String receiptNumber = "";
        /**
         * The status code used to acknowledge the transaction.
         */
        int processingStatusCode = 0;
        int authStatusCode = 0;
        String retPayerTransactionID = "";
        /**
         * Acknowledge message.
         */
        String acknowledgementMessage = "";
        HttpPost httppost;
        HttpParams httpParams;
        HttpClient httpclient;
        HttpResponse response;
        try {
            String payload = generatePayload(paymentData);
            httppost = new HttpPost(props.getPostPaymentAPIUrl());
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

            logging.info(
                    Utilities.getLogPreString() + "postPaymentPayment() | "
                    + "Formulating Parameters to post to the API  -"
                    + " Payer Transaction ID: "
                    + paymentData.getRewardRecipientID()
                    + ", Destination Account: "
                    + paymentData.getAccountNumber()
                    + ", Amount: " + paymentData.getAmount()
                    + " and service ID: "
                    + paymentData.getServiceID()
                    + ". Posting to: " + props.getPostPaymentAPIUrl());

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
                logging.info(Utilities.getLogPreString() + "Response from the API: "
                        + content.toString());

                if (!jsonReply.isEmpty()) {
                    JSONObject jsonResp = new JSONObject(
                            content.toString());
                    if (jsonResp.has("authStatus")) {

                        authStatusCode = jsonResp.getJSONObject("authStatus")
                                .getInt("authStatusCode");
                        authStatusDescription
                                = jsonResp.getJSONObject("authStatus")
                                .getString("authStatusDescription");

                        if (authStatusCode != props.getAuthSuccessCode()) {

                            //Failed authentication
                            logging.error(Utilities.getLogPreString()
                                    + "Authentication failed, Status code returned: "
                                    + authStatusCode + ", Status Description: "
                                    + authStatusDescription);

                            return;
                        }

                        /*
                         * Check if the message was processed successfully
                         * and it is the message that was sent.
                         */
                        JSONArray results = jsonResp.getJSONArray("results");
                        if (results.length() > 0) {
                            JSONObject resp = (JSONObject) results.get(0);
                            logging.info(
                                    Utilities.getLogPreString()
                                    + "The API invocation was successful. Params returned:: "
                                    + results.toString()
                            );
                            retStatusCode = resp.getInt("statusCode");
                            retRequestLogID = resp.getInt("beepTransactionID");
                            retPayerTransactionID = resp.getString("payerTransactionID");
                            statusDescription = resp.getString("statusDescription");

                            //check if the message was processed successfully and
                            //it is the message thatwas sent
                            if (retPayerTransactionID.compareTo(String.valueOf(
                                    paymentData.getRewardRecipientID())) == 0
                                    && retStatusCode == props.getNewPaymentStatus()) {
                                logging.info(Utilities.getLogPreString()
                                        + "The transaction has "
                                        + "been successfully posted to Hub "
                                        + "The Returned status is:"
                                        + retStatusCode
                                        + ". status description is: " + statusDescription);
                                paymentData.setBeepTransactionID(retRequestLogID);
                                processingStatusCode = 1;
                            } else if (retPayerTransactionID.compareTo(String.valueOf(
                                    paymentData.getRewardRecipientID())) == 0
                                    && retStatusCode != props.getNewPaymentStatus()) {
                                logging.info(Utilities.getLogPreString()
                                        + "The transaction WAS "
                                        + " NOT successfully posted to Hub "
                                        + "The Returned status is:"
                                        + retStatusCode
                                        + ". status description is: " + statusDescription);
                                processingStatusCode = 1;
                            } else {
                                logging.info(Utilities.getLogPreString()
                                        + "The transaction FAILED while "
                                        + " posting to Hub "
                                        + "The Returned status is:"
                                        + retStatusCode
                                        + ". status description is: " + statusDescription);
                            }
                        }
                    } else {
                        statusDescription = "There was an error "
                                + "processing this message on CORE ";

                        logging.info(Utilities.getLogPreString()
                                + "There was an error "
                                + "processing this message on CORE. "
                                + "Status Description: " + statusDescription);
                    }
                } else {
                    logging.info(Utilities.getLogPreString()
                            + "The API invocation returned a "
                            + "response but was empty.");
                    statusDescription = "Received an empty "
                            + "response from the API";

                }
            } else {
                statusDescription = "No response received from the CORE API";

                logging.error(Utilities.getLogPreString()
                        + "The API invocation failed. "
                        + "No response received from the CORE "
                        + "response.");
            }
        } catch (ClientProtocolException ex) {
            logging.error(Utilities.getLogPreString()
                    + "An ClientProtocolException has been caught while "
                    + "invoking the HUB API. Error Message: "
                    + ex.getMessage());

            statusDescription = "Error setting the client protocol"
                    + ex.getMessage();

        } catch (UnsupportedEncodingException ex) {
            logging.error(Utilities.getLogPreString()
                    + "An UnsupportedEncodingException has been caught "
                    + "while invoking the CORE API. Error Message: "
                    + ex.getMessage());

            statusDescription = "Error Encoding the message to send"
                    + ex.getMessage();

        } catch (SocketTimeoutException ex) {

            logging.error(Utilities.getLogPreString()
                    + "An " + ex.getClass().getCanonicalName()
                    + " was caught while invoking the hub "
                    + "API Url :  "
                    + props.getPostPaymentAPIUrl() + ". Error Message: "
                    + ex.getMessage());
            statusDescription = " Error was caught while invoking the hub with"
                    + " Error Message: " + ex.getMessage();

        } catch (NoHttpResponseException | ConnectTimeoutException |
                SocketException ex) {

            logging.error(Utilities.getLogPreString()
                    + "An " + ex.getClass().getCanonicalName()
                    + " was caught while invoking the hub "
                    + " API Url: "
                    + props.getPostPaymentAPIUrl() + ". Error Message: "
                    + ex.getMessage());
            statusDescription = " Error was caught while invoking the hub with"
                    + " Error Message: " + ex.getMessage();

        } catch (IOException ex) {
            logging.error(Utilities.getLogPreString()
                    + "A " + ex.getClass().getCanonicalName()
                    + " was caught while invoking the "
                    + "payment wrapper. Error Message: "
                    + ex.getMessage());
            statusDescription = " Error was caught while invoking the hub with"
                    + " Error Message: " + ex.getMessage();

        } catch (JSONException | IllegalStateException ex) {
            logging.error(Utilities.getLogPreString() + "A " + ex.getClass().getCanonicalName()
                    + " was caught while invoking the "
                    + " fetch Payment status. Error Message: "
                    + ex.getMessage());
            statusDescription = " Error was caught while invoking the hub with"
                    + " Error Message: " + ex.getMessage();

        } catch (ParseException ex) {
            logging.error(Utilities.getLogPreString()
                    + "A ParseException has been caught while decoding the "
                    + "reply." + jsonReply + " Error Message: "
                    + ex.getMessage());

            statusDescription = "A ParseException has been caught. Response "
                    + "from CORE: " + ex.getMessage();
        }

        updateTransaction(retPayerTransactionID, retStatusCode, statusDescription, processingStatusCode);
    }

    /**
     * Acknowledges the transaction.
     *
     * @param returnedRequestLogID The out message ID returned as client SMS ID
     * @param returnedPayerTrxID the returned payer transaction ID
     * @param statusCode status code to update to
     * @param statusDescription status description
     */
    private void updateTransaction(
            final String returnedPayerTrxID, final int statusCode,
            final String statusDescription, final int processingStatusCode) {
        String localStatusDescription = statusDescription;
        int localStatusCode = statusCode;

        int numberOfSends = paymentData.getNumberOfSends();
        if (processingStatusCode != props.getUnprocessedStatus()) {
            numberOfSends = paymentData.getNumberOfSends() + 1;
        }
        String addQuery = "";
        String trueQuery;

        if (paymentData.getBeepTransactionID() > 0) {
            addQuery = ", beepTransactionID =  " + paymentData.getBeepTransactionID();

        }

        if (localStatusDescription.length() >= 255) {
            localStatusDescription = localStatusDescription.substring(0, 252)
                    + "...";
        }

        localStatusDescription = localStatusDescription.replace('\"', '\'');

        String[] params = {
            String.valueOf(processingStatusCode),
            String.valueOf(localStatusDescription),
            String.valueOf(props.getNextSendInterval()),
            String.valueOf(numberOfSends),
            String.valueOf(paymentData.getRewardRecipientID())
        };

        logging.info(Utilities.getLogPreString() + "updateTranaction() | "
                + "Updating transaction with reward Recipient ID "
                + paymentData.getRewardRecipientID() + " with the following values: "
                + "postpayment status code: " + localStatusCode + ", "
                + "Push status description: " + localStatusDescription
                + ", Number of sends " + numberOfSends);

        String query = "UPDATE rewardRecipients SET processStatus = ?, "
                + "postPaymentStatus = ?, postPaymentStatusDesc = ?, "
                + "numberOfSends = ? " + addQuery
                + " WHERE rewardRecipientID = ? ";

        trueQuery = Utilities.prepareSqlString(query, params, 0);

        try (Connection conn = mysql.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, processingStatusCode);
            stmt.setInt(2, localStatusCode);
            stmt.setString(3, localStatusDescription);
            stmt.setInt(4, numberOfSends);
            stmt.setInt(5, paymentData.getRewardRecipientID());

            logging.info(Utilities.getLogPreString() + "updateTranaction() | "
                    + "Updating Record with Reward Recipient ID: "
                    + paymentData.getRewardRecipientID()
                    + " using query:" + trueQuery);
            stmt.executeUpdate();

        } catch (SQLException ex) {
            logging.error(Utilities.getLogPreString() + "updateTranaction() | "
                    + "An " + ex.getClass().getName() + " occured while "
                    + "updating the record with "
                    + "Reward Recipient ID: "
                    + paymentData.getRewardRecipientID() + ". Error: "
                    + ex.getMessage());
            query = "UPDATE rewardRecipients SET processStatus = ?, "
                    + "postPaymentStatus = ?, postPaymentStatusDesc = \"?\", "
                    + "numberOfSends = ? " + addQuery
                    + "WHERE rewardRecipientID = ?";

            String trueStoreQuery = Utilities.prepareSqlString(query, params,
                    0);
            Utilities.updateFile(logging, paymentData.getRewardRecipientID() + "_"
                    + paymentData.getServiceID() + "_"
                    + PaymentPusherConstants.FAILED_QUERIES_FILE,
                    trueStoreQuery
            );
        }
    }

    /**
     * Generate the post payload.
     *
     * @param paymentData object holding data
     * @param username
     * @param password
     * @return Payload
     */
    private String generatePayload(BroadcastData paymentData) throws ParseException {
        String beepUsername = props.getHubUsername();
        String beepPassword = props.getHubPassword();
        Map<String, Object> postPaymentPayload = new HashMap<>();
        Map<String, String> credentialsMap = new HashMap<>();
        Map<String, Object> packetMap = new HashMap<>();

        credentialsMap.put("username", beepUsername);
        credentialsMap.put("password", beepPassword);

        packetMap.put("serviceID", paymentData.getServiceID());
        packetMap.put("accountNumber", paymentData.getAccountNumber());
        packetMap.put("payerTransactionID", paymentData.getRewardRecipientID());
        packetMap.put("amount", paymentData.getAmount());
        packetMap.put("narration", "Caviar reward payment");
        packetMap.put("currencyCode", props.getDefaultCurrencyCode());
        packetMap.put("datePaymentReceived", paymentData.getDateCreated());
//        packetMap.put("currencyCode", paymentData.getCurrencyCode());
        packetMap.put("MSISDN", paymentData.getMSISDN());

        List packets = new ArrayList();
        packets.add(packetMap);
        postPaymentPayload.put("credentials", credentialsMap);
        postPaymentPayload.put("packet", packets);

        logging.info(Utilities.getLogPreString()
                + " API payload request map: " + packetMap.toString());

        Map obj = new HashMap();
        obj.put("function", props.getHubPostPaymentFunction());
        obj.put("payload", new JSONObject(postPaymentPayload).toString());

        JSONObject payloadJSONObject = new JSONObject(obj);

        return payloadJSONObject.toString();
    }

    /**
     * Runs the task.
     */
    @Override
    public void run() {
        processRequest();
    }
}

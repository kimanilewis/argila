package com.cellulant.CoreBroadcastProcessor.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.Iterator;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;

import org.apache.http.client.ClientProtocolException;

import org.apache.http.client.methods.HttpPost;

import org.apache.http.conn.ConnectTimeoutException;

import org.apache.http.entity.StringEntity;

import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.message.BasicHeader;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import org.apache.http.protocol.HTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/**
 * Utility class.
 *
 * @author dennis
 */
@SuppressWarnings({"FinalClass", "ClassWithoutLogger"})
public final class Utilities {
    /**
     * Prepended text added to each log message.
     *
     * @return "PaymentPusher | "
     */
    public static String getLogPreString() {
        return "PaymentPusher | ";
    }

    /**
     * Prepares the statement to store in the file.
     *
     * @param query the query string
     * @param params the parameters
     * @param index the parameter index
     *
     * @return the prepared query string
     */
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public static String prepareSqlString(final String query,
            final String[] params, final int index) {
        int localIndex = index;

        if (!query.contains("?")) {
            return query;
        }

        String s = query.replaceFirst("\\?", params[localIndex]);

        return prepareSqlString(s, params, ++localIndex);
    }

    /**
     * Call the Hub API.
     *
     * @param logging the log class instance
     * @param props the properties class instance
     * @param obj the payload data
     *
     * @return the Hub response
     */
    public static Object callBeepAPI(final Logging logging, final Props props,
            final JSONObject obj) {
        String logPreString = "Utilities | callBeepAPI() | ";
        ExecutorService executor = Executors.newCachedThreadPool();

        Callable<Object> task = new Callable<Object>() {
            @Override
            public Object call() {
                String logPreString = "Utilities | callBeepAPI() | ";
                try {
                    HttpPost request = new HttpPost(props.getHubJsonAPIUrl());
                    JSONStringer json = new JSONStringer();
                    StringBuilder sb = new StringBuilder(0);

                    if (obj != null) {
                        @SuppressWarnings("unchecked")
                        Iterator<String> itKeys = obj.keys();
                        if (itKeys.hasNext()) {
                            json.object();
                        }
                        while (itKeys.hasNext()) {
                            String k = itKeys.next();
                            json.key(k).value(obj.get(k));
                        }
                    }
                    json.endObject();

                    StringEntity entity = new StringEntity(json.toString());
                    entity.setContentType("application/json;charset=UTF-8");
                    entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                            "application/json;charset=UTF-8"));
                    request.setHeader("Accept", "application/json");
                    request.setEntity(entity);

                    HttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams,
                            props.getConnectionTimeout());
                    HttpConnectionParams.setSoTimeout(httpParams,
                            props.getReplyTimeout());

                    DefaultHttpClient httpClient
                            = new DefaultHttpClient(httpParams);

                    HttpResponse response = httpClient.execute(request);

                    InputStream in = response.getEntity().getContent();
                    BufferedReader reader
                            = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);

                    }

                    return sb.toString();
                } catch (ClientProtocolException | UnsupportedEncodingException
                        | SocketTimeoutException | NoHttpResponseException
                        | ConnectTimeoutException | SocketException ex) {
                    logging.error(logPreString
                            + "An " + ex.getClass().getCanonicalName()
                            + " was caught while invoking the hub "
                            + "acknowledgement function "
                            + "(" + props.getHubPostPaymentFunction() + ") on "
                            + props.getHubJsonAPIUrl() + ". Error Message: "
                            + ex.getMessage());
                    return null;
                } catch (IOException ex) {
                    logging.error(logPreString
                            + "A " + ex.getClass().getCanonicalName()
                            + " was caught while invoking the "
                            + "payment wrapper. Error Message: "
                            + ex.getMessage());
                    return null;
                } catch (JSONException | IllegalStateException ex) {
                    logging.error(logPreString + "A error during call: "
                            + ex.getMessage());
                    return null;
                }
            }
        };

        Future<Object> future = executor.submit(task);

        Object result = null;
        try {
            result = future.get(45, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            logging.fatal(logPreString + " | Timeout: "
                    + ex.getMessage());
        } catch (InterruptedException ex) {
            logging.fatal(logPreString + " | Interrupted: "
                    + ex.getMessage());
        } catch (ExecutionException ex) {
            logging.fatal(logPreString + " | Execution error: "
                    + ex.getMessage());
        } finally {
            future.cancel(true);
            executor.shutdown();
        }

        return result;
    }

    /**
     * Update a transaction.
     *
     * @param logging the log class instance
     * @param props the properties class instance
     * @param obj the payload data
     * @param beepTransactionID the beep transaction ID
     * @param statusCode the status code
     * @param isRetry is a retry
     * @param serviceID the service ID
     * @param serviceCode the service code
     *
     * @return true if updated, false otherwise
     */
    public static boolean updateTransaction(final Logging logging,
            final Props props, final JSONObject obj,
            final int beepTransactionID, final int statusCode,
            final boolean isRetry, final int serviceID,
            final String serviceCode) {
        String logPreString = "Utilities | updateTransaction() | ";

        boolean updated = false;
        Object response = null;

        try {
            response = Utilities.callBeepAPI(logging, props, obj);

            logging.info(logPreString + " Beep Core response "
                    + response);

            if (response != null && !response.toString().isEmpty()) {
                JSONObject apiRespObj = new JSONObject((String) response);

                JSONObject authStatusObj
                        = apiRespObj.getJSONObject("authStatus");

                // Check it was authenticated successfully
                int authStatusCode = authStatusObj.getInt("authStatusCode");
                String authStatusDescription
                        = authStatusObj.getString("authStatusDescription");

                if (authStatusCode != props.getAuthSuccessCode()) {
                    // Failed authentication
                    logging.error(logPreString
                            + "Authentication failed, Status code returned: "
                            + authStatusCode + ", Status Description: "
                            + authStatusDescription);
                    if (!isRetry) {
                        boolean written = Utilities.logFailedAcknowledgment(
                                logging, obj.toString(), beepTransactionID,
                                statusCode, serviceID, serviceCode);

                        if (!written) {
                            logging.error(logPreString + "The transaction "
                                    + "failed and was not written to file. "
                                    + "Details: " + obj.toString());
                        }
                    }

                    return false;
                }

                logging.info(logPreString
                        + "Authentication passed, Status code returned: "
                        + authStatusCode + ", Status Description: "
                        + authStatusDescription);

                JSONArray resultarray
                        = apiRespObj.getJSONArray("results");

                /*
                 * We first check the size of the result if it is one and
                 * confirm with the statusCode then we have no payments
                 */
                int pSize = resultarray.length();

                if (pSize > 0) {
                    for (int count = 0; count < pSize; count++) {
                        if (resultarray.get(count) != null) {
                            JSONObject resp
                                    = (JSONObject) resultarray.get(count);

                            int returnedStatusCode = resp.getInt("statusCode");
                            int returnedBeepTransactionID
                                    = resp.getInt("beepTransactionID");
                            String returnedStatusDescription
                                    = resp.getString("statusDescription");
                            if (returnedBeepTransactionID
                                    == beepTransactionID) {
                                if (returnedStatusCode
                                        == props.getAcknowledgmentOkStatus()) {
                                    logging.info(logPreString
                                            + "The transaction has "
                                            + "been successfully acknowledged "
                                            + "and updated to status: "
                                            + statusCode
                                            + ". The status description is: "
                                            + returnedStatusDescription);
                                    updated = true;
                                } else if (returnedStatusCode
                                        == props.getTransactionAlreadyAcknowledgedAccepted()
                                        || returnedStatusCode
                                        == props.getTransactionAlreadyAcknowledgedRejected()
                                        || returnedStatusCode
                                        == props.getTransactionAlreadyEscalated()) {
                                    logging.info(logPreString
                                            + "The transaction had "
                                            + "already been acknowledged and "
                                            + "updated to status: " + statusCode
                                            + ". The status description is: "
                                            + returnedStatusDescription);
                                    updated = true;
                                } else if (returnedStatusCode
                                        == props.getFinalEscalatedCode()) {
                                    logging.info(logPreString
                                            + "The transaction has been "
                                            + "escalated and returned status "
                                            + "is: " + statusCode
                                            + ". The status description is"
                                            + returnedStatusDescription);
                                    updated = true;
                                } else {
                                    logging.info(logPreString
                                            + "The transaction has not been "
                                            + "acknowledged and returned "
                                            + "status is:"
                                            + returnedStatusCode
                                            + ". The status description is"
                                            + returnedStatusDescription);
                                    updated = false;
                                }
                            } else {
                                logging.error(logPreString
                                        + "The beepTransactionID sent to API "
                                        + "differs from the one returned");
                                updated = false;
                            }
                        } else {
                            logging.info(logPreString
                                    + "The response returned was empty. "
                                    + "Cannot action on the record...");
                            updated = false;
                        }
                    }
                } else {
                    logging.error(logPreString + "Was expecting a record from "
                            + "the decoded Json response from Post Payment ACK "
                            + "but found none... ");
                    updated = false;
                }
            } else {
                logging.info(logPreString + "No response from API after calling"
                        + " post payment ACK... ");
                updated = false;
            }
        } catch (JSONException ex) {
            if (response != null) {
                logging.error(logPreString + "There was an error decoding the "
                        + "JSON string from postPaymentACK. String: "
                        + response.toString() + ". Error message: "
                        + ex.getMessage());
            }
            updated = false;
        }

        if (!updated) {
            if (!isRetry) {
                boolean written = Utilities.logFailedAcknowledgment(logging,
                        obj.toString(), beepTransactionID, statusCode,
                        serviceID, serviceCode);

                if (!written) {
                    logging.error(logPreString + "The transaction failed and "
                            + "was not written to file. Details: "
                            + obj.toString());
                }
            }
        }

        return updated;
    }

    /**
     * Logs the transaction as failed.
     *
     * @param logging the log class instance
     * @param jsonString the data
     * @param beepTransactionID the beep transaction ID
     * @param statusCode the status code
     * @param serviceID the service ID
     * @param name the name
     *
     * @return true if logged successfully, false otherwise
     */
    public static boolean logFailedAcknowledgment(final Logging logging,
            final String jsonString, final int beepTransactionID,
            final int statusCode, final int serviceID, final String name) {

        return updateFile(logging, name + "_" + serviceID + "_"
                + PaymentPusherConstants.FAILED_ACK_FILE,
                jsonString);
    }

    /**
     * Append data to a file.
     *
     * @param logging the logger
     * @param filepath the file
     * @param data the string to write
     *
     * @return true if written successfully, false otherwise
     */
    @SuppressWarnings({"null", "ConstantConditions"})
    private static Boolean writeToFile(final Logging logging,
            final String filepath, final String data) {
        String logPreString = "Utilities | writeToFile() | ";

        try (PrintWriter pout = new PrintWriter(
                new FileOutputStream(filepath, true))) {
            pout.println(data);
            pout.flush();

            logging.info(logPreString
                    + "Appended query: " + data + " to file: " + filepath);
            return true;
        } catch (IOException ex) {
            logging.fatal(logPreString + "Failed to append query: "
                    + data + " to file: "
                    + filepath + ". Error: " + ex.getMessage());

            return false;
        }
    }

    /**
     * <p>
     * Store failed post payment ACKs in a file. NOTE: Checks whether the file
     * exists and writes to it the ACKs that need to be performed.</p>
     *
     * <p>
     * Performance Issues: This has an effect on the speed of execution. Speed
     * reduces because of waiting for a lock to be unlocked. The wait time is
     * unlimited.</p>
     *
     * @param logging the logger
     * @param file the file to write to
     * @param data the string to write
     *
     * @return true if updated, false otherwise
     */
    @SuppressWarnings({"null", "ConstantConditions"})
    public static boolean updateFile(final Logging logging, final String file,
            final String data) {
        String logPreString = "Utilities | updateFile() | ";

        boolean status = false;
        File myfile = new File(file);
        if (!myfile.exists()) {
            logging.info(logPreString
                    + " Post payment ACKs or debits file was not found, "
                    + "creating and appending file");

            try {
                if (myfile.createNewFile()) {
                    logging.info(logPreString + " Failed post payment ACK or "
                            + "debit: " + data + ", appending to file");
                    status = writeToFile(logging, file, data);
                }
            } catch (IOException ex) {
                logging.fatal(logPreString + "Unable to create file. Error: "
                        + ex.getMessage());

                status = false;
            }
        } else {
            logging.info(logPreString
                    + " Failed post payment ACK or debit: " + data
                    + ", appending to file");
            status = writeToFile(logging, file, data);
        }

        return status;
    }

    /**
     * Private constructor.
     */
    private Utilities() {
        super();
    }
}

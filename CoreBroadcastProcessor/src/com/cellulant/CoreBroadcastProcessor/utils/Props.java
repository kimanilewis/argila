package com.cellulant.CoreBroadcastProcessor.utils;

import com.cellulant.encryption.Encryption;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Loads system properties from a file.
 *
 * @author <a href="brian.ngure@cellulant.com">Brian Ngure</a>
 */
@SuppressWarnings({"FinalClass", "ClassWithoutLogger"})
public final class Props {

    /**
     * A list of any errors that occurred while loading the properties.
     */
    private final List<String> loadErrors;
    /**
     * Info log level. Default = INFO.
     */
    private String infoLogLevel = "INFO";
    /**
     * Error log level. Default = FATAL.
     */
    private String errorLogLevel = "ERROR";
    /**
     * Database connection pool name.
     */
    private String dbPoolName;
    /**
     * Database user name.
     */
    private String dbUserName;
    /**
     * Database password.
     */
    private String dbPassword;
    /**
     * Database host.
     */
    private String dbHost;
    /**
     * Database port.
     */
    private String dbPort;
    /**
     * Database name.
     */
    private String dbName;
    /**
     * Database max connections.
     */
    private int dbMaxConnections;
    /**
     * Info log file name.
     */
    private String infoLogFile;
    /**
     * Error log file name.
     */
    private String errorLogFile;
    /**
     * No of threads that will be created in the thread pool to process
     * payments.
     */
    private int numOfChildren;
    /**
     * Beep API URL.
     */
    private String beepAPIUrl;
    /**
     * API connection timeout.
     */
    private int connectionTimeout;
    /**
     * API reply timeout.
     */
    private int replyTimeout;
    /**
     * Sleep time.
     */
    private int sleepTime;
    /**
     * The properties file.
     */
    private static final String PROPS_FILE = "conf/CheckoutResponseProcessorConfigs.xml";
    /**
     * The Application name of the Push Payment Status Daemon.
     */
    private String appName;

    /**
     * Maximum number of times to retry executing the failed text Query.
     */
    private int maxFailedQueryRetries;
    /**
     * Number of seconds before a next send.
     */
    private int nextSendInterval;
    /**
     * Maximum possible value of the run id.
     */
    private int maxRunID;
    /**
     * Size of the messages to be fetched at one go.
     */
    private int bucketSize;
    /**
     * Default time for expiry of payment to be pushed.
     */
    private int postPaymentPeriod;

    /**
     * Default time to fail transaction with a retry status.
     */
    private int failPendingTrxTimeout;
    /**
     * Authentication success status code.
     */
    private int authSuccessCode;
    /**
     * The string of statuses to push.
     */
    private int newPaymentStatus;
    /**
     * Status indicating the payment should be reprocessed.
     */
    private int reprocessPaymentStatus;
    /**
     * Statues code indicating transaction has been processed and is accepted.
     */
    private int finalAcceptedCode;
    /**
     * Statues code indicating transaction has been processed and is rejected.
     */
    private int finalRejectedCode;
    /**
     * Statues code indicating transaction has been processed and is escalated.
     */
    private int finalEscalatedCode;
    /**
     * Status code indicating the transaction was successfully acknowledged.
     */
    private int acknowledgmentOkStatus;
    /**
     * Status code indicating the transaction was already acknowledged as
     * accepted.
     */
    private int transactionAlreadyAcknowledgedAccepted;
    /**
     * Status code indicating the transaction was already acknowledged as
     * rejected.
     */
    private int transactionAlreadyAcknowledgedRejected;
    /**
     * Status code indicating the transaction was already acknowledged as
     * escalated.
     */
    private int transactionAlreadyEscalated;
    /**
     * The Hub JSON API URL.
     */
    private String hubJsonAPIUrl;
    /**
     * The Hub payment gateway username.
     */
    private String hubUsername;
    /**
     * The Hub payment gateway password.
     */
    private String hubPassword;
    /**
     * The hub payment gateway acknowledge function.
     */
    private String hubPostPaymentFunction;
    /**
     * Unprocessed Status.
     */
    private int unprocessedStatus;
    /**
     * Escalated Status.
     */
    private int processingEscalatedStatus;
    /**
     * Success Status.
     */
    private int processedStatus;

    /**
     * Status for a transaction that has failed, Payment pusher will not pick
     * the transaction again.
     */
    private int failedStatus;
    /**
     * String of service IDs for the services the payment pusher will process
     * payments for.
     */
    private String paymentServiceIDs;

    /**
     * The URL to the payment pusher wrapper script.
     */
    private String postPaymentAPIUrl;
    /**
     * Status code from external client indicating the transaction has been
     * successfully delivered.
     */
    private int successfullyDeliveredCode;
    /**
     * Status code from external client indicating the transaction has failed
     * delivery.
     */
    private int retryFailureCode;
    /**
     * Sleep time for retry of connection.
     */
    private int connectionRetrySleep;

    /**
     * The initialization vector used in SMS Password encryption.
     */
    private String initialisationVector;
    /**
     * The key used in SMS Password encryption.
     */
    private String encryptionKey;

    /**
     * Maximum number of sends per transaction.
     */
    private int maximumNumberOfSends;
    /**
     * defaultCurrencyCode.
     */
    private String defaultCurrencyCode;

    /**
     * Constructor.
     */
    public Props() {
        loadErrors = new ArrayList<>(0);
        loadProperties(PROPS_FILE);
    }

    /**
     * Load system properties.
     *
     * @param propsFile the system properties XML file
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private void loadProperties(final String propsFile) {
        Properties props = new Properties();

        try (FileInputStream propsStream = new FileInputStream(propsFile)) {
            props.loadFromXML(propsStream);

            String error1 = "ERROR: %s is <= 0 or may not have been set";
            String error2 = "ERROR: %s may not have been set";

            infoLogLevel = props.getProperty("InfoLogLevel", "");
            if (getInfoLogLevel().isEmpty()) {
                loadErrors.add(String.format(error2, "InfoLogLevel"));
            }

            errorLogLevel = props.getProperty("ErrorLogLevel", "");
            if (getErrorLogLevel().isEmpty()) {
                loadErrors.add(String.format(error2, "ErrorLogLevel"));
            }

            infoLogFile = props.getProperty("InfoLogFile", "");
            if (getInfoLogFile().isEmpty()) {
                loadErrors.add(String.format(error2, "InfoLogFile"));
            }

            errorLogFile = props.getProperty("ErrorLogFile", "");
            if (getErrorLogFile().isEmpty()) {
                loadErrors.add(String.format(error2, "ErrorLogFile"));
            }
            defaultCurrencyCode = props.getProperty("DefaultCurrencyCode", "");
            if (getDefaultCurrencyCode().isEmpty()) {
                loadErrors.add(String.format(error2, "DefaultCurrencyCode"));
            }

            String noc = props.getProperty("NumberOfThreads", "");
            if (noc.isEmpty()) {
                loadErrors.add(String.format(error1, "NumberOfThreads"));
            } else {
                numOfChildren = Integer.parseInt(noc);
                if (numOfChildren <= 0) {
                    loadErrors.add(String.format(error1,
                            "NumberOfThreads"));
                }
            }

            String connTimeout = props.getProperty("ConnectionTimeout", "");
            if (connTimeout.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "ConnectionTimeout"));
            } else {
                connectionTimeout = Integer.parseInt(connTimeout);
                if (connectionTimeout < 0) {
                    loadErrors.add(String.format(error1,
                            "ConnectionTimeout"));
                }
            }

            String replyTO = props.getProperty("ReplyTimeout", "");
            if (replyTO.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "ReplyTimeout"));
            } else {
                replyTimeout = Integer.parseInt(replyTO);
                if (replyTimeout < 0) {
                    loadErrors.add(String.format(error1,
                            "ReplyTimeout"));
                }
            }

            String sleep = props.getProperty("SleepTime", "");
            if (sleep.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "SleepTime"));
            } else {
                sleepTime = Integer.parseInt(sleep);
                if (sleepTime < 0) {
                    loadErrors.add(String.format(error1,
                            "SleepTime"));
                }
            }

            dbPoolName = props.getProperty("DbPoolName", "");
            if (getDbPoolName().isEmpty()) {
                loadErrors.add(String.format(error2, "DbPoolName"));
            }

            dbUserName = props.getProperty("DbUserName", "");
            if (getDbUserName().isEmpty()) {
                loadErrors.add(String.format(error2, "DbUserName"));
            }

            dbPassword = props.getProperty("DbPassword", "");
            if (getDbPassword().isEmpty()) {
                loadErrors.add(String.format(error2, "DbPassword"));
            }

            dbHost = props.getProperty("DbHost", "");
            if (getDbHost().isEmpty()) {
                loadErrors.add(String.format(error2, "DbHost"));
            }

            dbPort = props.getProperty("DbPort", "");
            if (getDbPort().isEmpty()) {
                loadErrors.add(String.format(error2, "DbPort"));
            }

            dbName = props.getProperty("DbName", "");
            if (getDbName().isEmpty()) {
                loadErrors.add(String.format(error2, "DbName"));
            }

            appName = props.getProperty("ApplicationName", "");
            if (getAppName().isEmpty()) {
                loadErrors.add(String.format(error2, "ApplicationName"));
            }

            String newPayment = props.getProperty("NewPaymentStatus", "");
            if (newPayment.isEmpty()) {
                loadErrors.add(String.format(error2, "NewPaymentStatus"));
            } else {
                newPaymentStatus = Integer.parseInt(newPayment);
                if (newPaymentStatus < 0) {
                    loadErrors.add(String.format(error1,
                            "NewPaymentStatus"));
                }
            }

            String reprocessPayment = props
                    .getProperty("ReprocessPaymentStatus");
            if (reprocessPayment.isEmpty()) {
                loadErrors.add(String.format(error2,
                        "ReprocessPaymentStatus"));
            } else {
                reprocessPaymentStatus = Integer.parseInt(reprocessPayment);
                if (reprocessPaymentStatus < 0) {
                    loadErrors.add(String.format(error1,
                            "ReprocessPaymentStatus"));
                }
            }

            paymentServiceIDs = props.getProperty("PaymentServiceIDs", "");
            if (paymentServiceIDs.isEmpty()) {

                loadErrors.add(String.format(error1,
                        "PaymentServiceIDs"));
            }

            postPaymentAPIUrl = props.getProperty("PostPaymentAPIUrl", "");
            if (postPaymentAPIUrl.isEmpty()) {
                loadErrors.add(String.format(error2, "PostPaymentAPIUrl"));
            }

            initialisationVector = props.getProperty("IntializationVector", "");
            if (initialisationVector.isEmpty()) {
                loadErrors.add(String.format(error2,
                        "IntializationVector"));
            }

            encryptionKey = props.getProperty("EncryptionKey", "");
            if (encryptionKey.isEmpty()) {
                loadErrors.add(String.format(error2, "EncryptionKey"));
            }

            String sdc = props.getProperty("SuccessfullyDeliveredCode", "");
            if (sdc.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "SuccessfullyDeliveredCode"));
            } else {
                successfullyDeliveredCode = Integer.parseInt(sdc);
                if (successfullyDeliveredCode < 0) {
                    loadErrors.add(String.format(error1,
                            "SuccessfullyDeliveredCode"));
                }
            }

            String rtfc = props.getProperty("RetryFailureCode", "");
            if (rtfc.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "RetryFailureCode"));
            } else {
                retryFailureCode = Integer.parseInt(rtfc);
                if (retryFailureCode < 0) {
                    loadErrors.add(String.format(error1,
                            "RetryFailureCode"));
                }
            }

            String nsi = props.getProperty("NextSendInterval", "");
            if (!nsi.isEmpty()) {
                nextSendInterval = Integer.parseInt(nsi);
                if (nextSendInterval <= 0) {
                    loadErrors.add(String.format(error1,
                            "NextSendInterval"));
                }
            } else {
                loadErrors.add(String.format(error1,
                        "NextSendInterval"));
            }

            String failPendingTimeOut = props.getProperty("FailPendingTrxTimeout", "");
            if (!failPendingTimeOut.isEmpty()) {
                failPendingTrxTimeout = Integer.parseInt(failPendingTimeOut);
                if (failPendingTrxTimeout <= 0) {
                    loadErrors.add(String.format(error1, "FailPendingTrxTimeout"));
                }
            } else {
                loadErrors.add(String.format(error1, "FailPendingTrxTimeout"));
            }

            String pat = props.getProperty("PostPaymentPeriod", "");
            if (!pat.isEmpty()) {
                postPaymentPeriod = Integer.parseInt(pat);
                if (postPaymentPeriod <= 0) {
                    loadErrors.add(String.format(error1,
                            "PostPaymentPeriod"));
                }
            } else {
                loadErrors.add(String.format(error1,
                        "PostPaymentPeriod"));
            }

            String maxFQretiries = props
                    .getProperty("MaximumFailedQueryRetries");
            if (!maxFQretiries.isEmpty()) {
                maxFailedQueryRetries = Integer.parseInt(maxFQretiries);
                if (maxFailedQueryRetries <= 0) {
                    loadErrors.add(String.format(error1,
                            "MaximumFailedQueryRetries"));
                }
            } else {
                loadErrors.add(String.format(error1,
                        "MaximumFailedQueryRetries"));
            }

            String bucket = props.getProperty("BucketSize", "");
            if (!bucket.isEmpty()) {
                bucketSize = Integer.parseInt(bucket);
                if (bucketSize <= 0) {
                    loadErrors.add(String.format(error1,
                            "BucketSize"));
                }
            } else {
                loadErrors.add(String.format(error1,
                        "BucketSize"));
            }

            String unprocessed = props.getProperty("UnprocessedStatus", "");
            if (unprocessed.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "UnprocessedStatus"));
            } else {
                unprocessedStatus = Integer.parseInt(unprocessed);
                if (unprocessedStatus < 0) {
                    loadErrors.add(String.format(error1,
                            "UnprocessedStatus"));
                }
            }

            String failed = props.getProperty("FailedStatus", "");
            if (failed.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "FailedStatus"));
            } else {
                failedStatus = Integer.parseInt(failed);
                if (failedStatus < 0) {
                    loadErrors.add(String.format(error1,
                            "FailedStatus"));
                }
            }

            String processed = props.getProperty("ProcessedStatus", "");
            if (processed.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "ProcessedStatus"));
            } else {
                processedStatus = Integer.parseInt(processed);
                if (processedStatus < 0) {
                    loadErrors.add(String.format(error1,
                            "ProcessedStatus"));
                }
            }
            String escalated = props.getProperty("EscalatedStatus", "");
            if (escalated.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "EscalatedStatus"));
            } else {
                processingEscalatedStatus = Integer.parseInt(escalated);
                if (processingEscalatedStatus < 0) {
                    loadErrors.add(String.format(error1,
                            "EscalatedStatus"));
                }
            }

            String fa = props.getProperty("FinalAccepted", "");
            if (fa.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "FinalAccepted"));
            } else {
                finalAcceptedCode = Integer.parseInt(fa);
                if (finalAcceptedCode < 0) {
                    loadErrors.add(String.format(error1,
                            "FinalAccepted"));
                }
            }
            String fr = props.getProperty("FinalRejected", "");
            if (fr.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "FinalRejected"));
            } else {
                finalRejectedCode = Integer.parseInt(fr);
                if (finalRejectedCode < 0) {
                    loadErrors.add(String.format(error1,
                            "FinalRejected"));
                }
            }
            String fe = props.getProperty("FinalEscalated", "");
            if (fe.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "FinalEscalated"));
            } else {
                finalEscalatedCode = Integer.parseInt(fe);
                if (finalEscalatedCode < 0) {
                    loadErrors.add(String.format(error1,
                            "FinalEscalated"));
                }
            }

            String ackOK = props.getProperty("AcknowledgmentOk", "");
            if (ackOK.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "AcknowledgmentOk"));
            } else {
                acknowledgmentOkStatus = Integer.parseInt(ackOK);
                if (acknowledgmentOkStatus < 0) {
                    loadErrors.add(String.format(error1,
                            "AcknowledgmentOk"));
                }
            }

            String alreadyAckAcc = props
                    .getProperty("TransactionAlreadyAcknowledgedAccepted");
            if (alreadyAckAcc.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "TransactionAlreadyAcknowledgedAccepted"));
            } else {
                transactionAlreadyAcknowledgedAccepted
                        = Integer.parseInt(alreadyAckAcc);
                if (transactionAlreadyAcknowledgedAccepted < 0) {
                    loadErrors.add(String.format(error1,
                            "TransactionAlreadyAcknowledgedAccepted"));
                }
            }

            String alreadyAckRej = props
                    .getProperty("TransactionAlreadyAcknowledgedRejected");
            if (alreadyAckRej.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "TransactionAlreadyAcknowledgedRejected"));
            } else {
                transactionAlreadyAcknowledgedRejected
                        = Integer.parseInt(alreadyAckRej);
                if (transactionAlreadyAcknowledgedRejected < 0) {
                    loadErrors.add(String.format(error1,
                            "TransactionAlreadyAcknowledgedRejected"));
                }
            }

            String alreadyEsc
                    = props.getProperty("TransactionAlreadyEscalated", "");
            if (alreadyEsc.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "TransactionAlreadyEscalated"));
            } else {
                transactionAlreadyEscalated = Integer.parseInt(alreadyEsc);
                if (transactionAlreadyEscalated < 0) {
                    loadErrors.add(String.format(error1,
                            "TransactionAlreadyEscalated"));
                }
            }

            String as = props.getProperty("AuthenticationSuccess", "");
            if (as.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "AuthenticationSuccess"));
            } else {
                authSuccessCode = Integer.parseInt(as);
                if (authSuccessCode < 0) {
                    loadErrors.add(String.format(error1,
                            "AuthenticationSuccess"));
                }
            }
            String mnsends = props.getProperty("MaximumNumberOfSends", "");
            if (mnsends.isEmpty()) {
                loadErrors.add(String.format(error1,
                        "MaximumNumberOfSends"));
            } else {
                maximumNumberOfSends = Integer.parseInt(mnsends);
                if (maximumNumberOfSends < 0) {
                    loadErrors.add(String.format(error1,
                            "MaximumNumberOfSends"));
                }
            }

            hubUsername = props.getProperty("BeepUsername", "");
            if (hubUsername.isEmpty()) {
                loadErrors.add(String.format(error2, "BeepUsername"));
            }

            hubJsonAPIUrl = props.getProperty("BeepJsonAPIUrl", "");
            if (hubJsonAPIUrl.isEmpty()) {
                loadErrors.add(String.format(error2, "BeepJsonAPIUrl"));
            }

            hubPostPaymentFunction = props.getProperty("BeepPostPaymentFunction", "");
            if (hubPostPaymentFunction.isEmpty()) {
                loadErrors.add(String.format(error2, "BeepPostPaymentFunction"));
            }

            String maxConns = props.getProperty("DbMaxConnections", "");
            if (!maxConns.isEmpty()) {
                dbMaxConnections = Integer.parseInt(maxConns);
                if (dbMaxConnections <= 0) {
                    loadErrors.add(String.format(error1,
                            "DbMaxConnections"));
                }
            } else {
                loadErrors.add(String.format(error1, "DbMaxConnections"));
            }

            String hubPasswordEncrypt = props.getProperty("BeepPassword", "");
            if (hubPasswordEncrypt.isEmpty()) {
                loadErrors.add(String.format(error2, "BeepPassword"));
            } else {

                Encryption mcrypt = new Encryption(
                        getInitialisationVector(),
                        getEncryptionKey());

                hubPassword = new String(
                        mcrypt.decrypt(hubPasswordEncrypt));
            }

        } catch (NumberFormatException | IOException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
            System.err.println("Exiting. Failed to load system properties: "
                    + ex.getMessage());

            System.exit(1);
        }
    }

    /**
     * Info log level. Default = INFO.
     *
     * @return the infoLogLevel
     */
    public String getInfoLogLevel() {
        return infoLogLevel;
    }

    /**
     * Error log level. Default = FATAL.
     *
     * @return the errorLogLevel
     */
    public String getErrorLogLevel() {
        return errorLogLevel;
    }

    /**
     * Info log file name.
     *
     * @return the infoLogFile
     */
    public String getInfoLogFile() {
        return infoLogFile;
    }

    /**
     * Error log file name.
     *
     * @return the errorLogFile
     */
    public String getErrorLogFile() {
        return errorLogFile;
    }

    /**
     * Gets the Beep API URL.
     *
     * @return the Beep API URL
     */
    public String getBeepAPIUrl() {
        return beepAPIUrl;
    }

    /**
     * No of threads that will be created in the thread pool to process
     * payments.
     *
     * @return the numOfChildren
     */
    public int getNumOfChildren() {
        return numOfChildren;
    }

    /**
     * Gets the connection timeout.
     *
     * @return the connection timeout
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Gets the reply timeout.
     *
     * @return the reply timeout
     */
    public int getReplyTimeout() {
        return replyTimeout;
    }

    /**
     * Gets the sleep time.
     *
     * @return the sleep time
     */
    public int getSleepTime() {
        return sleepTime;
    }

    public String getDbPoolName() {
        return dbPoolName;
    }

    public String getDbUserName() {
        return dbUserName;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbHost() {
        return dbHost;
    }

    public String getDbPort() {
        return dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public String getPaymentServiceIDs() {
        return paymentServiceIDs;
    }

    public int getSuccessfullyDeliveredCode() {
        return successfullyDeliveredCode;
    }

    public int getRetryFailureCode() {
        return retryFailureCode;
    }

    public int getFinalAcceptedCode() {
        return finalAcceptedCode;
    }

    public int getFinalRejectedCode() {
        return finalRejectedCode;
    }

    public int getFinalEscalatedCode() {
        return finalEscalatedCode;
    }

    public int getConnectionRetrySleep() {
        return connectionRetrySleep;
    }

    public int getNewPaymentStatus() {
        return newPaymentStatus;
    }

    public String getInitialisationVector() {
        return initialisationVector;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public String getAppName() {
        return appName;
    }

    public static String getPROPS_FILE() {
        return PROPS_FILE;
    }

    public int getMaxFailedQueryRetries() {
        return maxFailedQueryRetries;
    }

    public int getNextSendInterval() {
        return nextSendInterval;
    }

    public int getMaxRunID() {
        return maxRunID;
    }

    public int getFailPendingTrxTimeout() {
        return failPendingTrxTimeout;
    }

    public int getPostPaymentPeriod() {
        return postPaymentPeriod;
    }

    public String getPostPaymentAPIUrl() {
        return postPaymentAPIUrl;
    }

    public int getBucketSize() {
        return bucketSize;
    }

    public int getUnprocessedStatus() {
        return unprocessedStatus;
    }

    public int getProcessingEscalatedStatus() {
        return processingEscalatedStatus;
    }

    public int getProcessedStatus() {
        return processedStatus;
    }

    public int getFailedStatus() {
        return failedStatus;
    }

    public String getHubUsername() {
        return hubUsername;
    }

    public String getHubPassword() {
        return hubPassword;
    }

    public int getAcknowledgmentOkStatus() {
        return acknowledgmentOkStatus;
    }

    public int getTransactionAlreadyAcknowledgedAccepted() {
        return transactionAlreadyAcknowledgedAccepted;
    }

    public int getTransactionAlreadyEscalated() {
        return transactionAlreadyEscalated;
    }

    public int getTransactionAlreadyAcknowledgedRejected() {
        return transactionAlreadyAcknowledgedRejected;
    }

    public String getHubJsonAPIUrl() {
        return hubJsonAPIUrl;
    }

    public int getAuthSuccessCode() {
        return authSuccessCode;
    }

    public String getHubPostPaymentFunction() {
        return hubPostPaymentFunction;
    }

    public int getReprocessPaymentStatus() {
        return reprocessPaymentStatus;
    }

    public int getMaximumNumberOfSends() {
        return maximumNumberOfSends;
    }

    /**
     * Get the max DB connections.
     *
     * @return the max DB connections
     */
    public int getDbMaxConnections() {
        return dbMaxConnections;
    }

    public String getDefaultCurrencyCode() {
        return defaultCurrencyCode;
    }

    /**
     * A list of any errors that occurred while loading the properties.
     *
     * @return the loadErrors
     */
    public List<String> getLoadErrors() {
        return Collections.unmodifiableList(loadErrors);
    }

}

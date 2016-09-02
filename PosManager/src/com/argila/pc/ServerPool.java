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
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author lewie
 */
@SuppressWarnings({"ClassWithoutLogger", "FinalClass"})
public final class ServerPool implements Runnable {

    /**
     * Log class instance.
     */
    private final Logging logging;
    /**
     * Properties instance.
     */
    /**
     * Initializes the MySQL connection pool.
     */
    private MySQL mysql;
    /**
     * System properties class instance.
     */
    private final Props props;

    protected int serverPort = 9000;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    protected ExecutorService threadPool
            = Executors.newFixedThreadPool(2);

    public ServerPool(int port, final Props properties, final Logging log) {
        serverPort = port;
        props = properties;
        logging = log;

    }

    /**
     *
     */
    @Override
    public void run() {
        synchronized (this) {
            runningThread = Thread.currentThread();
        }
        logging.info(CoreUtils.getLogPreString()
                + "About to open socket ..."
                + "\n ");
        openServerSocket();

        while (!isStopped()) {
            logging.info(CoreUtils.getLogPreString()
                    + "Socket opened .. opened connection ..."
                    + "\n ");
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException | RuntimeException e) {
                if (isStopped()) {
                    logging.info(CoreUtils.getLogPreString()
                            + "No session accounts records were fetched "
                            + "\n Server Stopped...");
                    System.out.println("Server Stopped.");
                    break;
                }
                logging.info(CoreUtils.getLogPreString()
                        + "Error accepting client connection.. closing connection ..."
                        + "\n ");
            }
            openDatabaseConnection(clientSocket);

        }
        threadPool.shutdown();
        System.out.println("Server Stopped.");
    }

    private synchronized boolean isStopped() {
        return isStopped;
    }

    public synchronized void stop() {
        isStopped = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            logging.info(CoreUtils.getLogPreString()
                    + "Opening socket ..."
                    + "\n ");
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException | RuntimeException e) {
            logging.info(CoreUtils.getLogPreString()
                    + "Unable to open socket ..."
                    + "\n ");
        }
    }

    /**
     * Starts the daemon.
     */
    public synchronized void start() {
        isStopped = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    /**
     * Destroys the daemon. Destroys any object created in init().
     */
    public void destroy() {
        logging.info("Destroying POS server daemon...");
        logging.info("Exiting...");
    }

    private void openDatabaseConnection(Socket ClientSocket) {
        try {
            logging.info(CoreUtils.getLogPreString() + "init() |"
                    + " Initializing Pos Daemon daemon...");

            mysql = new MySQL(props.getDbHost(), props.getDbPort(),
                    props.getDbName(), props.getDbUserName(),
                    props.getDbPassword(), props.getDbPoolName(),
                    props.getDbMaxConnections());
            threadPool.execute(
                    new ServerJob(ClientSocket, mysql, logging, props));
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            logging.fatal(CoreUtils.getLogPreString() + "init() | "
                    + "Exception caught during initialization: "
                    + ex.getMessage());
            System.exit(1);
        } catch (SQLException ex) {
            logging.fatal(CoreUtils.getLogPreString() + "init() | SQL Exception "
                    + "Thrown = " + ex.getMessage());
            System.exit(1);
        }

    }
}

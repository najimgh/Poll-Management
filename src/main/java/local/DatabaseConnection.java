package local;

import app.ConfigProperties;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final ConfigProperties properties = new ConfigProperties();
    private static DatabaseConnection openConnection;
    private Connection conn;

    private static String URL = null;
    //Make sure to set the user and password to the proper values in the config.properties file.
    private static String USER = null;
    private static String PASSWORD = null;


    static {
        try {
            URL = properties.getConfigValues("database_url");
            USER = properties.getConfigValues("database_user");
            PASSWORD = properties.getConfigValues("database_password");
        } catch (IOException e) {
            System.out.println("Exception: " + e);
        }
    }

    /**
     * Constructor
     * When the constructor is called, a connection to the database is made.
     */
    private DatabaseConnection() {
        init();
    }

    /**
     * Creates an instance of the class. Does not allow for more than one instance of the class.
     *
     * @return An instance of the class.
     */
    public static DatabaseConnection start() {
        if (openConnection == null) {
            openConnection = new DatabaseConnection();
        }
        return openConnection;
    }

    /**
     * Creates a connection to a specified database (with the URL) using the JDBC API
     */
    private void init() {
        try {
            Class.forName("org.h2.Driver");
            this.conn = DriverManager.getConnection(URL, USER, PASSWORD); //Using JDBC's API we connect to the embedded memory database
            System.out.println("Connection Created");
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Getter
     *
     * @return A Connection object
     */
    public Connection getConnection() {
        return this.conn;
    }

    /**
     * Stops the connection to the database.
     */
    public void stop() {
        try {
            this.conn.close();
            System.out.println("Connection stopped.");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}

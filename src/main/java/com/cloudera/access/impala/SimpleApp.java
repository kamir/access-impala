/**
 * This example illustrates how to use the Cloudera-Impala-Connector 
 * to connect to a secure Impala cluster using SSL encryption and 
 * Kerberos authentication using keytab files.
 * 
 * We have to prepare 3 configuration files:
 * 
 * (1) config.props
 * 
 * connection.url = jdbc:impala://HOST.DOMAIN:PORT
 * dbc.driver.class.name = com.cloudera.impala.jdbc41.Driver
 * jdbc.query = Select count(*) from sample_07
 * keytab.file = ./sec/impala.keytab
 * 
 * (2) jaas-impala.conf 
 * 
 * This file has to provide a login context descriptor with name "Client" to be
 * used by the SIMBA driver.
   
   Client {
    com.sun.security.auth.module.Krb5LoginModule required
    useKeyTab=true
    keyTab="PATH TO PROJECT/access-impala/sec/impala.keytab"
    storeKey=true
    useTicketCache=true
    principal="impala/HOST.DOMAIN@REALM"
    doNotPrompt=true
    debug=true;
   };
   
 * Optionally more login context descriptors can be provided. In our example we
 * also login via Kerberos using the "Impala-client" context.
 * 
 * (3) krb5.conf
 * 
 * A kerberos client configuration is need as well. Simply copy the details
 * provided by the cluster administrator into this file. This allows you to use
 * a different Kereberos server than the one which is currently configured on
 * your workstation or development machine.
 * 
 * Furthermore we need the signed (self-signed) server-certificate in 
 * Java-KeyStore format.
 * 
 */
package com.cloudera.access.impala;

import com.sun.security.auth.callback.TextCallbackHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * SimpleApp : Impala access client
 * 
 * This example is used to show how the different authentication methods for
 * Impala are used in a secure environment.
 * 
 * We assume, that a CDH installation is used and that the example data set
 * available in HUE is installed. This means, a table sample_08 would exist
 * and the result of counting all records shoule be: 828.
 *
 * @author kamir
 */
public class SimpleApp {

    private static final String CONNECTION_URL_PROPERTY = "connection.url";
    private static final String JDBC_DRIVER_NAME_PROPERTY = "jdbc.driver.class.name";
    private static final String JDBC_QUERY = "jdbc.query";
    private static final String KEYTAB_FILE = "keytab.file";
    
    private static String connectionUrl;
    private static String jdbcDriverName;
    private static String query;
    private static String keytabFile;

    public static void main(String[] args) throws IOException, LoginException {

        loadConfiguration();

        String jaasFile = "./jaas-impala.conf";
        System.out.println("JAAS-Config: " + fileExists(jaasFile));

        String krb5conf = "./krb5.conf";
        System.out.println("KRB5-Config: " + fileExists(krb5conf));

        File fJaasFile = new File(jaasFile);

        System.setProperty("java.security.auth.login.config", fJaasFile.getAbsolutePath());
        System.setProperty("java.security.krb5.conf", krb5conf);
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

        // Obtain a LoginContext, needed for authentication. Tell 
        // it to use the LoginModule implementation specified by 
        // the entry named "JaasSample" in the JAAS login 
        // configuration file and to also use the specified 
        // CallbackHandler.
        LoginContext lc = null;
        try {
            lc = new LoginContext("Impala-client",
                    new TextCallbackHandler());
        } catch (LoginException le) {
            System.err.println("Cannot create LoginContext. "
                    + le.getMessage());
            System.exit(-1);
        } catch (SecurityException se) {
            System.err.println("Cannot create LoginContext. "
                    + se.getMessage());
            System.exit(-1);
        }

        try {

            // attempt authentication
            lc.login();

        } catch (LoginException le) {

            System.err.println("Authentication failed: ");
            System.err.println("  " + le.getMessage());
            System.exit(-1);

        }

        System.out.println("Authentication succeeded!");

        String sqlStatement = query;

        System.out.println("\n=============================================");
        System.out.println("Cloudera Impala JDBC Example");
        System.out.println("Using Connection URL: " + connectionUrl);
        System.out.println("Running Query: " + sqlStatement);

        Connection con = null;

        try {

            Class.forName(jdbcDriverName);

            connectionUrl = connectionUrl.concat("/default;ssl=1;AuthMech=1;AllowSelfSignedCerts=1;KrbRealm=CLOUDERA.COM;KrbHostFQDN=ag2r-nihed-2.vpc.cloudera.com;KrbServiceName=impala;SSLKeyStore=./sec/ag2r-nihed-1.keystore;SSLKeyStorePwd=cloudera;");

            System.out.println(connectionUrl);

            con = DriverManager.getConnection(connectionUrl);

            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery(sqlStatement);

            //AllowSelfSignedCerts 
            System.out.println("\n== Begin Query Results ======================");

            // print the results to the console
            while (rs.next()) {
                // the example query returns one String column
                System.out.println(rs.getString(1));
            }

            System.out.println("== End Query Results =======================\n\n");

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception e) {
                // swallow
            }
        }
    }
    
    private static void loadConfiguration() throws IOException {
        InputStream input = null;
        try {

            String filename = "config.props";
            input = new FileInputStream( new File( filename ) );

            Properties prop = new Properties();
            prop.load(input);

            connectionUrl = prop.getProperty(CONNECTION_URL_PROPERTY);
            jdbcDriverName = prop.getProperty(JDBC_DRIVER_NAME_PROPERTY);

            query = prop.getProperty(JDBC_QUERY);
            keytabFile = prop.getProperty(KEYTAB_FILE);

        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private static boolean fileExists(String filename) {
        File f = new File(filename);
        return (f.exists() && !f.isDirectory());
    }

}

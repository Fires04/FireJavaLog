/*
 * Copyright 2017 David "Fires" Stein <http://davidstein.cz>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.davidstein.firejavalog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.concurrent.Executors;

/**
 *
 * @author David "Fires" Stein <http://davidstein.cz>
 */
public class FireJavaLog {

    //constants
    public static final int T_LOGTODATABASE = 1;
    public static final int T_LOGTOFILE = 2;
    public static final int T_LOGTOCONSOLE = 3;
    private static final int T_LOGTOTEMPFILE = 4;
    public static final int L_LOG = 1;
    public static final int L_WARN = 2;
    public static final int L_SEVERE = 3;
    public static final int L_CRITICAL = 4;

    //database variables
    private String databaseTable;
    private String databaseDatabase;
    private String databaseHost;
    private String databaseUser;
    private String databasePass;

    //temp log variables
    private final String tempLogFile = "temp_log.log";

    //file log variables
    private String logFile = "log.log";

    //other variables
    private int logType = 0;

    //setup connection to database
    private Connection con = null;

    /**
     * Database constructor
     *
     * @param databaseTable
     * @param databaseDatabase
     * @param databaseHost
     * @param databaseUser
     * @param databasePass
     */
    public FireJavaLog(String databaseTable, String databaseDatabase, String databaseHost, String databaseUser, String databasePass) {
        this.logType = FireJavaLog.T_LOGTODATABASE;
        this.databaseTable = databaseTable;
        this.databaseDatabase = databaseDatabase;
        this.databaseHost = databaseHost;
        this.databaseUser = databaseUser;
        this.databasePass = databasePass;
    }

    /**
     * File log constructor with prepend date selector
     *
     * @param filename
     * @param prependDate
     */
    public FireJavaLog(String filename, Boolean prependDate) {
        if (prependDate) {
            //prepare date format
            SimpleDateFormat formatToLog = new SimpleDateFormat("yyyy-MM-dd");
            GregorianCalendar gcNow = new GregorianCalendar();
            String mysqlDateFormat = formatToLog.format(gcNow.getTime());
            this.logFile = mysqlDateFormat + "-" + filename;
        } else {
            this.logFile = filename;
        }
        this.logType = FireJavaLog.T_LOGTOFILE;
    }

    /**
     * File log constructor with prepend actual date
     *
     * @param filename
     */
    public FireJavaLog(String filename) {
        this(filename, Boolean.TRUE);
    }

    /**
     * Log some data to File/Database/Console with logType selector
     *
     * @param text
     * @param severe
     * @throws SQLException
     */
    //SQLException - remove
    public void log(String text, int severe, int logType, String datetime) {
        switch (logType) {
            case FireJavaLog.T_LOGTODATABASE:
                logToDatabase(datetime, text, severe);
                break;
            case FireJavaLog.T_LOGTOFILE:
                ArrayList<String> logToFile = new ArrayList<>();
                logToFile.add(this.prepareStringToLog(text, severe));
                this.writeToFile(this.logFile, logToFile);
                break;
            case FireJavaLog.T_LOGTOTEMPFILE:
                ArrayList<String> logToTempFile = new ArrayList<>();
                logToTempFile.add(this.prepareStringToLog(text, severe));
                this.writeToFile(this.tempLogFile, logToTempFile);
                break;
            case FireJavaLog.T_LOGTOCONSOLE:
                break;
            default:
                break;
        }
    }

    public void log(String text, int severe, int logType) {
        this.log(text, severe, logType, this.timeInMysqlFormat());
    }

    /**
     * Log some data to File/Database/Console
     *
     * @param text
     * @param severe
     */
    //SQLException - remove
    public void log(String text, int severe) {
        this.log(text, severe, this.logType, this.timeInMysqlFormat());
    }

    private void logToDatabase(String date, String text, int severe) {
        try {
            //connect to database
            this.connectToDatabase();
            //write log to table
            Statement stm = con.createStatement();
            String query = "INSERT INTO " + this.databaseTable + "(severe,date,text) VALUES("
                    + severe + ",\""
                    + date + "\",\""
                    + text.replaceAll("\"", "\\\\\"")
                    + "\");";
            int result = stm.executeUpdate(query);
            //logTempFileToDatabase();
            this.disconnectFromDatabase();
            //disconnect from database
        } catch (SQLException ex) {
            this.log(ex.getMessage(), FireJavaLog.L_SEVERE, FireJavaLog.T_LOGTOTEMPFILE);
        }
    }

    /**
     *
     *
     * Support functions
     *
     *
     */
    private String prepareStringToLog(String text, int severe) {
        return this.timeInMysqlFormat() + ";" + severe + ";" + text;
    }

    private void writeToFile(String file, ArrayList<String> content) {
        try {
            // Assume default encoding.
            FileWriter fileWriter = new FileWriter(file, true);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            //iterate throught content and write it to file
            for (String temp : content) {
                bufferedWriter.write(temp.replace("\n", "").replace("\r", ""));
                bufferedWriter.newLine();
            }
            // Always close files.
            bufferedWriter.close();
        } catch (IOException ex) {
            this.log(ex.getMessage(), FireJavaLog.L_SEVERE, FireJavaLog.T_LOGTOTEMPFILE);
        }

    }

    private ArrayList<String> readFromFile(String file) {
        //file content
        ArrayList<String> content = new ArrayList<>();

        // This will reference one line at a time
        String line = null;

        // FileReader reads text files in the default encoding.
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                content.add(line);
            }
            // Always close files.
            bufferedReader.close();

        } catch (FileNotFoundException ex) {
            this.log(ex.getMessage(), FireJavaLog.L_SEVERE, FireJavaLog.T_LOGTOTEMPFILE);
        } catch (IOException ex) {
            this.log(ex.getMessage(), FireJavaLog.L_SEVERE, FireJavaLog.T_LOGTOTEMPFILE);
        }

        return content;
    }

    private void connectToDatabase() throws SQLException {
        try {
            //connect to database
            this.con = DriverManager.getConnection("jdbc:mysql://" + this.databaseHost + ":3306/" + this.databaseDatabase, databaseUser, databasePass);
           
            
        } catch (SQLException ex) {
            throw ex;    
        }
    }

    private void disconnectFromDatabase() throws SQLException {
        try {
            this.con.close();
        } catch (SQLException ex) {
            throw ex;
        }
    }

    private void logTempFileToDatabase() {
        File f = new File(this.tempLogFile);
        if (f.exists() && !f.isDirectory()) {
            ArrayList<String> readFromFile = this.readFromFile(this.tempLogFile);
            this.emptyFile(this.tempLogFile);
            for (String line : readFromFile) {
                String[] parts = line.split(";");
                String date = parts[0];
                String severe = parts[1];
                String text = parts[2];
                //supply all arguments
                this.log(text, Integer.parseInt(severe), FireJavaLog.T_LOGTODATABASE, date);
            }
        } else {
            emptyFile(this.tempLogFile);
        }
    }

    private void emptyFile(String filename) {
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(filename, true);
            fileWriter.close();
        } catch (IOException ex) {
            this.log(ex.getMessage(), FireJavaLog.L_SEVERE, FireJavaLog.T_LOGTOTEMPFILE);
        }
    }

    private String timeInMysqlFormat() {
        //prepare date format
        SimpleDateFormat formatMysql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GregorianCalendar gcNow = new GregorianCalendar();
        String mysqlDateFormat = formatMysql.format(gcNow.getTime());
        return mysqlDateFormat;
    }

}

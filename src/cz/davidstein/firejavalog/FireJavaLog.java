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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author David "Fires" Stein <http://davidstein.cz>
 */
public class FireJavaLog {

    //constants
    public static final int T_LOGTODATABASE = 1;
    public static final int T_LOGTOFILE = 2;
    public static final int T_LOGTOCONSOLE = 3;
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
    private final String tempLogFile = "temp_log.json";

    //file log variables
    private final String logFile = "log.json";

    //other variables
    private int logType = 0;
    
    //setup connection to database
    private Connection con = null;

    public FireJavaLog(String databaseTable, String databaseDatabase, String databaseHost, String databaseUser, String databasePass) {
        this.logType = FireJavaLog.T_LOGTODATABASE;
        this.databaseTable = databaseTable;
        this.databaseDatabase = databaseDatabase;
        this.databaseHost = databaseHost;
        this.databaseUser = databaseUser;
        this.databasePass = databasePass;
    }
    
    

    //SQLException - remove
    public boolean log(String text, int severe) throws SQLException {
        switch (this.logType) {
            case FireJavaLog.T_LOGTODATABASE:
                logToDatabase(text, severe);
                break;
            case FireJavaLog.T_LOGTOFILE:
                return true;
            case FireJavaLog.T_LOGTOCONSOLE:
                return true;
            default:
                break;
        }
        return false;
    }

    private void logToDatabase(String text, int severe) throws SQLException {
        try {
            //connect to database
            this.connectToDatabase();
            //log proccess
            if(this.con == null){
                logToTempFile(text, severe);
            }else{
                //write log to table
                Statement stm = con.createStatement();
                String query = "INSERT INTO "+this.databaseTable+"(severe,date,text) VALUES("
                        +severe+","
                        +"NOW(),\" "
                        +text+"\");";
                int result = stm.executeUpdate(query);
                this.disconnectFromDatabase();
            }
            //disconnect from database
        } catch (SQLException ex) {
            //TODO: use log from this lib
            throw ex;
        }
    }

    private void connectToDatabase() throws SQLException {
        try {
            //connect to database
            this.con = DriverManager.getConnection("jdbc:mysql://"+this.databaseHost+":3306/"+this.databaseDatabase, databaseUser, databasePass);
        } catch (SQLException ex) {
            //TODO: use log from this lib
            throw ex;
        }
    }

    private void logToTempFile(String text, int severe) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void disconnectFromDatabase() throws SQLException {
        try {
            this.con.close();
        } catch (SQLException ex) {
            //TODO: use log from this lib
            throw ex;
        }
    }

}

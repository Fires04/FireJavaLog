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
package example;

import cz.davidstein.firejavalog.FireJavaLog;
import java.sql.SQLException;

/**
 *
 * @author David "Fires" Stein <http://davidstein.cz>
 */
public class Demo {
    

    public static void main(String[] args) throws SQLException{
        
        FireJavaLog loggerDb = new FireJavaLog("log", "firejavalog", "localhost", "root", "");
        loggerDb.log("Testing LOG log", FireJavaLog.L_LOG);
        loggerDb.log("Testing WARN log", FireJavaLog.L_WARN);
        loggerDb.log("Testing SEVERE log", FireJavaLog.L_SEVERE);
        loggerDb.log("Testing CRITICAL log", FireJavaLog.L_CRITICAL);
        
        FireJavaLog loggerFile = new FireJavaLog("demolog.log");
        loggerFile.log("Testing LOG log", FireJavaLog.L_LOG);
        loggerFile.log("Testing WARN log", FireJavaLog.L_WARN);
        loggerFile.log("Testing SEVERE log", FireJavaLog.L_SEVERE);
        loggerFile.log("Testing CRITICAL log", FireJavaLog.L_CRITICAL);
        
        FireJavaLog loggerFileDate = new FireJavaLog("demolog.log", Boolean.FALSE);
        loggerFileDate.log("Testing LOG log", FireJavaLog.L_LOG);
        loggerFileDate.log("Testing WARN log", FireJavaLog.L_WARN);
        loggerFileDate.log("Testing SEVERE log", FireJavaLog.L_SEVERE);
        loggerFileDate.log("Testing CRITICAL log", FireJavaLog.L_CRITICAL);
       
        
    }
    
}

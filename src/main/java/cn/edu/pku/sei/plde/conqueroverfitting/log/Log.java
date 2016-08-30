package cn.edu.pku.sei.plde.conqueroverfitting.log;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {
	private Logger log;
    public Log(String path) {
    	try{
           log = Logger.getLogger(path); 
           log.setUseParentHandlers(false);  
           log.setLevel(Level.INFO); 
           File file = new File(path);
           if(file.exists())
        	   file.delete();
           FileHandler fileHandler = new FileHandler(path); 
           fileHandler.setLevel(Level.INFO); 
           fileHandler.setFormatter(new LogHander()); 
           log.addHandler(fileHandler); 
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
   } 
   
   public void logStr(String str){
	   log.info(str);
   }
   
   public void logSignLine(String str){
	   logStr("****************************************   " + str + "   ****************************************");
   }
}

class LogHander extends Formatter { 
    @Override 
    public String format(LogRecord record) { 
            return record.getMessage()+"\r\n"; 
    } 
}
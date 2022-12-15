package org.datacrow.core.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.datacrow.core.DcConfig;

public class DcLogManager {
	
	public static org.apache.logging.log4j.Logger getLogger(String className) {
		return LogManager.getLogger(className);
	}
	
	public static org.apache.logging.log4j.Logger getLogger(Class<?> clazz) {
		return LogManager.getLogger(clazz);
	}
	
	public static void configureLog4j(Level level, boolean console) {
	    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
	    Configuration config = ctx.getConfiguration();

	    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
	    loggerConfig.setLevel(level);
	    
        TriggeringPolicy tp = SizeBasedTriggeringPolicy.createPolicy("10M");
                
	    RollingFileAppender fa = RollingFileAppender.newBuilder().setName("DataCrow_LogFile")
	            .withFilePattern(DcConfig.getInstance().getDataDir() + "data_crow_%d{MM-dd-yy}.log.gz")
                .withAppend(true)
                .withPolicy(tp)
                .withFileName(DcConfig.getInstance().getDataDir() + "data_crow.log")
                .setLayout(PatternLayout.newBuilder().withPattern("%-5p %d  [%t] %C{2} (%F:%L) - %m%n").build())
                .setConfiguration(config).build();
        
        fa.start();
        config.addAppender(fa);
        ctx.getRootLogger().addAppender(config.getAppender(fa.getName()));
        
        DcLogAppender la = new DcLogAppender("DcLogAppender", null, null, false);
        la.start();
        config.addAppender(la);
        ctx.getRootLogger().addAppender(config.getAppender(la.getName()));
        
        ctx.updateLoggers();
	}
}

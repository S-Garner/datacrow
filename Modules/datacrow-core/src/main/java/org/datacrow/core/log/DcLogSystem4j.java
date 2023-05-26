package org.datacrow.core.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.datacrow.core.DcConfig;

public class DcLogSystem4j extends DcLogSystem {	
	
	@Override
	public void initialize(boolean debug) {
		isDebug(debug);
		
	    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
	    Configuration config = ctx.getConfiguration();

	    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
	    loggerConfig.setLevel(isDebug() ? Level.DEBUG : Level.INFO);
	    
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
	
	@Override
	public DcLogger getLogger(String name) {
		return new Log4jLogger(name);
	}
	
	private class Log4jLogger extends DcLogger {
		
		private Logger logger;
		
		public Log4jLogger(String name) {
			super(name);

			logger = LogManager.getLogger(name);
		}
		
		@Override
		public boolean isDebugEnabled() {
			return isDebug();
		}

		@Override
		public void error(String msg) {
			logger.error(msg);
		}

		@Override
		public void error(String msg, Throwable t) {
			logger.error(msg, t);	
		}
		
		@Override
		public void error(String msg, String error) {
			logger.error(msg, error);	
		}

		@Override
		public void error(Throwable t) {
			logger.error(t);
		}

		@Override
		public void error(Throwable t1, Throwable t2) {
			logger.error(t1, t2);
		}

		@Override
		public void fatal(String msg) {
			logger.fatal(msg);
		}

		@Override
		public void fatal(String msg, Throwable t) {
			logger.fatal(msg, t);
		}

		@Override
		public void fatal(Throwable t1, Throwable t2) {
			logger.fatal(t1, t2);
		}

		@Override
		public void debug(String msg) {
			logger.debug(msg);
		}

		@Override
		public void debug(String msg, Throwable t) {
			logger.debug(msg, t);
		}

		@Override
		public void debug(Throwable t1, Throwable t2) {
			logger.debug(t1, t2);
		}

		@Override
		public void warn(String msg) {
			logger.warn(msg);
		}

		@Override
		public void warn(String msg, Throwable t) {
			logger.warn(msg, t);
		}

		@Override
		public void warn(Throwable t1, Throwable t2) {
			logger.warn(t1, t2);
		}

		@Override
		public void info(String msg) {
			logger.info(msg);
		}

		@Override
		public void info(Object o) {
			logger.info(o);
		}

		@Override
		public void info(String msg, Throwable t) {
			logger.info(msg, t);
		}

		@Override
		public void info(Throwable t1, Throwable t2) {
			logger.info(t1, t2);
		}
	}
}

package org.datacrow.server.db;

import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.settings.DcSettings;

public class DatabaseCheckpointCreator extends Thread {

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DatabaseCheckpointCreator.class.getName());
	
	@Override
	public void run() {
		long wait = DcSettings.getLong(DcRepository.Settings.stDatabaseCheckpointIntervalMs);
		
		while (true) {
            try {
            	// every so many milliseconds (commonly every 12 hours, a checkpoint is created.
            	// this forces all data to be written to disk. It does delay transactions that are current taking place, but, it does
            	// not close any connection or disrupt save or update actions. 
            	
                sleep(wait);
                DatabaseManager.getInstance().createCheckpoint();
                logger.info("Database checkpoint has been created successfully");
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
	}
}

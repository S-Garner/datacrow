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

		if (wait == -1) {
			logger.info("Checkpoint task will not be scheduled, as per settings (-1).");
		} else if (wait < 10000) {
			logger.info("Cannot schedule the checkpoint task to a value lower than 10.000ms. Reverting back to the default of 43.200.000ms (12h).");
			wait = 43200000;
		}
		
		logger.info("Scheduling the checkpoint task run every " + wait + " milliseconds.");
		
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

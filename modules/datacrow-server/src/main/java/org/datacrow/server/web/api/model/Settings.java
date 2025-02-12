package org.datacrow.server.web.api.model;

import org.datacrow.core.DcRepository;
import org.datacrow.core.settings.DcSettings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Settings {

	@JsonProperty("maxUploadAttachmentSize")
	private final long maxUploadAttachmentSize; 
	
	public Settings() {
		maxUploadAttachmentSize = DcSettings.getLong(DcRepository.Settings.stMaximumAttachmentFileSize) * 1000; 
	}

	public long getMaximumAttachmentFileSize() {
		return maxUploadAttachmentSize;
	}
}

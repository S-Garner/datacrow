package org.datacrow.core.utilities;

public interface IImageConverterListener {
	
	public void notifyImageProcessed();
	
	public void notifyToBeProcessedImages(int count);
	
	public void notifyError(String s);
	
	public void notifyFinished();
}

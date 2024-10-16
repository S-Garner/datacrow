package org.datacrow.core.migration.itemexport;

import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.objects.DcObject;
import org.datacrow.core.pictures.Picture;

public class ExportItem {
	
	private final DcObject dco;
	
	private final Collection<Picture> pictures;
	
	public ExportItem(DcObject dco) {
		this.dco = dco;
		this.pictures = null;
	}	
	
	public ExportItem(DcObject dco, Collection<Picture> pictures) {

		this.dco = dco;
		this.pictures = pictures;
	}

	public DcObject getDco() {
		return dco;
	}
	
	public Collection<Picture> getPictures() {
		return pictures == null ? new ArrayList<Picture>() : pictures;
	}
}

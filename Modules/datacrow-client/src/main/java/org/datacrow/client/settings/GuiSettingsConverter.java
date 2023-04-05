package org.datacrow.client.settings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import org.datacrow.core.settings.ISettingsValueConverter;
import org.datacrow.core.settings.objects.DcColor;
import org.datacrow.core.settings.objects.DcDimension;
import org.datacrow.core.settings.objects.DcFont;

public class GuiSettingsConverter implements ISettingsValueConverter {
	
	public Object convert(Object o) {
		Object v = o;
		
		if (v instanceof Font) {
			Font f = (Font) v;
			v = new DcFont(f.getName(), f.getStyle(), f.getSize());
		} else if (v instanceof Color) {
			Color c = (Color) v;
			v = new DcColor(c.getRed(), c.getGreen(), c.getBlue());
		} else if (v instanceof Dimension) {
			Dimension d = (Dimension) v;
			v = new DcDimension(d.width, d.height);
		}
		
		return v;
	}
}

/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.org                             *
 *                                                                            *
 *                       This file is part of Data Crow.                      *
 *       Data Crow is free software; you can redistribute it and/or           *
 *        modify it under the terms of the GNU General Public                 *
 *       License as published by the Free Software Foundation; either         *
 *              version 3 of the License, or any later version.               *
 *                                                                            *
 *        Data Crow is distributed in the hope that it will be useful,        *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *           MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.             *
 *           See the GNU General Public License for more details.             *
 *                                                                            *
 *        You should have received a copy of the GNU General Public           *
 *  License along with this program. If not, see http://www.gnu.org/licenses  *
 *                                                                            *
 ******************************************************************************/

package org.datacrow.client.console.components;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JToolTip;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.Layout;
import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.settings.DcSettings;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

public class DcDateField extends JComponent implements IComponent {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcDateField.class.getName());
    
    private final DatePicker datePicker;
    
    public DcDateField() {
        this.setLayout(Layout.getGBL());

        DatePickerSettings dateSettings = new DatePickerSettings();
        
        dateSettings.setFontCalendarWeekdayLabels(ComponentFactory.getSystemFont());
        dateSettings.setFontCalendarDateLabels(ComponentFactory.getSystemFont());
        dateSettings.setFontCalendarWeekNumberLabels(ComponentFactory.getSystemFont());
        dateSettings.setFontClearLabel(ComponentFactory.getSystemFont());
        dateSettings.setFontInvalidDate(ComponentFactory.getStandardFont());
        dateSettings.setFontMonthAndYearMenuLabels(ComponentFactory.getSystemFont());
        dateSettings.setFontMonthAndYearNavigationButtons(ComponentFactory.getSystemFont());
        dateSettings.setFontTodayLabel(ComponentFactory.getSystemFont());
        dateSettings.setFontValidDate(ComponentFactory.getStandardFont());
        dateSettings.setFontVetoedDate(ComponentFactory.getStandardFont());

        String format = DcSettings.getString(DcRepository.Settings.stDateFormat);
        dateSettings.setFormatForDatesBeforeCommonEra(format);
        dateSettings.setFormatForDatesCommonEra(format);
        
        this.datePicker = new DatePicker(dateSettings);
        add(datePicker,
        		Layout.getGBC( 0, 0, 1, 1, 80.0, 80.0
               ,GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
    }

    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat(
                DcSettings.getString(DcRepository.Settings.stDateFormat));
    }
    
    @Override
    public void setEditable(boolean b) {
        setEnabled(false);
    }
    
    @Override 
    public void setEnabled(boolean b) {
    	datePicker.setEnabled(b);
    }
    
    @Override
    public void setValue(Object value) {
        if (value instanceof Date) {
            setValue((Date) value);
        } else if (value instanceof String) {
            try {
                setValue(getDateFormat().parse((String) value));
            } catch (Exception e) {
                logger.warn("Could not set [" + value + "]. Not a valid date", e);
            }
        }
    }      
    
    public void setValue(Date date) {
    	if (date instanceof java.sql.Date) {
    		java.util.Date utilDate = new java.util.Date(date.getTime());
    		date = utilDate;
    	}
    	
    	if (date == null) {
    		datePicker.clear();
    	} else {
        	datePicker.setDate(LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    	}
    }
    
    @Override
    public Object getValue() {
    	LocalDate localDate = datePicker.getDate();
    	return localDate == null ? null : Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    @Override
    public JToolTip createToolTip() {
        return new DcMultiLineToolTip();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(GUI.getInstance().setRenderingHint(g));
    }

    @Override
    public void refresh() {}

	@Override
	public void clear() {
		datePicker.clear();
	}
}
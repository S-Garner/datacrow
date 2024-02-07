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

package org.datacrow.client.console.components.lists.elements;

import java.awt.Color;
import java.awt.Dimension;

import org.datacrow.client.console.ComponentFactory;
import org.datacrow.client.console.components.DcPicturePane;
import org.datacrow.client.console.components.DcTextPane;
import org.datacrow.core.DcRepository;
import org.datacrow.core.objects.DcObject;

public class DcCardObjectListElement extends DcObjectListElement {
	
	public static final Dimension size = new Dimension(250, 250);
	
    private static final Dimension dimTxt = new Dimension(250, 45);
    private static final Dimension dimPicLbl = new Dimension(248, 198);

    private final DcTextPane fldTitle;
    private final DcPicturePane fldPicture;
    
    private boolean build = false;

    public DcCardObjectListElement(int module) {
        super(module);
        
        fldPicture = new DcPicturePane(false);
        fldPicture.setPreferredSize(dimPicLbl);
        fldPicture.setMinimumSize(dimPicLbl);
        fldPicture.setMaximumSize(dimPicLbl);

        fldTitle = ComponentFactory.getTextPane();
        fldTitle.setPreferredSize(dimTxt);
        fldTitle.setMinimumSize(dimTxt);
        fldTitle.setMaximumSize(dimTxt);        
        
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
    }

    @Override
    public void update(DcObject dco) {
        if (this.dco == null || this.dco.isNew()) {
            this.dco = dco;
            clear();
        } else {
            clear();
        }
    }    
    
    private String getDescription() {
    	if (dco == null) return "";
    	
        int[] fields = (int[]) dco.getModule().getSetting(DcRepository.ModuleSettings.stCardViewItemDescription);
        if (fields != null && fields.length > 0) {
            StringBuilder sb = new StringBuilder();
            String disp;
            for (int field :  fields) {
                disp = dco.getDisplayString(field);
                if (disp.length() > 0) {
                    if (sb.length() > 0)
                        sb.append(" / ");
                    sb.append(disp);
                }
            }
            
            if (sb.length() == 0) {
                return dco.toString();
            } else {
                return sb.toString();
            }
        } 
        return dco.toString();
    }

    public boolean isBuild() {
        return build;
    }
    
    @Override
    public void setBackground(Color color) {
        if (fldTitle != null)
            fldTitle.setBackground(color);
    }    
    
    private void setPicture() {
    	
//    	if (DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT) {
//		
//		
//		
//	} else {
//		//return DcConfig.getInstance().getConnector().getPictures(dco.getID());
//		File file = new File(dco.  );
//		
//		
//	}
    	
    	
//    	for (Picture p : pictures) {
//       		fldPicture.setImageIcon(p.getScaledPicture());
//            fldPicture.setScaled(false);
//            break;
//        }

        add(fldPicture);
    }
    
    @Override
    public void build() {
        build = true;

        fldTitle.setText(getDescription());
        
        setPicture();
        add(fldTitle);
          
        super.setBackground(ComponentFactory.getColor(DcRepository.Settings.stCardViewBackgroundColor));
        
        validate();
        repaint();
    }
    
    @Override
    public void clear() {
        super.clear();
        
        removeAll();
        
        if (fldPicture != null) {
        	fldPicture.clear();
        	fldPicture.setImageIcon(null);
        }

        validate();
        repaint();
        
        build = false;
    }
}
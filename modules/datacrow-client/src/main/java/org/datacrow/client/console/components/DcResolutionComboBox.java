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

import org.datacrow.core.settings.objects.DcDimension;

public class DcResolutionComboBox extends DcComboBox<DcDimension> {

	public DcResolutionComboBox() {
        super();
        
        addItem(new DcDimension(800, 600));
        addItem(new DcDimension(1366, 768));
        addItem(new DcDimension(1440, 768));
        addItem(new DcDimension(1800, 1600));
    	addItem(new DcDimension(1920, 1080));
    	addItem(new DcDimension(2560, 1440));
    	addItem(new DcDimension(3840, 2160));
    }
}
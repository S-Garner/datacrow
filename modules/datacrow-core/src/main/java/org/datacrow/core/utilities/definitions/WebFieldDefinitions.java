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

package org.datacrow.core.utilities.definitions;

import java.util.Collection;
import java.util.LinkedList;

public class WebFieldDefinitions implements IDefinitions {

	private static final long serialVersionUID = 1L;

	private final Collection<WebFieldDefinition> definitions = new LinkedList<WebFieldDefinition>();
    
    private final int moduleIdx;
    
    public WebFieldDefinitions(int moduleIdx) {
        this.moduleIdx = moduleIdx;
    }
    
    @Override
    public void add(Collection<Definition> c) {
        for (Definition definition : c)
            add(definition);
    }
    
    @Override
    public void add(Definition definition) {
        if (!exists(definition)) {
            definitions.add((WebFieldDefinition) definition);
        }
    }

    @Override
    public void clear() {
        definitions.clear();
    }

    @Override
    public Collection<WebFieldDefinition> getDefinitions() {
        return definitions;
    }

    @Override
    public int getSize() {
        return definitions.size();
    }         

    private void removeDefinition(int fieldIdx) {
    	WebFieldDefinition definition = null;
        for (WebFieldDefinition d : definitions) {
            if (d.getFieldIdx() == fieldIdx) {
                definition = d;
                break;
            }
        }
        
        if (definition != null)
            definitions.remove(definition);
    }
    
    @Override
    public boolean exists(Definition definition) {
        return definitions.contains(definition);
    }        
    
    @Override
    public void add(String s) {
        int field = Integer.parseInt(s);
        removeDefinition(field);
        add(new WebFieldDefinition(moduleIdx, field));
    }
}

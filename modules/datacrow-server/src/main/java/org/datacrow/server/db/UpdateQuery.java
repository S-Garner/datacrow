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

package org.datacrow.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.core.utilities.CoreUtilities;

public class UpdateQuery extends Query {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(UpdateQuery.class.getName());
    
    private final DcObject dco;

    public UpdateQuery(SecuredUser su, DcObject dco) {
        super(su, dco.getModule().getIndex());
        this.dco = dco;
    }
    
    @SuppressWarnings({ "unchecked", "resource" })
	@Override
    public List<DcObject> run() {
        
        Collection<Collection<DcMapping>> references = new ArrayList<Collection<DcMapping>>();
        Collection<Object> values = new ArrayList<Object>();
        
        // create non existing references
        createReferences(dco);

        PreparedStatement ps = null;
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = DatabaseManager.getInstance().getConnection(getUser());
            stmt = conn.createStatement();

            StringBuffer sbValues = new StringBuffer();

            Collection<DcMapping> c;
            DcModule mappingMod;
            String sql;
            String s;
            
            for (DcField field : dco.getFields()) {

                // Make sure only changed fields are updated
                if (!dco.isChanged(field.getIndex()))
                    continue;
                
                if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                    c = (Collection<DcMapping>) dco.getValue(field.getIndex());
                    
                    if (c != null) references.add(c);
                    
                    if (dco.isChanged(field.getIndex())) {
                        mappingMod = DcModules.get(DcModules.getMappingModIdx(field.getModule(), field.getReferenceIdx(), field.getIndex()));
                        sql = "DELETE FROM " + mappingMod.getTableName() + " WHERE " +  
                                     mappingMod.getField(DcMapping._A_PARENT_ID).getDatabaseFieldName() + " = '" + dco.getID() + "'";
                        stmt.execute(sql);
                    }
                } else if (dco.isChanged(field.getIndex()) && !field.isUiOnly()) {
                    if (sbValues.length() > 0)
                        sbValues.append(", ");
    
                    sbValues.append(field.getDatabaseFieldName());
                    sbValues.append(" = ?");
                    values.add(getQueryValue(dco, field.getIndex()));
                }
            }
    
            if (dco.getModuleIdx() == DcModules._USER) {
            	UpdateQuery query;
            	for (DcObject child : dco.getCurrentChildren()) {
            		if (child.isChanged()) {
            			query = new UpdateQuery(getUser(), child);
            			query.run();
            		}
            	}
            }
            
            s = sbValues.toString();
            if (dco.getModule().getType() != DcModule._TYPE_MAPPING_MODULE && !CoreUtilities.isEmpty(values)) {
                ps = conn.prepareStatement("UPDATE " + dco.getTableName() + " SET " + s + "\r\n WHERE ID = '" + dco.getID() + "'");
                setValues(ps, values);
                ps.execute();
                ps.close();
            }
    
            for (Collection<DcMapping> mappings : references) {
                saveReferences(mappings, dco.getID());
            }

            for (DcField f : dco.getFields()) {
                if (dco.isChanged(f.getIndex()) &&
                    f.getValueType() == DcRepository.ValueTypes._ICON) {
                    saveIcon((String) dco.getValue(f.getIndex()), f, dco.getID());
                }
            }

            setSuccess(true);
        } catch (SQLException e) {
            setSuccess(false);
            logger.error("An error occured while running the query", e);
        }
        
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            logger.error("Error while closing connection", e);
        }
        
        return null;
    }
}

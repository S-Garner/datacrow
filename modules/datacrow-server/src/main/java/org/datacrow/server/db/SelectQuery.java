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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilterConverter;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.server.data.DataManager;

public class SelectQuery extends Query {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(SelectQuery.class.getName());
    
    private final int[] fields;
    private final DataFilter df;
    
    /**
     * Constructs a new Query object from a data filter.
     */
    public SelectQuery(SecuredUser su, DcObject dco, int[] fields) {
        super(su, dco.getModule().getIndex());
        this.fields = fields;
        this.df = new DataFilter(dco);
    }
    
    /**
     * Constructs a new Query object from a data filter.
     */
    public SelectQuery(SecuredUser su, DataFilter df, int[] fields) {
        super(su, df.getModule());
        this.fields = fields;
        this.df = df;
    }
    
    @Override
    @SuppressWarnings("resource")
    public List<DcObject> run()  {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<DcObject> items = new ArrayList<DcObject>();
        
        DataFilterConverter dfc = new DataFilterConverter(df);
        String sql = dfc.toSQL(fields, true, true);
        
        logger.debug(sql);
        
        try {
            conn = DatabaseManager.getInstance().getAdminConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            items.addAll(DataManager.getInstance().convert(rs, fields));
            setSuccess(true);
            
        } catch (SQLException e) {
            logger.error("Error (" + e +") while executing query: " + sql, e);
            setSuccess(false);
        } finally {
        	try { if (rs != null) rs.close(); } catch (Exception e) {logger.error("Could not close resource");}
        	try { if (stmt != null) stmt.close(); } catch (Exception e) {logger.error("Could not close resource");}
        }
        
        return items;
    }
}

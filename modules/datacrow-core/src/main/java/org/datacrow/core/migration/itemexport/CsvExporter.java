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

package org.datacrow.core.migration.itemexport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.DcThread;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcAssociate;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;

public class CsvExporter extends ItemExporter {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(CsvExporter.class.getName());
    
    
    
    public CsvExporter(
    		int moduleIdx, 
    		int mode, 
    		boolean processChildren) throws Exception {
    	
        super(moduleIdx, "CSV", mode, false);
    }

    private void writeBytes(String value, boolean separator) throws IOException {
        String s = value;
        s = s.replaceAll("\t", "");
        if (!s.equals("\r\n")) {
            s = s.replaceAll("\r", " ");
            s = s.replaceAll("\n", " ");
        }
        
        if (separator)
            s = "\t" + s;
        
        bos.write(s.getBytes("UTF8"));
    }

    @Override
    public String getName() {
        return DcResources.getText("lblTextExport");
    }

    @Override
    public String getFileType() {
        return "txt";
    }
    
    @Override
    public DcThread getTask() {
        return new Task(items);
    }

    private class Task extends DcThread {
        
        private List<String> items;
        
        public Task(Collection<String> items) {
            super(null, "CSV export to " + file);
            this.items = new ArrayList<String>();
            this.items.addAll(items);
        }

        @Override
        public void run() {
            try {
                create();
            } catch (Exception exp) {
                success = false;
                logger.error(DcResources.getText("msgErrorWhileCreatingReport", exp.toString()), exp);
                client.notify(DcResources.getText("msgErrorWhileCreatingReport", exp.toString()));
            } finally {
                client.notifyTaskCompleted(true, null);
            }
            
            items.clear();
            items = null;
        }
        
        @SuppressWarnings("unchecked")
        public void create() throws Exception {
            ItemExporterUtilities utilities = new ItemExporterUtilities(file.toString(), settings);
           
            if (items == null || items.size() == 0) return;
            
            // create the table and the header
            int counter = 0;
           
            DcModule module = getModule();

            DcField field;
            for (int fieldIdx : getFields(getModule())) {
               if (isCanceled()) break;

               field = module.getField(fieldIdx);
               if (field != null) {
                    writeBytes(field.getSystemName(), counter != 0);
                    counter++;
                }                
            }        
            
            writeBytes("\r\n", false);
            
            counter = 0;
            String s;
            Connector conn = DcConfig.getInstance().getConnector();
            
            for (String item : items) {
            	
            	DcObject dco = conn.getItem(moduleIdx, item, null);
                
                if (isCanceled()) break;
                
                client.notify(DcResources.getText("msgExportingX", dco.toString()));
                int fieldCounter = 0;
                Object o;
                s = "";
                for (int fieldIdx : getFields(getModule())) {
                    if (isCanceled()) break;

                    field = module.getField(fieldIdx);
                    
                    if (isCanceled()) break;

                    o = dco.getValue(field.getIndex());
                    
                    if (field != null) { 
                        s = "";
                        
                        if (o instanceof Collection && 
                            DcModules.get(field.getReferenceIdx()).getType() == DcModule._TYPE_ASSOCIATE_MODULE) {
                            
                           for (DcObject subDco : (Collection<DcObject>) o) {
                                if (subDco instanceof DcMapping)
                                    subDco = ((DcMapping) subDco).getReferencedObject();

                                if (subDco != null) { 
                                    s += (s.length() > 0 ? ", " : "");
                                    s += ((DcAssociate) subDco).getNameNormal(); 
                                }
                            }
                       	} else if (field.getValueType() == DcRepository.ValueTypes._PICTURE) {
                       		Collection<Picture> pictures =  DcConfig.getInstance().getConnector().getPictures(dco.getID());
                        	for (Picture p : pictures) {
                        		if (p.getFilename().endsWith(field.getDatabaseFieldName() + ".jpg"))
                        			s = utilities.getImageURL(p);
                        	}
                        } else {
                            s = dco.getDisplayString(field.getIndex());
                        }
                        
                        writeBytes(s, fieldCounter != 0);
                        fieldCounter++;
                    }
                }
                
                writeBytes("\r\n", false);
                counter++;
                client.notifyProcessed();
                bos.flush();
            }
            
            bos.close();
            
            if (!isCanceled())
            	client.notify(DcResources.getText("lblExportHasFinished"));
        }        
    }       
}

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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.DcThread;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;

/**
 * Creates a XML extract for a collection of items. The Resulting XML can be used in reports 
 * and or can be used to migrate information from one system to another.
 * 
 * @author Robert Jan van der Waals
 */
public class XmlExporter extends ItemExporter {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(XmlExporter.class.getName());
    
    public XmlExporter(
    		int moduleIdx, 
    		int mode, 
    		boolean processChildren) throws Exception {
    	
        super(moduleIdx, "XML", mode, processChildren);
    }

    @Override
    public String getFileType() {
        return "xml";
    }
  
    @Override
    public DcThread getTask() {
        return new Task(items);
    }

    @Override
    public String getName() {
        return DcResources.getText("lblXmlExport");
    }
    
    private class Task extends DcThread {
        
    	private final Collection<String> items = new ArrayList<String>();
        
        public Task(Collection<String> items) {
            super(null, "XML export to " + file);
            
            this.items.addAll(items);
        }

        @Override
        public void run() {
            try {
                
                String schemaFile = file.toString();
                schemaFile = schemaFile.substring(0, schemaFile.lastIndexOf(".xml")) + ".xsd";
                
                if (!isCanceled()) {
                    List<DcModule> exportedModules = generateXml(schemaFile);
                    
                    if (!isCanceled() && exportedModules != null && exportedModules.size() > 0)
                    	generateXsd(schemaFile, exportedModules);
                }
                
            } catch (Exception exp) {
                success = false;
                logger.error(DcResources.getText("msgErrorWhileCreatingReport", exp.toString()), exp);
                client.notify(DcResources.getText("msgErrorWhileCreatingReport", exp.toString()));
            } finally {
                if (items != null) items.clear();
                client.notifyTaskCompleted(true, null);
            }
        }

        /**
         * Writes the schema file. The schema is based on the object of the current module.
         * @param schemaFile
         * @throws IOException
         */
        private void generateXsd(String schemaFile, List<DcModule> modules) throws Exception {
            XmlSchemaWriter schema = new XmlSchemaWriter(schemaFile, settings, modules);
            schema.create();
        }

        /**
         * Create the XML file as follows:
         * - First, process the main items (as selected by the user)
         * - Capture the references (by module)
         * - Pass the ordered list of modules + (used) references onto the next generation part
         * - Export the references (used references, as assigned to the main items)
         * @param schemaFile
         * @return the ordered module list
         * @throws Exception
         */
        private List<DcModule> generateXml(String schemaFile) throws Exception {
            
        	
        	if (items == null || items.size() == 0) return null;
            
            XmlWriter writer = new XmlWriter(bos, file.toString(), schemaFile, settings);
            writer.startDocument();
            
            Connector conn = DcConfig.getInstance().getConnector();
            DcObject dco;
            
            Map<DcModule, Collection<String>> references = new LinkedHashMap<DcModule, Collection<String>>();

            writer.startModule(getModule());
            writer.setIdent(2);
            
            ExportItem exportItem;
            
            for (String item : items) {
            	
                if (isCanceled()) break;
                
                dco = conn.getItem(getModule().getIndex(), item, null);
                
                if (getModule().getIndex() == DcModules._MEDIA) {
                	DcObject media = getModule().getItem();
                	media.copy(dco, fields, true, true);
                	exportItem = new ExportItem(media, DcConfig.getInstance().getConnector().getPictures(item));
                	processItem(writer, exportItem, references, true);
                } else {
                	exportItem = new ExportItem(dco, DcConfig.getInstance().getConnector().getPictures(item));
                	processItem(writer, exportItem, references, true);
                }
                
                client.notifyProcessed();
            }
            
            writer.setIdent(1);
            writer.endModule(getModule());
            
            // TODO: reinit the progress bar for the references!
            for (DcModule module : references.keySet()) {
            	
            	writer.resetIdent();
            	writer.startModule(module);
            	
            	for (String id : references.get(module)) {
            		writer.setIdent(1);
	                dco = conn.getItem(module.getIndex(), id, null);
	                processItem(writer, new ExportItem(dco), null, false);
            	}
            	
            	writer.resetIdent();
            	writer.endModule(module);
            }
            
            writer.endDocument();
            
            if (!isCanceled())
            	client.notify(DcResources.getText("lblExportHasFinished"));
            
            List<DcModule> exportedModules = new LinkedList<DcModule>();
            exportedModules.add(getModule());
            exportedModules.addAll(references.keySet());
            return exportedModules;
        }
        
        private void processItem(XmlWriter writer, ExportItem exportItem,  Map<DcModule, Collection<String>> references, boolean full) throws Exception {
        	
        	DcObject dco = exportItem.getDco();
        	
            writeDCO(writer, exportItem, references);

            if (full && processChildren) {
                if (dco.getModule().getChild() != null) {
                
                    dco.loadChildren(null);
                    
                    writer.startRelations(dco.getModule().getChild());
                    writer.setIdent(3);

                    for (DcObject child : dco.getChildren()) {
                        writeDCO(writer, new ExportItem(child), references);
                        writer.endEntity(child);
                    }
                    
                    writer.setIdent(2);
                    writer.endRelations(dco.getModule().getChild());
                }
            }

            if (full && settings.getBoolean(ItemExporterSettings._INCLUDE_IMAGES))
            	writer.writePictures(exportItem.getPictures());
            
            if (full && settings.getBoolean(ItemExporterSettings._COPY_AND_INCLUDE_ATTACHMENTS))
            	writer.writeAttachments(dco.getID());
            
            //writer.resetIdent();
            writer.endEntity(dco);
            bos.flush();
            dco.cleanup();
        }        
        
        @SuppressWarnings({ "unchecked" })
		private void writeDCO(XmlWriter writer, ExportItem exportItem, Map<DcModule, Collection<String>> references) throws Exception {
        	
        	DcObject dco = exportItem.getDco();
        	
    		writer.startEntity(dco);
        	
            client.notify(DcResources.getText("msgExportingX", dco.toString()));

            Collection<String> currentReferences;
            DcObject reference;
            
            for (int fieldIdx : getFields(dco.getModule())) {
                DcField field = dco.getField(fieldIdx);
                
                if (	dco.isFilled(fieldIdx) &&
                		references != null &&
                		DcModules.getReferencedModule(field).getIndex() != moduleIdx &&
                		(field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                		 field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE)) {
                	
                	currentReferences = references.get(DcModules.get(field.getReferenceIdx()));
                	currentReferences = currentReferences == null ? new ArrayList<String>() : currentReferences;
                	
                	if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
                		for (DcObject r : (Collection<DcObject>) dco.getValue(fieldIdx)) {
                			if (!currentReferences.contains(r.getValue(DcMapping._B_REFERENCED_ID)))
                				currentReferences.add((String) r.getValue(DcMapping._B_REFERENCED_ID));
                		}
                	} else {
                		reference = (DcObject) dco.getValue(fieldIdx);
            			if (!currentReferences.contains(reference.getID()))
            				currentReferences.add(reference.getID());
                	}
                	
                	references.put(DcModules.getReferencedModule(field), currentReferences);
                } 
                
                if (field != null && !field.getSystemName().endsWith("_persist")) 
                    writer.writeAttribute(exportItem, field.getIndex());
            }
        }
    }    
}

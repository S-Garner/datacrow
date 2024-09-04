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

package org.datacrow.core.migration.itemimport;
        
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.DcThread;
import org.datacrow.core.attachments.Attachment;
import org.datacrow.core.clients.IItemImporterClient;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.migration.XmlUtilities;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.utilities.Converter;
import org.datacrow.core.utilities.CoreUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * TODO: XML Import: picture & attachments
 */
public class XmlImporter extends ItemImporter {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(XmlImporter.class.getName());
    
    public XmlImporter(int moduleIdx, int mode) throws Exception {
        super(DcConfig.getInstance().getConnector().getUser(), moduleIdx, "XML", mode);
    }
    
    @Override
    public Collection<String> getSettingKeys() {
        Collection<String> settingKeys = super.getSettingKeys();
        settingKeys.add(DcRepository.Settings.stImportMatchAndMerge);
        return settingKeys;
    }
 
	@Override
	public int getType() {
		return _TYPE_XML;
	}    
    
    @Override
    protected void initialize() {}

    @Override
    public DcThread getTask() {
        return new Task(file, client);
    }

    @Override
    public String[] getSupportedFileTypes() {
        return new String[] {"xml"};
    }
    
    @Override
    public void cancel() {}

    @Override
    public String getName() {
        return DcResources.getText("lblXImport", "XML");
    }
    
    private class Task extends DcThread {
        
        private File file;
        private IItemImporterClient listener;
        
        public Task(File file, IItemImporterClient listener) {
            super(null, "XML import for " + file);
            this.file = file;
            this.listener = listener;
        }
    
        private DcObject parseItem(DcModule module, Element eItem) throws Exception {
            DcObject dco = module.getItem();
            Node node;
            
            String value;
            String fieldTag;

            for (DcField field : module.getFields()) {
            	
                if ((   field.isUiOnly() && 
                        field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION) ||  
                        field.getIndex() == DcObject._SYS_EXTERNAL_REFERENCES) 
                    continue;
                
                fieldTag = XmlUtilities.getFieldTag(field);
                
                NodeList nlField = eItem.getChildNodes();
                Element eField = null;
                
                try {
                    for (int i = 0; i < nlField.getLength(); i++) {
                        node = nlField.item(i);
                        
                        if (node.getNodeName().equals(fieldTag))
                            eField = (Element) node;
                    }
                } catch (Exception e) {
                    logger.error("Could not match " + fieldTag + " with an existing child tag", e);
                }
                
                // field was not found; skip - TODO: log!
                if (eField == null) 
                    continue;
                
                if (	field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
                		field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
                	
                    // retrieve the items by their module name
                    DcModule referenceMod = DcModules.get(field.getReferenceIdx());
                    String referenceName = Converter.getValidXmlTag(referenceMod.getSystemObjectName());
                    NodeList elReferences = eField.getElementsByTagName(referenceName);
                    
                    String id;
                    String name;
                    
                    DcField fieldId = referenceMod.getField(DcObject._ID);
                    DcField fieldDisplay = referenceMod.getField(referenceMod.getSystemDisplayFieldIdx());
                    
                    DcObject reference;
                    DcObject existingReference;
                    
                    for (int j = 0; elReferences != null && j < elReferences.getLength(); j++) {
                        // retrieve the values by the display field index (the system display field index)
                    	
                    	
                    	
                    	Element eReference = (Element) elReferences.item(j);
                        reference = referenceMod.getItem();
                        
                        id = eReference.getElementsByTagName(XmlUtilities.getFieldTag(fieldId)).getLength() > 0 ?
                        	 eReference.getElementsByTagName(XmlUtilities.getFieldTag(fieldId)).item(0).getTextContent() : null;
                        name = eReference.getElementsByTagName(XmlUtilities.getFieldTag(fieldDisplay)).getLength() > 0 ?
                           	 eReference.getElementsByTagName(XmlUtilities.getFieldTag(fieldDisplay)).item(0).getTextContent() : null;
                        
                        if (name == null || id == null)
                        	continue;
                        
                        existingReference = DcConfig.getInstance().getConnector().getItem(referenceMod.getIndex(), id);
                        
                        reference.setValue(fieldId.getIndex(), id);
                        reference.setValue(fieldDisplay.getIndex(), name);
                        
                    	dco.createReference(field.getIndex(), existingReference != null ? existingReference : reference);
                    }
                } else {
                    value = eField.getTextContent();
                    if (!CoreUtilities.isEmpty(value))
                        setValue(dco, field.getIndex(), value, listener);
                }
            }
            
            dco.setIDs();
            
            NodeList nlPictures = eItem.getElementsByTagName("picture");
            Element ePicture;
            String link;
            Picture picture;
            for (int i = 0; i < nlPictures.getLength(); i++) {
            	
            	if (nlPictures.item(i).getNodeType() != Node.ELEMENT_NODE)
            		continue;
            	
            	ePicture = (Element) nlPictures.item(i);
            	
            	if (ePicture.getElementsByTagName("link").getLength() > 0) {
            		
                	if (ePicture.getElementsByTagName("link").item(0).getNodeType() == Node.ELEMENT_NODE) {
	                	link = ePicture.getElementsByTagName("link").item(0).getTextContent();
	                	picture = new Picture(dco.getID(), new DcImageIcon(new URL(link)));
	                	dco.addNewPicture(picture);
                	}
            	}
            }
            
            NodeList nlAttachments = eItem.getElementsByTagName("attachment");
            Element eAttachment;
            Attachment attachment;
            File file;
            for (int i = 0; i < nlAttachments.getLength(); i++) {
            	
            	if (nlAttachments.item(i).getNodeType() != Node.ELEMENT_NODE)
            		continue;
            	
            	eAttachment = (Element) nlAttachments.item(i);
            	
            	
            	if (eAttachment.getElementsByTagName("link").getLength() > 0) {
            		
                	if (eAttachment.getElementsByTagName("link").item(0).getNodeType() == Node.ELEMENT_NODE) {
	                	link = eAttachment.getElementsByTagName("link").item(0).getTextContent();
	                	
	                	try {
	    	            	file = new File(new URL(link).getFile());
	    	            	
	    	            	attachment = new Attachment(dco.getID(), file);
	    	            	attachment.setData(CoreUtilities.readFile(file));
	    	            	
	    	            	dco.addNewAttachment(attachment);
	                	} catch (Exception e) {
	                		// TODO: log to client
	                		logger.error(e, e);
	                	}
                	}
            	}
            }            
            
            return dco;
        }
        
        private DcModule getModule(String holderTag) {
        	for (DcModule module : DcModules.getAllModules()) {
        		if (XmlUtilities.getElementTagForList(module).equals(holderTag))
        			return module;
        	}
        	
        	return null;
        }
        
        @Override
        public void run() {
            InputSource input = null;
            @SuppressWarnings("resource")
			InputStreamReader in = null;
            BufferedReader reader = null;
            
            try {
            	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                
                in = new InputStreamReader(new FileInputStream(file), "utf-8" );
                reader = new BufferedReader (in);
                input = new InputSource(reader);

                Document document = db.parse(input);
                
                NodeList nlTop = document.getElementsByTagName("data-crow-objects");
                
                if (nlTop != null && nlTop.getLength() > 0) {
                	
                	NodeList nlItemHolders = nlTop.item(0).getChildNodes();
                	
                	NodeList nlItems;
                	Node eItemHolder;
                	Element eItem;
                	DcModule module;
                	DcObject dco;
                	
                	DcModule cm;
                	NodeList nlChildren;
                	Element eChild;
                	DcObject child;
                	
                	for (int i = nlItemHolders.getLength() - 1; i >= 0 && !isCanceled(); i--) {
                        
                    	if (nlItemHolders.item(i).getNodeType() != Node.ELEMENT_NODE)
                    		continue;
                    	
                    	eItemHolder = (Element) nlItemHolders.item(i);
                    	module = getModule(eItemHolder.getNodeName());
                    	
                    	// skip if not found
                    	// TODO: log this as an issue
                    	if (module == null || module.isAbstract())
                    		continue;
                    	
                    	nlItems = eItemHolder.getChildNodes();
                    	for (int j = 0; j < nlItems.getLength() && !isCanceled(); j++) {
                    		
                    		try {
                    			
                            	if(nlItems.item(j).getNodeType() != Node.ELEMENT_NODE)
                            		continue;
                    			
	                    		eItem = (Element) nlItems.item(j);
	                    		dco = parseItem(module, eItem);

	                    		cm = module.getChild();
	                        	if (cm != null && !cm.isAbstract()) {
	                                nlChildren = eItem.getElementsByTagName(XmlUtilities.getElementTag(cm));
	                                
	                                for (int k = 0; nlChildren != null && k < nlChildren.getLength(); k++) {
	                                    eChild = (Element) nlChildren.item(k);
	                                    child = parseItem(cm, eChild);
	                                    dco.addChild(child);
	                                }
	                        	}
	                        	
	                        	listener.notifyProcessed(dco);
                            } catch (Exception e) {
                                listener.notify(e.getMessage());
                                logger.error(e, e) ;
                            }
                    	}
                    }
                }
                
                listener.notifyTaskCompleted(true, null);
                
            } catch (Exception e) {
                logger.error(e, e) ;
            } finally {
            	try { if (reader != null) reader.close(); } catch (Exception e) {logger.error("Could not close resource");}
            	try { if (in != null) in.close(); } catch (Exception e) {logger.error("Could not close resource");}
            }
        }
    }
}

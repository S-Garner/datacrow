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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.DcRepository.ValueTypes;
import org.datacrow.core.attachments.Attachment;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.utilities.CoreUtilities;

public class XmlWriter extends XmlBaseWriter {
    
    private ItemExporterSettings settings;
    
    private final String schemaFile;
    private final int stepSize = 4;

    private int tagIdent;
    private int valueIdent;
    
    private ItemExporterUtilities utilities;
    
	public XmlWriter(String filename, String schemaFile, ItemExporterSettings properties) throws IOException {
        this(new BufferedOutputStream(new FileOutputStream(filename)), filename, schemaFile, properties);
    }
    
    public XmlWriter(BufferedOutputStream bos, String filename, String schemaFile, ItemExporterSettings settings) {
        super(bos);
        
        this.schemaFile = schemaFile;
        this.settings = settings;
        this.utilities = new ItemExporterUtilities(filename, settings);
        
        resetIdent();
    }    
    
    public void resetIdent() {
        tagIdent = stepSize * 1;
        valueIdent =  stepSize * 2;
    }
    
    public void setIdent(int times) {
        tagIdent =  (stepSize * (1 * times)) + stepSize;
        valueIdent = stepSize * (2 * times);
    }
    
    public void startDocument() throws IOException {
        writeTag("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        newLine();
        
        String xsd = new File(schemaFile).getName();
        writeTag("<" + uberTag + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"" + xsd + "\">");
        newLine();
    }

    public void endDocument() throws IOException  {
        writeTag("</" + uberTag + ">");
        newLine();
        bos.close();
    }

    public void startModule(DcModule m) throws IOException {
		ident(tagIdent);
		writeTag("<" + getValidTag(m.getSystemObjectName()) + "-items>");
		newLine();
    }

    public void endModule(DcModule m) throws IOException {
		ident(tagIdent);
		writeTag("</" + getValidTag(m.getSystemObjectName()) + "-items>");
		newLine();
    }        

    public void startEntity(DcObject dco) throws IOException {
		ident(tagIdent);
		writeTag("<" + getValidTag(dco.getModule().getSystemObjectName()) + ">");
		newLine();
    }

    public void endEntity(DcObject dco) throws IOException {
        ident(tagIdent);
        writeTag("</" + getValidTag(dco.getModule().getSystemObjectName()) + ">");
        newLine();
    }
    
    @SuppressWarnings("unchecked")
    public void writeAttribute(ExportItem exportItem, int field) throws IOException {
    	
    	DcObject dco = exportItem.getDco();
    	
        if (	dco.getField(field) == null || 
        		(!dco.isFilled(field) && dco.getModule().getField(field).getValueType() != DcRepository.ValueTypes._PICTURE))
            return; 
        
        ident(valueIdent);
        
        String tag = XmlUtilities.getFieldTag(dco.getField(field));
        
        writeTag("<" + tag + ">");
        writeValue(exportItem, field);
        writeTag("</" + tag + ">");
        newLine();
        
        if (dco.getField(field).getValueType() == ValueTypes._DCOBJECTCOLLECTION) {
        	
        	ident(valueIdent);
        	
            writeTag("<" + XmlUtilities.getFieldTag(dco.getField(field)) + "-list>");
            
            if (dco.isFilled(field)) {
                StringBuffer sb = new StringBuffer();
                for (DcObject ref : (Collection<DcObject>) dco.getValue(field)) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(ref.toString());
                }
                write(sb.toString());
            }
            
            writeTag("</" + XmlUtilities.getFieldTag(dco.getField(field)) + "-list>");
            newLine();
        }
    }
    
    public void writeAttachments(String ID) throws IOException {

    	Collection<Attachment> attachments = DcConfig.getInstance().getConnector().getAttachmentsList(ID);
    	
    	if (attachments.size() > 0) {
        	setIdent(1);
        	ident(valueIdent);

    		writeTag("<attachments>");
    		newLine();
        	setIdent(2);
    		ident(tagIdent);
    		
    		String url;
    		for (Attachment attachment : attachments) {
    			url = utilities.getAttachmentURL(attachment);
    			
    			if (!CoreUtilities.isEmpty(url)) {
    				writeTag("<attachment>");
    				newLine();
    	        	setIdent(3);
    				ident(tagIdent);
    				writeTag("<link>");
    				write(url);
    				writeTag("</link>");
    				newLine();
    				setIdent(2);
    				ident(tagIdent);
    				writeTag("</attachment>");
    				newLine();
    			}
    		}
    		setIdent(1);
    		ident(valueIdent);
    		writeTag("</attachments>");
    		newLine();
    	}
    }

    public void writePictures(Collection<Picture> pictures) throws IOException {
    	
    	if (pictures.size() > 0) {
        	setIdent(1);
        	ident(valueIdent);

    		writeTag("<pictures>");
    		newLine();
        	setIdent(2);
    		ident(tagIdent);
    		
    		String url;
    		for (Picture picture : pictures) {
    			url = utilities.getImageURL(picture);
    			
    			if (!CoreUtilities.isEmpty(url)) {
    				writeTag("<picture>");
    				newLine();
    	        	setIdent(3);
    				ident(tagIdent);
    				writeTag("<link>");
    				write(url);
    				writeTag("</link>");
    				newLine();
    				setIdent(2);
    				ident(tagIdent);
    				writeTag("</picture>");
    				newLine();
    			}
    		}
    		setIdent(1);
    		ident(valueIdent);
    		writeTag("</pictures>");
    		newLine();
    	}    	
    }    
    
    public void startRelations(DcModule childModule) throws IOException {
        ident(valueIdent);
        writeTag("<" + getValidTag(childModule.getSystemObjectName()) + "-children>");
        newLine();
    }

    public void endRelations(DcModule childModule) throws IOException {
        ident(valueIdent);
        writeTag("</" + getValidTag(childModule.getSystemObjectName()) + "-children>");
        newLine();
    }
    
    @SuppressWarnings("unchecked")
    private void writeValue(ExportItem exportItem, int field) throws IOException {
    	
    	Collection<Picture> pictures = exportItem.getPictures();
    	DcObject dco = exportItem.getDco();
    	Object o = dco.getValue(field);

    	if (	dco.getField(field).getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
    			dco.getField(field).getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
    	   
    		newLine();

            tagIdent += (stepSize * 2);
            valueIdent += (stepSize * 2);
            
            Collection<DcObject> items = new ArrayList<DcObject>();
            if (dco.getField(field).getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE)
            	items.add((DcObject) o);	
            else
            	items.addAll((Collection<? extends DcObject>) o);
            
            
            ExportItem eiRef;
            
            for (DcObject ref : items) {
               if (ref instanceof DcMapping)
                    ref = ((DcMapping) ref).getReferencedObject();

                if (ref != null) { 
	                startEntity(ref);
	                int fieldIdx = ref.getSystemDisplayFieldIdx();
	                
	                eiRef = new ExportItem(ref);
	                		
	                writeAttribute(eiRef, DcObject._ID);
	                writeAttribute(eiRef, fieldIdx);
	                
	                endEntity(ref);
                }
            }

            valueIdent -= (stepSize * 2);
            tagIdent -= (stepSize * 2);
            ident(valueIdent);
       	} else if (dco.getField(field).getValueType() == DcRepository.ValueTypes._PICTURE) {
        	for (Picture p : pictures) {
        		if (p.getFilename().endsWith(dco.getField(field).getDatabaseFieldName() + ".jpg"))
        			write(utilities.getImageURL(p));
        	}       		
        } else if (o instanceof Date) {
        	Date date = (Date) o;
            write(new SimpleDateFormat("yyyy-MM-dd").format(date));
        }  else if (o instanceof Number) {
           write(o.toString());
        } else {
            String text = dco.getDisplayString(field);
            int maximumLength = settings.getInt(ItemExporterSettings._MAX_TEXT_LENGTH);
            if (maximumLength > 0 && text.length() > maximumLength) {
                text = text.substring(0, maximumLength);
                
                if (text.lastIndexOf(" ") > -1)
                    text = text.substring(0, text.lastIndexOf(" ")) + "...";
            }
            write(text);
        }
    }
    
    private void ident(int x) throws IOException {
        String s = "";
        for (int i = 0; i < x; i++)
            s += " ";
        bos.write(s.getBytes());
    }    
    
    private void write(String value) throws IOException {
        String s = value;
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("\"", "&quot;");
        s = s.replaceAll("'", "&apos;");
        bos.write(s.getBytes("UTF8"));
    }    
}

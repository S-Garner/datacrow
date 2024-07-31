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
import java.util.List;

import org.datacrow.core.DcRepository;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcObject;

/**
 * @author Robert Jan van der Waals
 */
public class XmlSchemaWriter extends XmlBaseWriter {
    
    private final ItemExporterSettings settings;
    
    private final List<DcModule> modules;
    
    public XmlSchemaWriter(
    		String filename,
    		ItemExporterSettings settings,
    		List<DcModule> modules) throws IOException {
    	
        super(filename);
        
        this.settings = settings;
        this.modules = modules;
    }
    
    public void create() throws IOException {
    	    	
        startDocument();
        
        for (DcModule m : modules)
        	handle(m);
        
        if (settings.getBoolean(ItemExporterSettings._INCLUDE_IMAGES)) {
        	writeLine("<xsd:complexType name=\"picture-items-type\">", 1);
        	writeLine("<xsd:sequence>", 2);
        	writeLine("<xsd:element name=\"picture\" type=\"picture-type\" />", 3);
        	writeLine("</xsd:sequence>", 2);
        	writeLine("</xsd:complexType>", 1);
        	newLine();
        	
        	writeLine("<xsd:complexType name=\"picture-type\">", 1);
        	writeLine("<xsd:sequence>", 2);
        	writeLine("<xsd:element name=\"link\" type=\"xsd:string\" />", 3);
        	writeLine("</xsd:sequence>", 2);
        	writeLine("</xsd:complexType>", 1);
        	newLine();
        }

        if (settings.getBoolean(ItemExporterSettings._COPY_AND_INCLUDE_ATTACHMENTS)) {
        	writeLine("<xsd:complexType name=\"attachment-items-type\">", 1);
        	writeLine("<xsd:sequence>", 2);
        	writeLine("<xsd:element name=\"attachment\" type=\"attachment-type\" />", 3);
        	writeLine("</xsd:sequence>", 2);
        	writeLine("</xsd:complexType>", 1);
        	newLine();
        	
        	writeLine("<xsd:complexType name=\"attachment-type\">", 1);
        	writeLine("<xsd:sequence>", 2);
        	writeLine("<xsd:element name=\"link\" type=\"xsd:string\" />", 3);
        	writeLine("</xsd:sequence>", 2);
        	writeLine("</xsd:complexType>", 1);        	
        	newLine();
        }
        
        endDocument();
    }
    
    private void startDocument() throws IOException{
        writeLine("<?xml version=\"1.0\"?>", 0);
        writeLine("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">", 0);
        writeLine("<xsd:element name=\"data-crow-objects\">", 1);
        writeLine("<xsd:complexType>", 1);
        writeLine("<xsd:sequence>", 2);
        
        for (DcModule m : modules) {
            writeLine(
        		"<xsd:element" +
        		" name=\"" + XmlUtilities.getElementTagForList(m) + "\"" + 
        		" type =\"" + XmlUtilities.getElementTagTypeForList(m) + "\"/>", 3);
        }
        
        writeLine("</xsd:sequence>", 2);
        writeLine("</xsd:complexType>", 1);
        writeLine("</xsd:element>", 0);
    }
    
    private void endDocument() throws IOException {
        writeLine("</xsd:schema>", 0);
        bos.flush();
        bos.close();
    }    
    
    private void handle(DcModule m) throws IOException {
    	
        if (m.getChild() != null) {
        	writeModule(m.getChild(), true);
            newLine();
        }
        
        writeModule(m, true);
    }
    
    private void writeField(DcField field) throws IOException {

        if (	field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
        		field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {

            writeLine("<xsd:element name=\"" + XmlUtilities.getFieldTag(field) + "\" nillable=\"true\" minOccurs=\"0\">", 3);
            writeLine("<xsd:complexType>", 4);
            writeLine("<xsd:sequence>", 5);
            
            
            writeLine("<xsd:element name=\"" + XmlUtilities.getElementTag(field) + "\" maxOccurs=\"unbounded\">", 6);
            writeLine("<xsd:complexType>", 7);
            writeLine("<xsd:sequence>", 8);

            DcModule m = DcModules.getReferencedModule(field);
            writeField(m.getField(DcObject._ID));
            writeField(m.getField(m.getSystemDisplayFieldIdx()));

            writeLine("</xsd:sequence>", 8);
            writeLine("</xsd:complexType>", 7);
            writeLine("</xsd:element>", 6);
            writeLine("</xsd:sequence>", 5);
            writeLine("</xsd:complexType>", 4);
            writeLine("</xsd:element>", 3);
            
            writeLine("<xsd:element name=\"" + XmlUtilities.getFieldTag(field) + "-list\" nillable=\"true\" minOccurs=\"0\"/>", 3);

        } else {
            String type;
            switch (field.getValueType()) {
            case DcRepository.ValueTypes._BIGINTEGER :
                type = "long";
                break;
            case DcRepository.ValueTypes._BOOLEAN :
                type = "boolean";
                break;
            case DcRepository.ValueTypes._DATETIME :
            case DcRepository.ValueTypes._DATE :
                type = "date";
                break;
            case DcRepository.ValueTypes._LONG :
                type = "integer";
                break;
            default:
                type = "string";
            }
            
            writeLine("<xsd:element name=\"" + XmlUtilities.getFieldTag(field) + "\" type=\"xsd:" + type + "\" nillable=\"true\" minOccurs=\"0\"/>" , 3);
        }        
    }
    
    private void writeModule(DcModule m, boolean detailed) throws IOException {
        newLine();
        
        writeLine("<xsd:complexType name=\"" + XmlUtilities.getElementTagTypeForList(m) + "\" >", 1);
        writeLine("<xsd:sequence>", 2);
        writeLine("<xsd:element type=\"" + XmlUtilities.getElementTagType(m) +  "\" name=\"" + XmlUtilities.getElementTag(m) + "\" maxOccurs=\"unbounded\"/>", 3);
        writeLine("</xsd:sequence>", 2);
        writeLine("</xsd:complexType>", 1);
        
        newLine();
        
        writeLine("<xsd:complexType name=\"" + XmlUtilities.getElementTagType(m) + "\" >", 1);
        writeLine("<xsd:sequence>", 2);
        

        for (int fieldIdx : m.getFieldIndices()) {
            DcField field = m.getField(fieldIdx);
            if (	field != null &&
            		field.getValueType() != DcRepository.ValueTypes._PICTURE &&
            		!field.getSystemName().endsWith("_persist"))
            	writeField(field);
        }
        
        
        // TODO: incorrect!
        if (detailed && m.getChild() != null) {
            String name = getValidTag(m.getChild().getSystemObjectName() + "-children");
            writeLine("<xsd:element name=\"" + name + "\" nillable=\"true\"/>", 3);
        }

        // only export images and attachments for top level items or its children
        if (detailed) {
            if (settings.getBoolean(ItemExporterSettings._INCLUDE_IMAGES)) {
            	writeLine("<xsd:element name=\"pictures\" minOccurs=\"0\" maxOccurs=\"unbounded\" type=\"picture-items-type\" />", 3);
            }
            
            if (settings.getBoolean(ItemExporterSettings._COPY_AND_INCLUDE_ATTACHMENTS)) {
            	writeLine("<xsd:element name=\"attachments\" minOccurs=\"0\" maxOccurs=\"unbounded\" type=\"attachment-items-type\" />", 3);
            }
        }
        
        writeLine("</xsd:sequence>", 2);
        writeLine("</xsd:complexType>", 1);
    }
}
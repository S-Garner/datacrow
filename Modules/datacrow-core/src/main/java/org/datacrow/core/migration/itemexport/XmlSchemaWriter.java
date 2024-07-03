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

import org.datacrow.core.modules.DcModule;

/**
 * @author Robert Jan van der Waals
 */
public class XmlSchemaWriter extends XmlBaseWriter {
    
    private final Collection<XmlReference> references = new ArrayList<XmlReference>();
    
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
        
        if (settings.getBoolean(ItemExporterSettings._INCLUDE_IMAGES)) {
        	writeLine("<xsd:element name=\"picture\" type=\"type-picture\"/>", 1);
        	writeLine("<xsd:complexType name=\"type-picture\">", 1);
        	writeLine("<xsd:sequence>", 2);
        	writeLine("<xsd:element name=\"link\" type=\"xsd:string\" />", 3);
        	writeLine("</xsd:sequence>", 2);
        	writeLine("</xsd:complexType>", 1);
        	newLine();
        }

        if (settings.getBoolean(ItemExporterSettings._COPY_AND_INCLUDE_ATTACHMENTS)) {
        	writeLine("<xsd:element name=\"attachment\" type=\"type-attachment\"/>", 1);
        	writeLine("<xsd:complexType name=\"type-attachment\">", 1);
        	writeLine("<xsd:sequence>", 2);
        	writeLine("<xsd:element name=\"link\" type=\"xsd:string\" />", 3);
        	writeLine("</xsd:sequence>", 2);
        	writeLine("</xsd:complexType>", 1);        	
        	newLine();
        }
        
        endDocument();
    }
    
//    private void handle(DcObject dco, Collection<String> handled) throws IOException {
//    	
//    	DcField field;
//    	DcModule sm;
//    	DcObject so;
//        
//    	for (int fieldIdx : dco.getFieldIndices()) {
//            
//        	field = dco.getField(fieldIdx);
//            
//            if (field == null) continue;
//            
//            if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION ||
//            	field.getValueType() == DcRepository.ValueTypes._DCOBJECTREFERENCE) {
//            	
//                sm = DcModules.get(field.getReferenceIdx());
//                so = sm.getItem();
//
//                if (!handled.contains(so.getModule().getSystemObjectName())) {
//                    writeDco(so, false);
//                    newLine();
//                }
//                
//                handled.add(so.getModule().getSystemObjectName());
//            }
//        }
//
//        if (dco.getModule().getChild() != null) {
//            writeDco(dco.getModule().getChild().getItem(), true);
//            newLine();
//            handled.add(dco.getModule().getChild().getSystemObjectName());
//        }
//        
//        writeDco(dco, true);
//        handled.add(dco.getModule().getSystemObjectName());
//    }
    
//    private void addReference(String name, String reference) {
//        XmlReference xmlReference = new XmlReference(name, reference);
//        if (!references.contains(xmlReference))
//            references.add(xmlReference);
//    }
    
//    private void writeField(DcField field) throws IOException {
//        String label = getValidTag(field.getSystemName());
//
//        if (field.getValueType() == DcRepository.ValueTypes._DCOBJECTCOLLECTION) {
//            DcModule sm = DcModules.get(field.getReferenceIdx());
//            String name = getTagName(field);
//            String reference = getValidTag(sm.getSystemObjectName());
//
//            writeLine("<xsd:element name=\"" + name + "\" minOccurs=\"0\" />", 3);
//            addReference(name, reference);
//            writeLine("<xsd:element name=\"" + label + "-list\" type=\"xsd:string\" minOccurs=\"0\" nillable=\"true\" />", 3);
//        } else {
//            String type;
//            switch (field.getValueType()) {
//            case DcRepository.ValueTypes._BIGINTEGER :
//                type = "long";
//                break;
//            case DcRepository.ValueTypes._BOOLEAN :
//                type = "boolean";
//                break;
//            case DcRepository.ValueTypes._DATETIME :
//            case DcRepository.ValueTypes._DATE :
//                type = "date";
//                break;
//            case DcRepository.ValueTypes._LONG :
//                type = "integer";
//                break;
//            default:
//                type = "string";
//            }
//            
//            writeLine("<xsd:element name=\"" + label + "\" type=\"xsd:" + type + "\" nillable=\"true\" minOccurs=\"0\" />", 3);
//        }        
//    }
    
//    private void writeDco(DcObject dco, boolean detailed) throws IOException {
//        String baseName = getValidTag(dco.getModule().getSystemObjectName());
//        
//        newLine();
//        writeLine("<xsd:element name=\"" + baseName + "\" type=\"type-" + baseName + "\"/>", 1);
//        writeLine("<xsd:complexType name=\"type-" + baseName + "\">", 1);
//        writeLine("<xsd:sequence>", 2);
//        
//        if (    dco.getModule().getType() == DcModule._TYPE_PROPERTY_MODULE || 
//                dco.getModule().getType() == DcModule._TYPE_ASSOCIATE_MODULE) {
//            
//            int field = dco instanceof DcProperty ? DcProperty._A_NAME : DcAssociate._A_NAME;
//            String label = getValidTag(dco.getField(field).getSystemName());
//
//            writeLine("<xsd:element name=\"" + getValidTag(dco.getField(DcObject._ID).getSystemName()) + "\" type=\"xsd:string\"/>", 3);
//            writeLine("<xsd:element name=\"" + label + "\" type=\"xsd:string\"/>", 3);
//            
//        } else if (
//                dco.getModule().getType() == DcModule._TYPE_MEDIA_MODULE || 
//                dco.getModule().getType() == DcModule._TYPE_MODULE) {
//            
//            for (int fieldIdx : dco.getFieldIndices()) {
//                DcField field = dco.getField(fieldIdx);
//                if (field != null && !field.getSystemName().endsWith("_persist"))
//                	writeField(field);
//            }
//        }
//        
//        if (detailed && dco.getModule().getChild() != null) {
//            String name = getValidTag(dco.getModule().getChild().getSystemObjectNamePlural());
//            String reference = getValidTag(dco.getModule().getChild().getSystemObjectName());
//            
//            writeLine("<xsd:element name=\"" + name + "\" nillable=\"true\"/>", 3);
//            addReference(name, reference);
//        }
//
//        // only export images and attachments for top level items or its children
//        if (detailed) {
//            if (settings.getBoolean(ItemExporterSettings._INCLUDE_IMAGES)) {
//            	writeLine("<xsd:element name=\"pictures\" minOccurs=\"0\" />", 3);
//            	addReference("pictures", "picture");
//            }
//            
//            if (settings.getBoolean(ItemExporterSettings._COPY_AND_INCLUDE_ATTACHMENTS)) {
//            	writeLine("<xsd:element name=\"attachments\" minOccurs=\"0\" />", 3);
//            	addReference("attachments", "attachment");
//            }            
//        }
//        
//        writeLine("</xsd:sequence>", 2);
//        writeLine("</xsd:complexType>", 1);
//    }
    
    private void startDocument() throws IOException{
        writeLine("<?xml version=\"1.0\"?>", 0);
        writeLine("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">", 0);
        writeLine("<xsd:element name=\"data-crow-objects\">", 1);
        writeLine("<xsd:complexType>", 1);
        writeLine("<xsd:sequence>", 2);
        
        for (DcModule m : modules) {
            writeLine(
            		"<xsd:element maxOccurs=\"unbounded\" ref=\"" + 
                    getValidTag(m.getSystemObjectName() + "-items")  + "\"/>", 3);
        }
        
        writeLine("</xsd:sequence>", 2);
        writeLine("</xsd:complexType>", 1);
        writeLine("</xsd:element>", 0);
    }
    
    private void endDocument() throws IOException {
        writeReferences();
        writeLine("</xsd:schema>", 0);
        bos.flush();
        bos.close();
    }
    
    private void writeReferences() throws IOException {
        
        for (XmlReference reference : references) {
            
            newLine();

            writeLine("<xsd:element name=\"" + reference.getName() + "\">", 1);
            writeLine("<xsd:complexType>", 2);
            writeLine("<xsd:sequence>", 3);
            writeLine("<xsd:element maxOccurs=\"unbounded\" ref=\"" + reference.getReference() + "\"/>", 4);
            writeLine("</xsd:sequence>", 3);
            writeLine("</xsd:complexType>", 2);
            writeLine("</xsd:element>", 1);
        }        
    }
}
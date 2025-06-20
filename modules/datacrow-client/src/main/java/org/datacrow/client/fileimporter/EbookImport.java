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

package org.datacrow.client.fileimporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.datacrow.core.fileimporter.FileImporter;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Book;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.Hash;
import org.datacrow.core.utilities.isbn.ISBN;

/**
 * E-Book (Electronical Book) file importer.
 * @author Robert Jan van der Waals
 */
public class EbookImport extends FileImporter {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(EbookImport.class.getName());
    
    public EbookImport() {
        super(DcModules._BOOK);
    }
    
    @Override
    public FileImporter getInstance() {
        return new EbookImport();
    }

    @Override
    public String[] getSupportedFileTypes() {
        return new String[] {
                "html",
                "htm",
                "txt",  // N/A
                "chm",  // CHM parser
                "doc",  // Office Parse
                "docx", // Office Parser
                "odt",  // Open Office Parser
                "pdf",  // PDF Box
                "epub", // EPUB Parser
                "prc",  
                "rtf",
                "pdb", 
                "kml", 
                "html",  // HTML Parser 
                "htm",   // HTML Parser
                "prc", 
                "lit",
                "fb2"
        }; 
    }
    
    @Override
    public boolean allowReparsing() {
        return true;
    }
    
    private void findAndSetIsbn(DcObject book, String s) {
        ISBN isbn = new ISBN();
        if (isbn.parse(s)) {
            book.setValue(Book._J_ISBN10, isbn.getIsbn10());
            book.setValue(Book._N_ISBN13, isbn.getIsbn13());
        }
    }
    
    @Override
    public DcObject parse(String filename, int directoryUsage) {
        DcObject book = DcModules.get(DcModules._BOOK).getItem();
        
        try {
            book.setValue(Book._A_TITLE, getName(filename, directoryUsage));
            book.setValue(Book._SYS_FILENAME, filename);
            
            // check if the filename contains an ISBN
            findAndSetIsbn(book, filename);
            
            InputStream is = null;
            try {
                is = new FileInputStream(new File(filename));

                AutoDetectParser parser = new AutoDetectParser();
                BodyContentHandler handler = new BodyContentHandler(-1);
                Metadata metadata = new Metadata();
                ParseContext conext = new ParseContext();

                parser.parse(is, handler, metadata, conext);
                
                String author =  metadata.get(org.apache.tika.metadata.Office.AUTHOR);
                author = author == null ? metadata.get(TikaCoreProperties.CONTRIBUTOR) : author;
                
                String creator = metadata.get(TikaCoreProperties.CREATOR);
                String description = metadata.get(TikaCoreProperties.DESCRIPTION);
                String publisher = metadata.get(TikaCoreProperties.PUBLISHER);
                String pagecount = metadata.get(org.apache.tika.metadata.Office.PAGE_COUNT);
                String title = metadata.get(TikaCoreProperties.TITLE);
                    
                if (!CoreUtilities.isEmpty(author))
                    book.createReference(Book._G_AUTHOR, author);
                else if (!CoreUtilities.isEmpty(creator))
                    book.createReference(Book._G_AUTHOR, creator);

                if (!CoreUtilities.isEmpty(title))
                    book.setValue(Book._A_TITLE, title);

                if (!CoreUtilities.isEmpty(description))
                    book.setValue(Book._B_DESCRIPTION, description);

                if (!CoreUtilities.isEmpty(publisher))
                    book.createReference(Book._F_PUBLISHER, publisher);
                
                if (!CoreUtilities.isEmpty(pagecount)) {
                    try { 
                        book.setValue(Book._T_NROFPAGES, Long.parseLong(pagecount));
                    } catch (NumberFormatException nfe) {
                        logger.debug("Could not parse number of pages for " + pagecount, nfe);
                    }
                }
            } finally {
                if (is != null) is.close();
            }
            
            if (filename.toLowerCase().endsWith(".pdf")) {
                PDFRenderer renderer;
                
                try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(filename))) {
                	book.setValue(Book._T_NROFPAGES, Long.valueOf(document.getNumberOfPages()));
                    
                    renderer = new PDFRenderer(document);
                    renderer.setSubsamplingAllowed(true);
                    BufferedImage bi = renderer.renderImageWithDPI(0, 300f, ImageType.RGB);
                    book.addNewPicture(new Picture(book.getID(), new DcImageIcon(bi)));
                
                    PDFTextStripper stripper = new PDFTextStripper();
                    stripper.setStartPage(0);
                    stripper.setEndPage(4);
                    String text = stripper.getText(document);
                    
                    stripper.setStartPage(document.getNumberOfPages() - 4);
                    stripper.setEndPage(document.getNumberOfPages() - 1);
                    
                    text += stripper.getText(document);
    
                    findAndSetIsbn(book, text);
                }
            }                

            Hash.getInstance().calculateHash(book);
        } catch (OutOfMemoryError err) {
            logger.error(err, err);
            getClient().notify(DcResources.getText("msgOutOfMemory"));
        } catch (Exception exp) {
            logger.error(exp, exp);
            getClient().notify(DcResources.getText("msgCouldNotReadInfoFrom", filename));
        } catch (Error err) {
            logger.error(err, err);
            getClient().notify(DcResources.getText("msgCouldNotReadInfoFrom", filename));
        }
        return book;
    }

	@Override
	public String getFileTypeDescription() {
		return DcResources.getText("lblEBookFiles");
	}
}

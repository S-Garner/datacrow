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

package org.datacrow.core.utilities.isbn;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.utilities.CoreUtilities;

public class ISBN {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ISBN.class.getName());
    
    private static final String CheckDigits = new String("0123456789X0");
    
    private String isbn10 = "";
    private String isbn13 = "";
    
    public ISBN () {}
    
    public ISBN (String isbn) throws InvalidBarCodeException {
        setIsbn(isbn);
    }
    
    public boolean isValid() {
        return !CoreUtilities.isEmpty(isbn10) && !CoreUtilities.isEmpty(isbn13);
    }
    
    public boolean parse(String s) {
        
        boolean found = false;
        
        s = s.replaceAll("%20", "");
        s = s.replaceAll("-", "");
        s = s.replaceAll(" ", "");
        
        Pattern p = Pattern.compile("(?<!\\d)\\d{10,13}(?!\\d)");
        Matcher m = p.matcher(s);
        
        String isbn;
        while(m.find()) {
            try {
                isbn = m.group();
                
                if (isISBN10(isbn)) {
                    isbn10 = isbn;
                    isbn13 = convertToIsbn13(isbn10);
                    found = true;
                }

                if (isISBN13(isbn)) {
                    isbn13 = isbn;
                    isbn10 = convertToIsbn10(isbn13);
                    found = true;
                }                

            } catch (InvalidBarCodeException ibce) {
                logger.debug("[" + m.group() +  "] is not a valid ISBN " + ibce);
            }
        }
        
        return found;
    }
    
    private void setIsbn(String isbn) throws InvalidBarCodeException {
        if (isISBN10(isbn)) {
            isbn10 = isbn;
            isbn13 = convertToIsbn13(isbn10);
        }

        if (isISBN13(isbn)) {
            isbn13 = isbn;
            isbn10 = convertToIsbn10(isbn13);
        }        
    }
    
    
    public String getIsbn10() {
        return isbn10;
    }

    public String getIsbn13() {
        return isbn13;
    }
    
    private int CharToInt(char a) {
        switch (a) {
        case '0':
            return 0;
        case '1':
            return 1;
        case '2':
            return 2;
        case '3':
            return 3;
        case '4':
            return 4;
        case '5':
            return 5;
        case '6':
            return 6;
        case '7':
            return 7;
        case '8':
            return 8;
        case '9':
            return 9;
        default:
            return -1;
        }
    }

    private String convertToIsbn10(String ISBN) throws InvalidBarCodeException {
        String s9;
        int i, n, v;

        s9 = ISBN.substring(3, 12);
        n = 0;
        for (i = 0; i < 9; i++) {
            v = CharToInt(s9.charAt(i));
            if (v == -1)
                throw new InvalidBarCodeException();
            else
                n = n + (10 - i) * v;
        }

        n = 11 - (n % 11);
        
        return s9 + CheckDigits.substring(n, n + 1);
    }

    private String convertToIsbn13(String ISBN10) throws InvalidBarCodeException {
        String s12;
        int i, n, v;
        boolean ErrorOccurred;
        ErrorOccurred = false;
        s12 = "978" + ISBN10.substring(0, 9);
        n = 0;
        for (i = 0; i < 12; i++) {
            if (!ErrorOccurred) {
                v = CharToInt(s12.charAt(i));
                if (v == -1)
                    throw new InvalidBarCodeException();
                else {
                    if ((i % 2) == 0)
                        n = n + v;
                    else
                        n = n + 3 * v;
                }
            }
        }

        n = n % 10;
        if (n != 0) n = 10 - n;
        return s12 + CheckDigits.substring(n, n + 1);
    }
    
    private boolean isISBN13(String isbn) {
        String s = "";
        
        if (isbn != null) {
            char[] chars = isbn.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if ("1234567890".indexOf(chars[i]) > -1) 
                    s += chars[i];
            }
        }
        
        return s.length() == 13;
    }

    private static boolean isISBN10(String isbn) {
        String s = "";
        
        if (isbn != null) {
            char[] chars = isbn.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if ("1234567890X".indexOf(chars[i]) > -1) 
                    s += chars[i];
            }
        }
        
        return s.length() == 10;
    }
}
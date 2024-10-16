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

package org.datacrow.core.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Simplification for retrieving data from a specific address.
 */
public class HttpConnectionUtil {

    /**
     * Creates a new connection.
     * @param url
     * @return
     * @throws HttpConnectionException
     */
    public static HttpConnection getConnection(URL url) throws HttpConnectionException {
        return new HttpConnection(url);
    }
    
    /**
     * Retrieves the page content (UTF8).
     * @param url
     * @return
     * @throws HttpConnectionException
     */
    public static String retrievePage(String url) throws HttpConnectionException {
        return retrievePage(getURL(url), StandardCharsets.UTF_8);
    }

    /**
     * Retrieves the page content using the supplied character set.
     * @param url
     * @param charset
     * @throws HttpConnectionException
     */
    public static String retrievePage(String url, Charset charset) throws HttpConnectionException {
        return retrievePage(getURL(url), charset);
    }

    /**
     * Retrieves the page content (UTF8).
     * @param url
     * @throws HttpConnectionException
     */
    public static String retrievePage(URL url) throws HttpConnectionException {
        return retrievePage(url, StandardCharsets.UTF_8);
    }

    /**
     * Retrieves the page content using the supplied character set.
     * @param url
     * @param charset
     * @throws HttpConnectionException
     */
    public static String retrievePage(URL url, Charset charset) throws HttpConnectionException {
        HttpConnection connection = new HttpConnection(url);
        String page = connection.getString(charset);
        connection.close();
        return page;
    }

    private static URL getURL(String url) throws HttpConnectionException {
        try {
            return new URL(url);
        } catch (MalformedURLException mue) {
            throw new HttpConnectionException(mue);
        }
    }
}

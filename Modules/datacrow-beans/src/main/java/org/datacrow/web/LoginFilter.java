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

package org.datacrow.web;

import java.io.IOException;
import java.io.Serializable;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.web.bean.LoginBean;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter
public class LoginFilter implements Filter, Serializable {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(ReferencesCache.class.getName());
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
    
    @Override
    public void doFilter(
            ServletRequest request, 
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        
        Instance<LoginBean> loginBean = CDI.current().select(LoginBean.class);
        boolean loggedOn = false;
        HttpServletRequest req = (HttpServletRequest) request;
        
        if (loginBean != null) { 
            LoginBean lb = loginBean.get();
            loggedOn = lb != null && lb.isLoggedIn();
            logger.debug("Login bean found. Instance is [" + lb + "] and someone is logged on: " + loggedOn);
        } else {
            logger.debug("Login bean not found: null");
        }
        
        if ( !req.getRequestURI().endsWith(".xhtml")) {
            // make sure we are not redirecting CSS style requests and the likes
            chain.doFilter(request, response);
        } else if (loggedOn || req.getRequestURI().endsWith("login.xhtml")) {
            logger.debug("Going to the requested page");
            chain.doFilter(request, response);
        } else {
            logger.debug("Referring to the login page");
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendRedirect("login.xhtml?faces-redirect=true");
        }
    }
}

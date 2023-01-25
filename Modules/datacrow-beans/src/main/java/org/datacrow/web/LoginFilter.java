package org.datacrow.web;

import java.io.IOException;
import java.io.Serializable;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.log.DcLogManager;
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
    
    private transient static Logger logger = DcLogManager.getLogger(ReferencesCache.class.getName());
    
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

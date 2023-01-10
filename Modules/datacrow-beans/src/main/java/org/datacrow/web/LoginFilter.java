package org.datacrow.web;

import java.io.IOException;
import java.io.Serializable;

import org.datacrow.web.bean.LoginBean;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
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
    
    @Inject
    public Instance<LoginBean> loginBean;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
    
    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        

        boolean loggedOn = false;
        HttpServletRequest req = (HttpServletRequest) request;
        
        if (loginBean != null) { 
            LoginBean lb = loginBean.get();
            loggedOn = lb != null && lb.isLoggedIn();
        }

        if (loggedOn || req.getRequestURI().endsWith("login.xhtml")) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
        }
    }
}

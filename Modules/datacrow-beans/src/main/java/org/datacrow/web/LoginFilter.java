package org.datacrow.web;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

@WebFilter
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
    
    public void doFilter(
            ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        /*
        HttpServletRequest req = (HttpServletRequest) request;
        
        if (loginBean != null && req.getRequestURI().endsWith("login.xhtml")) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
        }*/
    }
}

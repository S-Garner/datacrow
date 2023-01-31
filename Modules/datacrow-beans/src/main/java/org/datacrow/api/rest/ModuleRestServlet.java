package org.datacrow.api.rest;

import java.io.IOException;

import org.datacrow.core.utilities.CoreUtilities;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ModuleRestServlet extends HttpServlet {

    private String getModules() {
        return "modules";
    }

    private String getModule(String idx) {
        return "module " + idx;
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String id = (String) request.getParameter("id");
        String json = "";
        
        if (CoreUtilities.isEmpty(id)) {
            json = getModules();
        } else {
            json = getModule(id);
        }
        
        response.getOutputStream().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {}
}

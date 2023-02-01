package org.datacrow.api.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.api.rest.json.JsonField;
import org.datacrow.api.rest.json.JsonModule;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.server.serialization.SerializationHelper;
import org.datacrow.core.utilities.CoreUtilities;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ModuleRestServlet extends HttpServlet {

    private String getModules() {
        Collection<JsonModule> modules = new ArrayList<>();
        
        for (DcModule module : DcModules.getModules()) {
            if (DcModules.isTopModule(module.getIndex()))
                modules.add(new JsonModule(module));
        }
        
        Gson gson = SerializationHelper.getInstance().getGson();
        return gson.toJson(modules);
    }

    private String getModule(int idx) {
        JsonModule module = new JsonModule(DcModules.get(idx));
        Gson gson = SerializationHelper.getInstance().getGson();
        return gson.toJson(module);
    }
    
    private String getFields(int idx) {
        DcModule module = DcModules.get(idx);
        
        Collection<JsonField> fields = new ArrayList<>();
        for (DcField field : module.getFields()) {
            fields.add(new JsonField(field));
        }
        
        Gson gson = SerializationHelper.getInstance().getGson();
        return gson.toJson(fields);
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        
        String url = request.getRequestURI();
        
        String json = "";
        
        if (url.endsWith("/modules")) {
            String id = (String) request.getParameter("id");
            
            if (CoreUtilities.isEmpty(id))
                json = getModules();
            else
                json = getModule(Integer.valueOf(id));
        } else if (url.endsWith("/fields")) {
            String moduleIndex = (String) request.getParameter("module");
            json = getFields(Integer.valueOf(moduleIndex));
        }
        
        response.getOutputStream().println(json);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {}
}

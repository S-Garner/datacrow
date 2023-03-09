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

package org.datacrow.web.bean;

import java.io.Serializable;

import org.apache.logging.log4j.Level;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.security.SecuredUser;
import org.datacrow.server.LocalServerConnector;
import org.datacrow.server.security.SecurityCenter;
import org.datacrow.web.util.WebUtilities;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named
@SessionScoped
public class LoginBean implements Serializable {

	private LocalServerConnector conn = new LocalServerConnector();
	
	public LoginBean() {}

	public String getUsername() {
		return conn.getUsername();
	}

	public void setUsername(String username) {
		conn.setUsername(username);
	}
	
	public boolean isAdmin() {
	    return conn.getUser().isAdmin();
	}

	public String getPassword() {
	    return conn.getPassword();
	}

	public void setPassword(String password) {
		conn.setPassword(password);
	}
	
	public boolean isAuthorized(Object o) {
	    SecuredUser user = conn.getUser();
	    
	    if (user == null) return false;
	    if (user.isAdmin()) return true;
	    
	    if (o instanceof DcField) {
	        return user.isAuthorized((DcField) o);
	    } else if (o instanceof DcModule) {
	        return user.isAuthorized((DcModule) o);
        }
	    
	    return false;
	}
	
	public boolean isEditingAllowed(Object o) {
        SecuredUser user = conn.getUser();
        
        if (user == null) return false;
        if (user.isAdmin()) return true;
        
        if (o instanceof DcField) {
            return user.isEditingAllowed((DcField) o);
        } else if (o instanceof DcModule) {
            return user.isEditingAllowed((DcModule) o);
        }
        
        return false;
    }
	
	public boolean isLoggedIn() {
	    return conn.getUser() != null;
	}
	
	private void reset() {
	    
        try {
            ItemsBean itemsBean = (ItemsBean) WebUtilities.getBean("itemsBean");
            itemsBean.reset();
            
            conn.setUser(null);
        } catch (Exception e) {
            WebUtilities.log(Level.ERROR, e);
        }
	}
	
	public String logoff() {
	    reset();
        
        return "/login?faces-redirect=true";
	}
	
	public String login() {
	    
	    String username = conn.getUsername();
	    String password = conn.getPassword();
	    
	    reset();
	    
	    conn.setUsername(username);
	    
		try {
		    SecuredUser su =  SecurityCenter.getInstance().login(username, password);
		    conn.setUser(su);
		} catch (org.datacrow.core.security.SecurityException se) {
		    FacesContext.getCurrentInstance().addMessage(null, 
	                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", 
	                                     DcResources.getText("msgUserOrPasswordIncorrect")));
	        return "/login";
		}
			
        try {
            ModulesBean modulesBean = (ModulesBean) WebUtilities.getBean("modulesBean");
            modulesBean.load();
            
            ItemsBean itemsBean = (ItemsBean) WebUtilities.getBean("itemsBean");
            itemsBean.setSelectedModule(modulesBean.getSelectedModuleIdx());
            
        } catch (Exception e) {
            WebUtilities.log(Level.ERROR, e);
        }
        
		return "/index?faces-redirect=true";
	}
}

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

package plugins;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.apache.logging.log4j.Logger;
import org.datacrow.client.console.GUI;
import org.datacrow.client.console.windows.DcDialog;
import org.datacrow.client.console.windows.DcFrame;
import org.datacrow.client.util.launcher.URLLauncher;
import org.datacrow.core.IconLibrary;
import org.datacrow.core.console.IWindow;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcTemplate;
import org.datacrow.core.plugin.Plugin;
import org.datacrow.core.resources.DcResources;

public class Help extends Plugin {
	
	private static Logger logger = DcLogManager.getLogger(Help.class.getName());

	private static final long serialVersionUID = 1;

	private static final Map<String, String> index = new HashMap<>();
	
	public Help(DcObject dco, DcTemplate template, int viewIdx, int moduleIdx, int viewType) {
        super(dco, template, viewIdx, moduleIdx, viewType);
        
        fillHelpIndex();
    }
	
	private void fillHelpIndex() {
		index.put("dc.general.introduction","introduction");
		index.put("dc.general.wheretostart","introduction/#wheretostart");
		index.put("dc.general.starting","starting-data-crow");
		index.put("dc.general.screenlayout","the-ui");
		index.put("dc.general.grouping","grouping-items");
		index.put("dc.general.menubar","the-ui/#menubar");
		index.put("dc.general.toolbar","the-ui/#toolbar");
		index.put("dc.general.quickview","the-ui/#quickview");
		index.put("dc.general.defaultmodules","introduction/#modules");
		index.put("dc.general.log","the-log");
		index.put("dc.items.views","views");
		index.put("dc.items.tableview","views/#tableview");
		index.put("dc.items.cardview","views/#cardview");
		index.put("dc.items.templates","item-templates");
		index.put("dc.items.wizard","the-new-item-wizard");
		index.put("dc.items.itemform","the-item-form");
		index.put("dc.items.createmultiple","create-multiple-items");
		index.put("dc.items.itemform_property","maintaining-properties/#alternative-names");
		index.put("dc.items.itemform_multiref","the-item-form/#multireference");
		index.put("dc.items.mergeitems","maintaining-properties/#merging");
		index.put("dc.items.sort","sorting");
		index.put("dc.items.administration","maintaining-properties");
		index.put("dc.charts","charts");
		index.put("dc.filters","filters");
		index.put("dc.onlinesearch","online-search");
		index.put("dc.loans","managing-loans");
		index.put("dc.loanadmin","managing-loans#loan-administration");
		index.put("dc.reports","reporting");
		index.put("dc.security","user-management");
		index.put("dc.settings.fields","configuring-data-crow/#fieldsettings");
		index.put("dc.settings.font","configuring-data-crow/#fonts");
		index.put("dc.settings.regional","configuring-data-crow/#regional");
		index.put("dc.settings.fileassociations","configuring-data-crow/#fileassociations");
		index.put("dc.settings.general","configuring-data-crow/#general");
		index.put("dc.settings.http","configuring-data-crow/#http");
		index.put("dc.settings.colors","configuring-data-crow/#selectioncolor");
		index.put("dc.settings.module","configuring-data-crow/#modules");
		index.put("dc.settings.laf","configuring-data-crow/#laf");
		index.put("dc.settings.quickview","configuring-data-crow/#quickview");
		index.put("dc.settings.filehash","configuring-data-crow/#filehashing");
		index.put("dc.settings.cardview","configuring-data-crow/#cardview");
		index.put("dc.settings.tableview","configuring-data-crow/#tableview");
		index.put("dc.settings.itemformsettings","configuring-data-crow/#itemform");
		index.put("dc.settings.drivemappings","configuring-data-crow/#drivemappings");
		index.put("dc.settings.drivemappings","configuring-data-crow/#drivemappings");
		index.put("dc.settings.directoriesasdrives","configuring-data-crow/#dirsasdrives");
		index.put("dc.modules","introduction/#modules");
		index.put("dc.modules.create","module-design/#create");
		index.put("dc.modules.alter","module-design/#alter");
		index.put("dc.modules.fields","module-design/#fields");
		index.put("dc.modules.relate","module-design/#relate");
		index.put("dc.modules.delete","module-design/#delete");
		index.put("dc.modules.copy","module-design/#copy");
		index.put("dc.modules.export","module-design/#export");
		index.put("dc.modules.import","module-design/#import");
		index.put("dc.tools.wizard","tool-select-wizard");
		index.put("dc.tools.files","file-operations");
		index.put("dc.migration.wizard.importer","item-import-export/#import");
		index.put("dc.migration.wizard.exporter","item-import-export/#export");
		index.put("dc.tools.fileinfoimport","file-import");
		index.put("dc.tools.massupdate","mass-update");
		index.put("dc.tools.databaseeditor","database-editor");
		index.put("dc.tools.updateall","update-all");
		index.put("dc.tools.findreplace","find-replace");
		index.put("dc.tools.backup_restore","backup-restore");
		index.put("dc.tools.titlerewriter","title-rewriting");
		index.put("dc.tools.associatenamerewriter","person-name-rewriting");
		index.put("dc.tools.autonumbering","auto-numbering");
		index.put("dc.tools.drivemanager","drive-manager");
		index.put("dc.tools.filerenamer","file-renamer");
		index.put("dc.tools.resourceeditor","resource-editor");
		index.put("dc.tools.icalendar_export","managing-loans#export");
	}
    
    @Override
    public boolean isAdminOnly() {
        return false;
    }
    
    @Override
    public boolean isAuthorizable() {
        return false;
    }
    
	@Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        while (!(o instanceof IWindow) && o != null)
            o = ((Component) o).getParent();
        
        if (o != null) {
        	String key = null;
            if (o instanceof DcFrame)
            	key = ((DcFrame) o).getHelpIndex();
            else if (o instanceof DcDialog)
            	key = ((DcDialog) o).getHelpIndex();
            
            if (index.get(key) != null) {
            	String link = key == null ? "" : index.get(key);
                launch("https://datacrow.org/docs/" + link);
            } if (key == null) {
            	logger.error("Key has not been set for [" + e.getSource() + "]");
            	launch("https://datacrow.org/docs/");
            } else if (index.get(key) == null) {
        		logger.error("Help index [" + key + "] has not been mapped");
        		launch("https://datacrow.org/docs/");
            }
        } else {
        	launch("https://datacrow.org/docs/");  	
        }
    }
	
	private void launch(String link) {
        try {
        	URL url = new URL(link);
        	URLLauncher launcher = new URLLauncher(url);
        	launcher.launch();
        } catch (Exception exp) {
            GUI.getInstance().displayErrorMessage(exp.toString());
        }
	}
    
    @Override
    public ImageIcon getIcon() {
        return IconLibrary._icoHelp;
    }
    
    @Override
    public boolean isSystemPlugin() {
        return true;
    }

    @Override
    public KeyStroke getKeyStroke() {
        return KeyStroke.getKeyStroke("F1");
    }
    
    @Override
    public String getLabel() {
        return DcResources.getText("lblHelp");
    }
    
    @Override
    public String getHelpText() {
        return DcResources.getText("tpHelp");
    }
}

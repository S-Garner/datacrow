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

package org.datacrow.core.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.console.IMasterView;
import org.datacrow.core.console.UIComponents;
import org.datacrow.core.modules.DcMediaModule;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcAssociate;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcMediaObject;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.DcProperty;
import org.datacrow.core.objects.helpers.ContactPerson;
import org.datacrow.core.objects.helpers.Container;
import org.datacrow.core.objects.helpers.Movie;
import org.datacrow.core.objects.helpers.MusicAlbum;
import org.datacrow.core.objects.helpers.Software;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.settings.objects.DcDimension;
import org.datacrow.core.utilities.definitions.DcFieldDefinition;
import org.datacrow.core.utilities.definitions.DcFieldDefinitions;
import org.datacrow.core.utilities.definitions.QuickViewFieldDefinition;
import org.datacrow.core.utilities.definitions.QuickViewFieldDefinitions;

/**
 * Module specific settings.
 *
 * @see DcRepository.ModuleSettings
 * 
 * @author Robert Jan van der Waals
 */
public class DcModuleSettings extends Settings {
    
	private static final long serialVersionUID = 1L;

	private String _General = "lblGroupGeneral";
	
	private final int moduleIdx;
    
    /**
     * Initializes and loads all module settings
     * @param module
     */
    public DcModuleSettings(DcModule module) {
        super();
        
        moduleIdx = module.getIndex();
        
        createSettings(module);
        createSystemSettings(module);
        createDefinitions(module);
        
        // load the default settings (if available)
        String filename = module.getName().toLowerCase() + ".properties";
        File file = new File(new File(DcConfig.getInstance().getInstallationDir(), "modules"), filename);
        if (file.exists()) {
            // this is here for backwards compatibility
            setSettingsFile(file);
            load();
        }

        // load the user settings
        if (DcConfig.getInstance().isAllowLoadSettings()) {
            setSettingsFile(new File(DcConfig.getInstance().getModuleSettingsDir(), module.getName().toLowerCase() + ".properties"));
            load();
        }
        
        correctSettings(module);
    }
    
    private void correctSettings(DcModule module) {
        int[] fields = getIntArray(DcRepository.ModuleSettings.stOnlineSearchFieldOverwriteSettings);
        Collection<Integer> correctedFields = new ArrayList<Integer>();
        for (int field : fields) {
            DcField fld = module.getField(field);
            if (fld != null)
                correctedFields.add(Integer.valueOf(field));
        }
        
        int i = 0;
        fields = new int[correctedFields.size()];
        for (Integer field : correctedFields)
            fields[i++] = field.intValue();
        
        set(DcRepository.ModuleSettings.stOnlineSearchFieldOverwriteSettings, fields);
    }
    
    public int getModuleIdx() {
        return moduleIdx;
    }

    @Override
    protected void createGroups() {
        _General = "lblGroupGeneral";
        
        SettingsGroup generalGroup = new SettingsGroup(_General, "dc.Settings.GeneralSettings");
        addGroup(_General, generalGroup);
    }
    
    private void createDefinitions(DcModule module) {
        QuickViewFieldDefinitions qvDefinitions = new QuickViewFieldDefinitions(module.getIndex());
        
        for (DcField field : module.getFields()) {
            boolean enabled = field.isSystemField() ? false : true;
            qvDefinitions.add(new QuickViewFieldDefinition(field.getModule(), field.getIndex(), enabled, DcResources.getText("lblHorizontal"), 0));
        }
        
        addSetting(_General,
			    new Setting(DcRepository.ValueTypes._DEFINITIONGROUP,
		                    DcRepository.ModuleSettings.stQuickViewFieldDefinitions,
		                    qvDefinitions,
		                    -1,
		                    "",
		                    "",
		                    false,
		                    false, module.getIndex()));
         addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stFileImportRecursive,
                            Boolean.FALSE,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._TABLESETTINGS,
                            DcRepository.ModuleSettings.stTableSettings,
                            new DcTableSettings(module.getIndex()),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stShowPicturesInSeparateTabs,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stExportFields,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stMassUpdateUseOriginalServiceSettings,
                            Boolean.FALSE,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stMassUpdateAlwaysUseFirst,
                            Boolean.FALSE,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stMassUpdateServer,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex())); 
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stMassUpdateMode,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));    
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stMassUpdateRegion,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));          
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BIGINTEGER,
                            DcRepository.ModuleSettings.stDefaultSearchView,
                            IMasterView._LIST_VIEW,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex())); 
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BIGINTEGER,
                            DcRepository.ModuleSettings.stDefaultInsertView,
                            IMasterView._TABLE_VIEW,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stReportFile,
                            "",
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stReportType,
                            "",
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stSelectedReport,
                            "",
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
    }
    
    protected void createSettings(DcModule module) {
        
        if (module.isFileBacked()) {
            addSetting(_General,
                    new Setting(DcRepository.ValueTypes._STRING,
                                DcRepository.ModuleSettings.stFileRenamerPattern,
                                "",
                                -1,
                                "",
                                "",
                                false,
                                false,
                                module.getIndex()));
        }
        
        if (module.getIndex() == DcModules._CONTAINER) {
            addSetting(_General,
                    new Setting(DcRepository.ValueTypes._LONG,
                                DcRepository.ModuleSettings.stTreePanelShownItems,
                                DcModules._ITEM,
                                -1,
                                "",
                                "",
                                false,
                                false,
                                module.getIndex()));
            addSetting(_General,
                    new Setting(DcRepository.ValueTypes._BOOLEAN,
                                DcRepository.ModuleSettings.stContainerTreePanelFlat,
                                Boolean.FALSE,
                                -1,
                                "",
                                "",
                                false,
                                false,
                                module.getIndex()));            
        }
        
        int[] order;
        if (module.getIndex() == DcModules._CONTAINER)
            order = new int[] {Container._A_NAME};
        else if (module.getIndex() == DcModules._ITEM)
            order = new int[] {DcObject._SYS_MODULE, DcObject._SYS_DISPLAYVALUE};
        else if (module instanceof DcMediaModule)
            order = new int[] {DcMediaObject._A_TITLE, DcMediaObject._C_YEAR, DcMediaObject._E_RATING};
        else
            order = new int[] {DcObject._SYS_DISPLAYVALUE};
        
        int[] picFieldOrder;
        if (module.getIndex() == DcModules._SOFTWARE) {
            int[] fields = {Software._M_PICTUREFRONT, Software._O_PICTURECD, Software._N_PICTUREBACK,
                           Software._P_SCREENSHOTONE, Software._Q_SCREENSHOTTWO, Software._R_SCREENSHOTTHREE};
            picFieldOrder = fields;
        } else if (module.getIndex() == DcModules._MOVIE) {
            int[] fields = {Movie._X_PICTUREFRONT, Movie._Z_PICTURECD, Movie._Y_PICTUREBACK};
            picFieldOrder = fields;
        } else if (module.getIndex() == DcModules._MUSIC_ALBUM) {
            int[] fields = {MusicAlbum._J_PICTUREFRONT, MusicAlbum._L_PICTURECD, MusicAlbum._K_PICTUREBACK};
            picFieldOrder = fields;
        } else {
            Collection<DcField> pics = new ArrayList<DcField>();
            for (DcField field : module.getFields()) {
            	if (field.getValueType() == DcRepository.ValueTypes._PICTURE)
            		pics.add(field);
            }
            picFieldOrder = new int[pics.size()];
            int i = 0;
            for (DcField field : pics)
            	picFieldOrder[i++] = field.getIndex();
        }
        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stCardViewPictureOrder,
                            picFieldOrder,
                            -1,
                            "",
                            "",
                            false,
                            false,
                            module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stTableColumnOrder,
                            order,
                            -1,
                            "",
                            "",
                            false,
                            false,
                            module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stCardViewItemDescription,
                            new int[] {DcObject._SYS_DISPLAYVALUE},
                            -1,
                            "",
                            "",
                            false,
                            false,
                            module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stTitleCleanup,
                            "axxo,dvdrip,cdrip,dvd-rip,cd-rip",
                            -1,
                            "",
                            "",
                            false,
                            false,
                            module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stTitleCleanupRegex,
                            "",
                            -1,
                            "",
                            "",
                            false,
                            false,
                            module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stImportLocalArt,
                            module.getIndex() == DcModules._SOFTWARE ? Boolean.FALSE : Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false,
                            module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stFileImportUseOnlineService,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false,
                            module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.ModuleSettings.stFileImportDirectoryUsage,
                            Long.valueOf(0),
                            -1,
                            "",
                            "",
                            false,
                            false,
                            module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stFileImportOnlineService,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false,
                            module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stFileImportOnlineServiceMode,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false,
                            module.getIndex()));   
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stFileImportOnlineServiceRegion,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));           
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stImportLocalArtRecurse,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stImportLocalArtFrontKeywords,
                            "front,cover,case",
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stImportLocalArtBackKeywords,
                            "back",
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stImportLocalArtMediaKeywords,
                            "cd,dvd,media",
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stOnlineSearchFormSize,
                            new DcDimension(700, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stSearchOrder,
                            new int[] {},
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stItemFormSize,
                            new DcDimension(700, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stFilterDialogSize,
                            new DcDimension(700, 400),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));   
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stFieldSettingsDialogSize,
                            new DcDimension(500, 300),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stCardViewSettingsDialogSize,
                            new DcDimension(500, 400),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex())); 
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stTableViewSettingsDialogSize,
                            new DcDimension(500, 400),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stGroupedBy,
                            new int[] {},
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stQuickViewSettingsDialogSize,
                            new DcDimension(500, 300),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stLoanFormSize,
                            new DcDimension(500, 500),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
               new Setting(DcRepository.ValueTypes._DIMENSION,
                           DcRepository.ModuleSettings.stImportCDDialogSize,
                           new DcDimension(600, 900),
                           -1,
                           "",
                           "",
                           false,
                           false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stImportCDContainer,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stImportCDStorageMedium,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stFileImportDialogSize,
                            new DcDimension(550, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stSynchronizerDialogSize,
                            new DcDimension(550, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));  
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stFieldSettingsDialogSize,
                            new DcDimension(500, 300),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stOnlineSearchFormSize,
                            new DcDimension(700, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stSimpleItemViewSize,
                            new DcDimension(450, 550),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stSimpleItemFormSize,
                            new DcDimension(450, 550),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));  
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stOnlineSearchFieldSettingsDialogSize,
                            new DcDimension(600, 450),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.ModuleSettings.stQuickFilterDefaultField,
                            Long.valueOf(DcObject._ID),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));           
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stOnlineSearchDefaultServer,
                            null,
                            UIComponents._SHORTTEXTFIELD,
                            "",
                            "",
                            false,
                            false, module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stOnlineSearchDefaultRegion,
                            null,
                            UIComponents._SHORTTEXTFIELD,
                            "",
                            "",
                            false,
                            false, module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.ModuleSettings.stOnlineSearchDefaultMode,
                            null,
                            UIComponents._SHORTTEXTFIELD,
                            "",
                            "",
                            false,
                            false, module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stAutoAddPerfectMatch,
                            false,
                            UIComponents._CHECKBOX,
                            DcResources.getText("tpAutoAddPerfectMatch"),
                            DcResources.getText("lblAutoAddPerfectMatch"),
                            false,
                            false, module.getIndex())); 
        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stEnabled,
                            true,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex())); 
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.ModuleSettings.stOnlineSearchFieldSettingsDialogSize,
                            new DcDimension(600, 450),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.ModuleSettings.stOnlineSearchOverwrite,
                            false,
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stOnlineSearchFieldOverwriteSettings,
                            new int[] {},
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stOnlineSearchRetrievedFields,
                            module.getFieldIndices(),
                            -1,
                            "",
                            "",
                            false,
                            false, module.getIndex()));
        
        List<Integer> cWebFormFields = new ArrayList<Integer>();
        for (DcField field : module.getFields()) {
            if (!field.isLoanField())
                cWebFormFields.add(Integer.valueOf(field.getIndex()));
        }
        
        int[] webFormFields = new int[cWebFormFields.size()];
        int counter = 0;
        for (Integer i : cWebFormFields)
            webFormFields[counter++] = i.intValue();
        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stWebItemFormFields,
                            webFormFields,
                            -1,
                            "",
                            "",
                            false,
                            false,
                            module.getIndex()));
        
        List<Integer> cWebOverviewFields = new ArrayList<Integer>();
        if (module.getType() == DcModule._TYPE_MEDIA_MODULE) {
            cWebOverviewFields.add(Integer.valueOf(DcMediaObject._A_TITLE));
            cWebOverviewFields.add(Integer.valueOf(DcMediaObject._B_DESCRIPTION));
            cWebOverviewFields.add(Integer.valueOf(DcMediaObject._C_YEAR));            
            cWebOverviewFields.add(Integer.valueOf(DcMediaObject._E_RATING));
        } else if (module.getType() == DcModule._TYPE_PROPERTY_MODULE) {
            cWebOverviewFields.add(Integer.valueOf(DcProperty._A_NAME));           
        } else if (module.getType() == DcModule._TYPE_ASSOCIATE_MODULE) {
            cWebOverviewFields.add(Integer.valueOf(DcAssociate._A_NAME));
            cWebOverviewFields.add(Integer.valueOf(DcAssociate._B_DESCRIPTION));
        } else if (module.getIndex() == DcModules._CONTAINER) {
            cWebOverviewFields.add(Integer.valueOf(Container._A_NAME));
            cWebOverviewFields.add(Integer.valueOf(Container._B_TYPE));
            cWebOverviewFields.add(Integer.valueOf(Container._D_DESCRIPTION));              
        } else if (module.getIndex() == DcModules._CONTACTPERSON) {
            cWebOverviewFields.add(Integer.valueOf(ContactPerson._A_NAME));
            cWebOverviewFields.add(Integer.valueOf(ContactPerson._B_DESCRIPTION));
            cWebOverviewFields.add(Integer.valueOf(ContactPerson._D_CATEGORY));   
        } else {
            boolean longtext = false;
            for (DcField field : module.getFields()) {
                if (    field.isEnabled() && 
                        !field.isSystemField() && 
                        !field.isUiOnly() &&
                        field.getValueType() != DcRepository.ValueTypes._DCOBJECTREFERENCE &&
                        field.getValueType() != DcRepository.ValueTypes._DCOBJECTCOLLECTION &&
                        field.getValueType() != DcRepository.ValueTypes._PICTURE &&
                        field.getValueType() != DcRepository.ValueTypes._ICON &&
                        field.getIndex() != DcObject._SYS_DISPLAYVALUE &&
                        field.getIndex() != DcObject._SYS_MODULE &&
                        !field.isLoanField()) {
                    
                    if (field.getFieldType() == UIComponents._LONGTEXTFIELD && !longtext) {
                        longtext = true;
                        cWebFormFields.add(field.getIndex());
                    } else if (field.getFieldType() != UIComponents._LONGTEXTFIELD) {
                        cWebFormFields.add(field.getIndex());
                    }
                }
            }
        }
        
        int[] webOverviewFields = new int[cWebOverviewFields.size()];
        counter = 0;
        for (Integer i : cWebOverviewFields)
            webOverviewFields[counter++] = i.intValue();
        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._INTEGERARRAY,
                            DcRepository.ModuleSettings.stWebOverviewFields,
                            webOverviewFields,
                            -1,
                            "",
                            "",
                            false,
                            false,
                            module.getIndex()));
    }
    
    private void createSystemSettings(DcModule module) {
    	DcFieldDefinitions fldDefinitions = new DcFieldDefinitions(module.getIndex());
        
        for (DcField field : module.getFields()) {
            boolean enabled = field.isSystemField() ? false : true;
            
            if (module.getIndex() == DcModules._SOFTWARE && field.getIndex() == 7)
                enabled = false;
            
            fldDefinitions.add(new DcFieldDefinition(field.getModule(), field.getIndex(), null, enabled, false, false, false, null));
        }
    	
        Setting s = new Setting(DcRepository.ValueTypes._DEFINITIONGROUP,
                DcRepository.ModuleSettings.stFieldDefinitions,
                fldDefinitions,
                -1,
                "",
                "",
                false,
                false, module.getIndex());
        s.setReadonly(DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT);
        addSetting(_General, s);
    }
}

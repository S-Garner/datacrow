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

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.console.UIComponents;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.settings.objects.DcColor;
import org.datacrow.core.settings.objects.DcDimension;
import org.datacrow.core.settings.objects.DcFont;
import org.datacrow.core.settings.objects.DcLookAndFeel;
import org.datacrow.core.synchronizers.Synchronizer;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.definitions.ProgramDefinitions;

/**
 * Holder for application settings.
 * 
 * @see DcSettings
 * @see DcRepository.Settings
 * 
 * @author Robert Jan van der Waals
 */
public class DcApplicationSettings extends Settings {
    
	private static final long serialVersionUID = 1L;

	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcApplicationSettings.class.getName());
    
    public static final String _General = "lblGroupGeneral";
    public static final String _SizeLimits = "lblSizeLimits";
    public static final String _FileHashing = "lblFileHashing";
    public static final String _Regional = "lblGroupRegional";
    public static final String _Module = "lblModuleSettings";
    public static final String _DriveMappings = "lblDriveMappings";
    public static final String _DirectoriesAsDrives = "lblDirectoriesAsDrives";
    public static final String _FileHandlers = "lblProgramDefinitions";
    public static final String _SelectionColor = "lblSelectionColor";
    public static final String _HTTP = "lblHTTPSettings";
    public static final String _Font = "lblFont";

    /**
     * Initializes and loads all settings.
     */
    public DcApplicationSettings() {
        super();
        createSettings();
        createSystemSettings();

        File fileClient = new File(DcConfig.getInstance().getApplicationSettingsDir(), "data_crow.properties");
        File fileDefault = new File(DcConfig.getInstance().getApplicationSettingsDir(), "data_crow.properties");
        
        try {
            if (fileDefault.exists() && !fileClient.exists())
            	CoreUtilities.copy(fileDefault, fileClient, true);
        } catch (Exception e) {
            logger.warn("Could not use the default settings as template for " + 
                    "_data_crow.properties. Failed to copy " + fileDefault, e);
        }
        
        logger.debug("Using settings file: " + fileClient);
        setSettingsFile(fileClient);

        if (DcConfig.getInstance().isAllowLoadSettings())
            load();
    }

    @Override
    protected void createGroups() {
        SettingsGroup generalGroup = new SettingsGroup(_General, "dc.settings.general");
        SettingsGroup regionalGroup = new SettingsGroup(_Regional, "dc.settings.regional");
        SettingsGroup sizeLimits = new SettingsGroup(_SizeLimits, "dc.settings.sizelimits");
        SettingsGroup colorGroup = new SettingsGroup(_SelectionColor, "dc.settings.colors");
        SettingsGroup http = new SettingsGroup(_HTTP, "dc.settings.http");
        SettingsGroup fileHandlers = new SettingsGroup(_FileHandlers, "dc.settings.fileassociations");
        SettingsGroup moduleGroup = new SettingsGroup(_Module, "dc.settings.module");
        SettingsGroup fontGroup = new SettingsGroup(_Font, "dc.settings.font");
        SettingsGroup fileHashingGroup = new SettingsGroup(_FileHashing, "dc.settings.filehash");
        SettingsGroup driveMappings = new SettingsGroup(_DriveMappings, "dc.settings.drivemappings");
        SettingsGroup directoriesAsDrives = new SettingsGroup(_DirectoriesAsDrives, "dc.settings.directoriesasdrives");

        addGroup(_Module, moduleGroup);
        addGroup(_Regional, regionalGroup);
        addGroup(_General, generalGroup);
        addGroup(_SizeLimits, sizeLimits);
        addGroup(_Font, fontGroup);
        addGroup(_HTTP, http);
        addGroup(_SelectionColor, colorGroup);
        addGroup(_FileHandlers, fileHandlers);
        addGroup(_DriveMappings, driveMappings);
        addGroup(_DirectoriesAsDrives, directoriesAsDrives);
        addGroup(_FileHashing, fileHashingGroup);
    }

    protected void createSettings() {
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stIsUpgraded,
                            Boolean.FALSE,
                            -1,
                            "",
                            "",
                            true,
                            false, -1));
        addSetting(_SizeLimits,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stMaximumAttachmentFileSize,
                            Long.valueOf(5000),
                            UIComponents._FILESIZEFIELD,
                            "",
                            "lblMaximumAttachFileSize",
                            true,
                            true, -1));
        addSetting(_SizeLimits,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stMaximumImageResolutionChosen,
                            Boolean.FALSE,
                            -1,
                            "",
                            "",
                            true,
                            false, -1));          
        addSetting(_SizeLimits,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stMaximumImageResolution,
                            new DcDimension(1920, 1080),
                            UIComponents._RESOLUTIONCOMBO,
                            "tpMaximumImageResolution",
                            "lblMaximumImageResolution",
                            true,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stDrivePollerRunOnStartup,
                            Boolean.FALSE,
                            UIComponents._CHECKBOX,
                            "",
                            "lblRunOnStartup",
                            false,
                            false, 
                            -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stICalendarFullExport,
                            Boolean.FALSE,
                            UIComponents._CHECKBOX,
                            "",
                            "",
                            false,
                            false, 
                            -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stServerAddress,
                            "",
                            UIComponents._SHORTTEXTFIELD,
                            "",
                            "",
                            false,
                            false, 
                            -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stApplicationServerPort,
                            Long.valueOf(9000),
                            UIComponents._NUMBERFIELD,
                            "",
                            "",
                            false,
                            false, 
                            -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stImageServerPort,
                            Long.valueOf(9001),
                            UIComponents._NUMBERFIELD,
                            "",
                            "",
                            false,
                            false, 
                            -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stOpenItemsInEditModus,
                            Boolean.TRUE,
                            UIComponents._CHECKBOX,
                            "",
                            "lblOpenItemsDefaultModus",
                            false,
                            true, 
                            -1));  
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stDoNotAskAgainChangeUserDir,
                            Boolean.FALSE,
                            UIComponents._CHECKBOX,
                            "",
                            "",
                            false,
                            false, 
                            -1));         
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stUsage,
                            Long.valueOf(0),
                            UIComponents._NUMBERFIELD,
                            "",
                            "",
                            false,
                            false, 
                            -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stAskForDonation,
                            Boolean.TRUE,
                            UIComponents._CHECKBOX,
                            "",
                            "",
                            false,
                            false, 
                            -1));     
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stShowToolSelectorOnStartup,
                            Boolean.TRUE,
                            UIComponents._CHECKBOX,
                            "",
                            "",
                            false,
                            false, 
                            -1));   
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stDriveScannerRunOnStartup,
                            Boolean.FALSE,
                            UIComponents._CHECKBOX,
                            "",
                            "lblRunOnStartup",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stMobyGamesApiKey,
                            "",
                            UIComponents._SHORTTEXTFIELD,
                            "lblMobyGamesApiKeyHelp",
                            "lblMobyGamesApiKey",
                            true,
                            false, -1));
        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stComicVineApiKey,
                            "",
                            UIComponents._SHORTTEXTFIELD,
                            "lblComicVineApiKeyHelp",
                            "lblComicVineApiKey",
                            true,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stComicVineAddEnemiesAndFriends,
                            Boolean.FALSE,
                            UIComponents._CHECKBOX,
                            "lblComicVineQueryEnemiesAndFriendsHelp",
                            "lblComicVineQueryEnemiesAndFriends",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stHighRenderingQuality,
                            Boolean.TRUE,
                            UIComponents._CHECKBOX,
                            "",
                            "lblHighRenderingQuality",
                            false,
                            true, -1));   
        addSetting(_Regional,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stLanguage,
                            "",
                            UIComponents._LANGUAGECOMBO,
                            "",
                            "lblLanguage",
                            true,
                            true, -1));
        addSetting(_Regional,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stDatabaseLanguage,
                            "Latin1_General",
                            UIComponents._COLLATIONCOMBO,
                            "tpDatabaseLanguage",
                            "lblDatabaseLanguage",
                            true,
                            true, -1));
        addSetting(_Regional,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stDateFormat,
                            "EEEEE, d MMMMM yyyy",
                            UIComponents._DATEFOMATCOMBO,
                            "",
                            "lblDateFormat",
                            true,
                            true, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stGracefulShutdown,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));          
        addSetting(_DriveMappings,
                new Setting(DcRepository.ValueTypes._STRINGARRAY,
                            DcRepository.Settings.stDriveMappings,
                            null,
                            UIComponents._DRIVEMAPPING,
                            "",
                            "lblDriveMappings",
                            false,
                            true, -1));          
        addSetting(_DirectoriesAsDrives,
                new Setting(DcRepository.ValueTypes._STRINGARRAY,
                            DcRepository.Settings.stDirectoriesAsDrives,
                            null,
                            UIComponents._DIRECTORIESASDRIVES,
                            "",
                            "lblDirectoriesAsDrive",
                            false,
                            true, -1));          
        addSetting(_Module,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stModuleSettings,
                            "",
                            UIComponents._MODULESELECTOR,
                            "",
                            "",
                            false,
                            true, -1));          
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stBackupLocation,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stOnlineSearchSelectedView,
                            Long.valueOf(0),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stTreeNodeHeight,
                            Long.valueOf(20),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));         
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stTableRowHeight,
                            Long.valueOf(25),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));            
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stImportCharacterSet,
                            "UTF-8",
                            UIComponents._CHARACTERSETCOMBO,
                            "",
                            "lblCharacterSet",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stImportMatchAndMerge,
                            Boolean.TRUE,
                            UIComponents._CHECKBOX,
                            "",
                            "lblMatchAndMerge",
                            false,
                            false, -1)); 
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stButtonHeight,
                            Long.valueOf(25),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));          
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stInputFieldHeight,
                            Long.valueOf(25),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));             
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stButtonHeight,
                            Long.valueOf(25),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));         
        addSetting(_FileHashing,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stHashType,
                            "md5",
                            UIComponents._HASHTYPECOMBO,
                            "",
                            "lblFileHashType",
                            true,
                            true, -1));
        addSetting(_FileHashing,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stHashMaxFileSizeKb,
                            Long.valueOf(500000),
                            UIComponents._FILESIZEFIELD,
                            "",
                            "lblFileHashMaxFileSize",
                            true,
                            true, -1));             
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stWebServerPort,
                            Long.valueOf(8080),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));  
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stImportSeperator,
                            ",",
                            UIComponents._SHORTTEXTFIELD,
                            "",
                            "lblValueSeperator",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stShowTipsOnStartup,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BIGINTEGER,
                            DcRepository.Settings.stXpMode,
                            -1,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stShowGroupingPanel,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));   
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stShowToolbar,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));           
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stShowQuickFilterBar,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));          
        addSetting(_SelectionColor,
                new Setting(DcRepository.ValueTypes._COLOR,
                            DcRepository.Settings.stSelectionColor,
                            new DcColor(255, 255, 153),
                            UIComponents._COLORSELECTOR,
                            "",
                            "",
                            false,
                            true, -1));
        addSetting(_SelectionColor,
                new Setting(DcRepository.ValueTypes._COLOR,
                            DcRepository.Settings.stQuickViewBackgroundColor,
                            new DcColor(255, 255, 255),
                            UIComponents._COLORSELECTOR,
                            "",
                            "",
                            false,
                            false, -1));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._COLOR,
                            DcRepository.Settings.stCardViewBackgroundColor,
                            new DcColor(255, 255, 255),
                            UIComponents._COLORSELECTOR,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._COLOR,
                            DcRepository.Settings.stOddRowColor,
                            new DcColor(227, 226, 226),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._COLOR,
                            DcRepository.Settings.stEvenRowColor,
                            new DcColor(236, 236, 237),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._COLOR,
                            DcRepository.Settings.stTableHeaderColor,
                            new DcColor(204, 204, 204),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        
 
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stShowTableTooltip,
                            true,
                            UIComponents._CHECKBOX,
                            "tpShowTableTooltips",
                            "lblShowTableTooltips",
                            false,
                            true, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stModule,
                            DcModules._SOFTWARE,
                            -1,
                            "",
                            "",
                            true,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stMassUpdateItemPickMode,
                            Synchronizer._ALL,
                            -1,
                            "",
                            "",
                            true,
                            false, -1));
        addSetting(_Font,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stFontRendering,
                            Long.valueOf(0),
                            UIComponents._FONTRENDERINGCOMBO,
                            "",
                            "lblFontRendering",
                            true,
                            true, -1)); 
        addSetting(_Font,
                new Setting(DcRepository.ValueTypes._FONT,
                            DcRepository.Settings.stStandardFont,
                            new DcFont("Arial", DcFont.PLAIN, 12),
                            UIComponents._FONTSELECTOR,
                            "tpFont",
                            "lblFontNormal",
                            false,
                            true, -1));
        addSetting(_Font,
                new Setting(DcRepository.ValueTypes._FONT,
                            DcRepository.Settings.stSystemFont,
                            new DcFont("Arial", DcFont.PLAIN, 12),
                            UIComponents._FONTSELECTOR,
                            "tpFont",
                            "lblFontBold",
                            false,
                            true, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LOOKANDFEEL,
                            DcRepository.Settings.stLookAndFeel,
                            new DcLookAndFeel("FlatLaf Light", "com.formdev.flatlaf.FlatLightLaf", null, 1),
                            UIComponents._LOOKANDFEELSELECTOR,
                            "",
                            "lblLookAndFeel",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stUIScaling,
                            100,
                            UIComponents._UISCALECOMBO,
                            "",
                            "lblUIScaling",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stIconSize,
                            Long.valueOf(16),
                            UIComponents._ICONSIZECOMBO,
                            "",
                            "lblLookAndFeel",
                            false,
                            false, -1));
        addSetting(_HTTP,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stProxyServerName,
                            "",
                            UIComponents._SHORTTEXTFIELD,
                            "tpProxyServerName",
                            "lblProxyServerName",
                            true,
                            true, -1));
        addSetting(_HTTP,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stProxyServerPort,
                            0,
                            UIComponents._NUMBERFIELD,
                            "tpProxyServerPort",
                            "lblProxyServerPort",
                            true,
                            true, -1));
        addSetting(_HTTP,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stProxyUserName,
                            "",
                            UIComponents._SHORTTEXTFIELD,
                            "tpProxyUserName",
                            "lblProxyUserName",
                            true,
                            true, -1));
        addSetting(_HTTP,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stProxyPassword,
                            "",
                            UIComponents._PASSWORDFIELD,
                            "tpProxyPassword",
                            "lblProxyPassword",
                            true,
                            true, -1));
        addSetting(_HTTP,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stBrowserPath,
                            "",
                            UIComponents._FILEFIELD,
                            "",
                            "lblBrowserPath",
                            true,
                            true, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stMergeItemsDialogSize,
                            new DcDimension(500, 400),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stSynchServiceDialogSize,
                            new DcDimension(500, 400),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stSynchServicePort,
                            9000,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stSynchServiceName,
                            "My Android Synch Service",
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stNewItemsDialogSize,
                            new DcDimension(700, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stMaintainTabsDialogSize,
                            new DcDimension(400, 500),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stChartsDialogSize,
                            new DcDimension(700, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stExpertFormSize,
                            new DcDimension(700, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stLoanAdminFormSize,
                            new DcDimension(600, 500),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stServerSettingsDialogSize,
                            new DcDimension(400, 300),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stDirectoriesAsDrivesDialogSize,
                            new DcDimension(400, 300),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stItemFormSettingsDialogSize,
                            new DcDimension(700, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stLogFormSize,
                            new DcDimension(590, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stDriveManagerDialogSize,
                            new DcDimension(573, 548),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stHelpFormSize,
                            new DcDimension(700, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stReferencesDialogSize,
                            new DcDimension(300, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stSelectItemDialogSize,
                            new DcDimension(300, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stWebServerFrameSize,
                            new DcDimension(300, 300),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));    
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stItemWizardFormSize,
                            new DcDimension(800, 550),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stToolSelectWizard,
                            new DcDimension(500, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));  
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stModuleExportWizardFormSize,
                            new DcDimension(800, 550),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));  
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stModuleImportWizardFormSize,
                            new DcDimension(800, 550),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stItemExporterWizardFormSize,
                            new DcDimension(800, 550),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stItemImporterWizardFormSize,
                            new DcDimension(800, 550),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stModuleWizardFormSize,
                            new DcDimension(800, 550),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stUpdateAllDialogSize,
                            new DcDimension(600, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stFindReplaceDialogSize,
                            new DcDimension(400, 200),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stFindReplaceTaskDialogSize,
                            new DcDimension(600, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stMainViewSize,
                            new DcDimension(1024, 733),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stMainViewState,
                            0,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stMainViewLocation,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stTextViewerSize,
                            new DcDimension(500, 700),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));

        addSetting(_General,
                    new Setting(DcRepository.ValueTypes._DIMENSION,
                             DcRepository.Settings.stReportingDialogSize,
                             new DcDimension(700, 490),
                             -1,
                             "",
                             "",
                             false,
                             false, -1));
        addSetting(_General,
                    new Setting(DcRepository.ValueTypes._DIMENSION,
                             DcRepository.Settings.stModuleSelectDialogSize,
                             new DcDimension(600, 400),
                             -1,
                             "",
                             "",
                             false,
                             false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stShowModuleList,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stBroadband,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stLastDirectoryUsed,
                            "",
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stShowMenuBarLabels,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stUpdateAllSelectedItemsOnly,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stShowQuickView,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stCheckForNewVersion,
                            Boolean.TRUE,
                            UIComponents._CHECKBOX,
                            "",
                            "msgVersionCheckOnStartup",
                            false,
                            true, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stAutoUpdateOnlineServices,
                            Boolean.FALSE,
                            UIComponents._CHECKBOX,
                            "",
                            "lblAutoUpdateOnlineServices",
                            false,
                            true, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stAutoUpdateOnlineServicesAsked,
                            Boolean.FALSE,
                            UIComponents._CHECKBOX,
                            "",
                            "",
                            false,
                            false, -1));  
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stQuickViewDividerLocation,
                            681,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stQuickViewDividerLocationOnlineSearchTable,
                            300,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stQuickViewDividerLocationOnlineSearchCard,
                            300,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._LONG,
                            DcRepository.Settings.stTreeDividerLocation,
                            272,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));        
        addSetting(_FileHandlers,
                new Setting(DcRepository.ValueTypes._DEFINITIONGROUP,
                            DcRepository.Settings.stProgramDefinitions,
                            new ProgramDefinitions(),
                            UIComponents._PROGRAMDEFINITIONFIELD,
                            "tpProgramDefinitions",
                            "",
                            false,
                            true, -1));

        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stResourcesEditorViewSize,
                            new DcDimension(542, 680),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stBackupDialogSize,
                            new DcDimension(500, 500),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stICalendarExportDialogSize,
                            new DcDimension(500, 500),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stFileRenamerDialogSize,
                            new DcDimension(300, 300),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));

        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stFileRenamerPreviewDialogSize,
                            new DcDimension(400, 600),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stSortDialogSize,
                            new DcDimension(300, 200),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._DIMENSION,
                            DcRepository.Settings.stGroupByDialogSize,
                            new DcDimension(300, 200),
                            -1,
                            "",
                            "",
                            false,
                            false, -1));
        addSetting(_Regional,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stDecimalGroupingSymbol,
                            ".",
                            UIComponents._CHARACTERFIELD,
                            "lblDecimalGroupingSymbol",
                            "lblDecimalGroupingSymbol",
                            true,
                            true, -1));
        addSetting(_Regional,
                new Setting(DcRepository.ValueTypes._STRING,
                            DcRepository.Settings.stDecimalSeparatorSymbol,
                            ",",
                            UIComponents._CHARACTERFIELD,
                            "lblDecimalSeperatorSymbol",
                            "lblDecimalSeperatorSymbol",
                            true,
                            true, -1));
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stRestoreDatabase,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));          
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stRestoreModules,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));          
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._BOOLEAN,
                            DcRepository.Settings.stRestoreReports,
                            Boolean.TRUE,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));          
        
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRINGARRAY,
                            DcRepository.Settings.stDriveManagerDrives,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));          
        addSetting(_General,
                new Setting(DcRepository.ValueTypes._STRINGARRAY,
                            DcRepository.Settings.stDriveManagerExcludedDirs,
                            null,
                            -1,
                            "",
                            "",
                            false,
                            false, -1));          
    }
    
    private void createSystemSettings() {
        Setting s = new Setting(DcRepository.ValueTypes._BOOLEAN,
                DcRepository.Settings.stCheckRequiredFields,
                true,
                UIComponents._CHECKBOX,
                "tpRequiredFields",
                "lblCheckRequiredFields",
                false,
                true, -1);
        s.setReadonly(DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT);
        addSetting(_General, s);
        
        s = new Setting(DcRepository.ValueTypes._BOOLEAN,
                DcRepository.Settings.stCheckUniqueness,
                true,
                UIComponents._CHECKBOX,
                "tpUniqueness",
                "lblUniqueness",
                false,
                true, -1);
        s.setReadonly(DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT);
        addSetting(_General, s);
        
        s = new Setting(DcRepository.ValueTypes._STRING,
                DcRepository.Settings.stConnectionString,
                "dc",
                -1,
                "",
                "",
                false,
                false, -1);
        s.setReadonly(DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT);
        addSetting(_General, s);
        
        s = new Setting(DcRepository.ValueTypes._STRING,
                DcRepository.Settings.stDatabaseDriver,
                "org.hsqldb.jdbcDriver",
                -1,
                "",
                "",
                false,
                false, -1);
        s.setReadonly(DcConfig.getInstance().getOperatingMode() == DcConfig._OPERATING_MODE_CLIENT);
        addSetting(_General, s);
    }
}

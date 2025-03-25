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

package org.datacrow.core.fileimporter;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.clients.IFileImportClient;
import org.datacrow.core.clients.ISynchronizerClient;
import org.datacrow.core.data.DataFilter;
import org.datacrow.core.data.DataFilterEntry;
import org.datacrow.core.data.Operator;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcMediaObject;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.ValidationException;
import org.datacrow.core.pictures.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.server.Connector;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.settings.Settings;
import org.datacrow.core.synchronizers.Synchronizer;
import org.datacrow.core.synchronizers.Synchronizers;
import org.datacrow.core.utilities.CoreUtilities;
import org.datacrow.core.utilities.Directory;
import org.datacrow.core.utilities.StringUtils;

/**
 * Base for all file importers. A file importer is capable of scanning a specific
 * location for specific file types. These files are parsed and their information
 * is stored in a {@link DcObject}.
 * 
 * @author Robert Jan van der Waals
 */
public abstract class FileImporter {

    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(FileImporter.class.getName());
    
    private final int module;
    private IFileImportClient client;
    
    /**
     * Creates a new instance.
     * @param module The module to which this importer belongs.
     */
    public FileImporter(int module) {
        this.module = module;
    }
    
    public abstract FileImporter getInstance();
    
    public void setClient(IFileImportClient client) {
        this.client = client;
    }
    
    public IFileImportClient getClient() {
        return client;
    }
    
    /**
     * The module to which this importer belongs.
     */
	public int getModuleIdx() {
        return module;
    }
    
    /**
     * Parses a file and extracts its information.
     * @param filename The file to check.
     * @param directoryUsage A free interpretation of the directory usage. 
     * Depends on a specific implementation.
     * @throws ParseException
     */
    public abstract DcObject parse(String filename, int directoryUsage);
    
    public abstract String[] getSupportedFileTypes();
    
    public abstract String getFileTypeDescription();

    /**
     * Indicates if a directory can be used instead of a file.
     * @return false
     */
    public boolean allowDirectoryRegistration() {
        return false;
    }
    
    /**
     * Indicates if files can be parsed again. This useful when you know that the information
     * of the file can be changed (such as the ID tag content of MP3 files).
     * @return false
     */
    public boolean allowReparsing() {
        return false;
    }
    
    /**
     * Indicates if local art can be used.
     * @return false
     */
    public boolean canImportArt() {
        return false;
    }
    
    /**
     * To be executed before a file is parsed.
     */
    public void beforeParse() {}
    
    /**
     * Starts the parsing task.
     * @param sources The files to check.
     * @throws Exception
     */
    public void parse(final Collection<String> sources) throws Exception {
        
        beforeParse();
        
        if (sources == null || sources.size() == 0)
            throw new Exception(DcResources.getText("msgSelectFiles"));
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                IFileImportClient client = getClient();
                
                if (client != null) {
                    client.notify(DcResources.getText("msgImportStarts"));
                    client.notify(DcResources.getText("msgParsingXFiles", String.valueOf(sources.size())));

                    client.notifyTaskStarted(sources.size());
                }
                
                try {
                    int counter = 1;
                    for (String filename : sources) {
                        if  (client != null && client.isCancelled()) break;
                        
                        try {
                            parse(filename, counter == sources.size());
                        } catch (ValidationException ve) {
                        	if (client != null) client.notifyWarning(ve.getMessage());
                        	
                        	logger.warn(ve, ve);
                        } catch (Throwable e) {
                            if (client != null) client.notifyError(e);
                            
                            logger.error("An unhandled error occured during the import of " + filename, e);
                        }
                        
                        if (client != null) 
                        	client.notifyProcessed();
                    }
                    if (client != null) client.notify(DcResources.getText("msgImportStops"));
                } finally {
                	try {
                		afterImport();
                    } catch (ValidationException ve) {
                    	if (client != null) client.notifyWarning(ve.getMessage());
                    }
                    
                    if (client != null) client.notifyTaskCompleted(true, null);
                }
            }
        });
        
        thread.start();
    }
    
    /**
     * Parses a single file. The process skips already imported files.
     * @param listener
     * @param filename
     */
    protected void parse(String filename, boolean last) throws ValidationException {
        int module = getClient().getModuleIdx();
        
        module = DcModules._MUSIC_TRACK;
        
        DataFilter df = new DataFilter(getClient().getModuleIdx());
        df.addEntry(new DataFilterEntry(module, DcObject._SYS_FILENAME, Operator.EQUAL_TO, filename));
        Map<String, Integer> items = DcConfig.getInstance().getConnector().getKeys(df); 
        
        if (items.size() > 0) {
            getClient().notify(DcResources.getText("msgSkippingAlreadyImportedFile", 
                                new String[] {filename, ""}));
            
        } else {
            getClient().notify(DcResources.getText("msgProcessingFileX", filename));
            DcObject dco = parse(filename, getClient().getDirectoryUsage());
                
            if (getClient().getStorageMedium() != null) { 
                for (DcField  field : dco.getFields()) {
                    if (field.getSourceModuleIdx() == DcModules._STORAGEMEDIA)
                    	dco.createReference(field.getIndex(), getClient().getStorageMedium());
                }
            }
    
            DcObject container = client != null ? client.getContainer() : null;
            if (container != null && dco.getModule().isContainerManaged())
            	dco.createReference(DcObject._SYS_CONTAINER, container);
            
            dco.setLastInLine(last);
            dco.applyTemplate();
            dco.setIDs();
            afterParse(dco);
        }
    }
    
    /**
     * Called after finishing the whole parsing process.
     */
    protected void afterImport() throws ValidationException {}
    
    /**
     * Called after parsing a single file.
     * @param listener
     * @param dco
     */
    protected void afterParse(DcObject dco) throws ValidationException {
        if (client != null && client.useOnlineServices()) {        	
            try {
                getClient().notify(DcResources.getText("msgSearchingOnlineFor", StringUtils.normalize(dco.toString())));
                
                String originalTitle = (String) dco.getValue(DcMediaObject._A_TITLE);
                dco.setValue(DcMediaObject._A_TITLE, StringUtils.normalize(originalTitle));
                
                int moduleIdx = getClient().getModuleIdx();
                Synchronizers synchronizers = Synchronizers.getInstance();
                if (synchronizers.hasSynchronizer(moduleIdx)) {
                	Synchronizer synchronizer = synchronizers.getSynchronizer(moduleIdx);
                	synchronizer.onlineUpdate(new SynchronizerClient(), dco);
                }
                
                dco.setValue(DcMediaObject._A_TITLE, originalTitle);
            } catch (Exception e) {
                logger.error(e, e);
            }
        }

        dco.setIDs();
        
    	Connector connector = DcConfig.getInstance().getConnector();
        dco.setUpdateGUI(false);
        connector.saveItem(dco);
    }

    /**
     * Tries to create a name from the specified file.
     * @param file
     * @param directoryUsage Either 1 (to use directory information) or 0.
     * @return The name.
     */
    protected String getName(String file, int directoryUsage) {
        String name = "";
        if (directoryUsage == 1) {
            File f = new File(file);
            String path = f.isDirectory() ? f.toString() : f.getParent();
            name = path.substring(path.lastIndexOf(File.separator) + 1);
        } else {
            name = new File(file).getName();
            int index = name.lastIndexOf(".");
            if (index > 0 && index > name.length() - 5)
                name = name.substring(0, index);
            
            String regex = DcModules.get(getModuleIdx()).getSettings().getString(DcRepository.ModuleSettings.stTitleCleanupRegex);
            if (!CoreUtilities.isEmpty(regex)) {
                Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(name);
                while (matcher.find())
                    name = matcher.replaceAll("");
            }
            
            name = name.replaceAll("\\.", " ");
            
            String remove = 
                DcModules.get(getModuleIdx()).getSettings().getString(DcRepository.ModuleSettings.stTitleCleanup);
            
            if (!CoreUtilities.isEmpty(remove)) {
                StringTokenizer st = new StringTokenizer(remove, ",");
                while (st.hasMoreElements()) {
                    String s = (String) st.nextElement();
                    int idx = name.toLowerCase().indexOf(s.toLowerCase());
                    if (idx > -1)
                        name = name.substring(0, idx) + name.substring(idx + s.length());
                }
            }
        }
        
        return StringUtils.capitalize(name.trim());
    }    
    
    private boolean match(String settingKey, String filename) {
        Settings settings = DcModules.get(module).getSettings();
        String keywords = settings.getString(settingKey);
        StringTokenizer st = new StringTokenizer(keywords, ",");
        while (st.hasMoreElements()) {
            if (filename.toLowerCase().indexOf((String) st.nextElement()) != -1)
                return true;
        }
        return false;
    }
    
    /**
     * Retrieve and use local art.
     * 
     * @see DcRepository.ModuleSettings#stImportLocalArt
     * @see DcRepository.ModuleSettings#stImportLocalArtRecurse
     * @see DcRepository.ModuleSettings#stImportLocalArtFrontKeywords
     * 
     * @param sourceFile The file location for which art will be retrieved.
     * @param dco
     */
    protected void setImages(String sourceFile, DcObject dco) {
        Settings settings = DcModules.get(module).getSettings();
        if (!settings.getBoolean(DcRepository.ModuleSettings.stImportLocalArt)) 
            return;
        
        String directory = new File(sourceFile).getParent();
        boolean recurse = settings.getBoolean(DcRepository.ModuleSettings.stImportLocalArtRecurse);
        
        Directory dir = new Directory(directory, recurse, ImageIO.getReaderFileSuffixes());
        Collection<String> files = dir.read();
        
        LinkedList<Picture> pictures = new LinkedList<Picture>();
        Picture picture;
        for (String file : files) {
            try {
                String name1 = new File(sourceFile).getName();
                String name2 = new File(file).getName();
                
                name1 = name1.substring(0, name1.lastIndexOf(".") > 0 ? name1.lastIndexOf(".") : name1.length());
                name2 = name2.substring(0, name2.lastIndexOf(".") > 0 ? name2.lastIndexOf(".") : name2.length());
                
                if (	StringUtils.equals(name1, name2) ||
                		client.getDirectoryUsage() > 0) {
                	
                	picture = new Picture(dco.getID(), file);
                	
                	if (match(DcRepository.ModuleSettings.stImportLocalArtKeywords, file)) {
                		pictures.addFirst(picture);
                        break;
                	} else {
                		pictures.addLast(picture);
                	}
                }
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
        
        dco.addNewPictures(pictures);
    }
    
    private class SynchronizerClient implements ISynchronizerClient {
        
        @Override
        public boolean useOnlineServices() {
            return client.useOnlineServices();
        }

        @Override
        public SearchMode getSearchMode() {
            return null; // the default will be used
        }

        @Override
        public IServer getServer() {
            return client.getServer();
        }

        @Override
        public Region getRegion() {
            return client.getRegion();
        }

        @Override
        public DcObject getContainer() {
            return client.getContainer();
        }

        @Override
        public DcObject getStorageMedium() {
            return client.getStorageMedium();
        }

        @Override
        public int getDirectoryUsage() {
            return client.getDirectoryUsage();
        }

        @Override
        public int getModuleIdx() {
            return module;
        }

        @Override
        public void notify(String msg) {
            client.notify(msg);
        }

        @Override
        public void notifyWarning(String msg) {
            client.notifyWarning(msg);
        }

        @Override
        public void notifyError(Throwable e) {
            client.notifyError(e);
        }
        
        @Override
        public boolean askQuestion(String msg) {
            return false;
        }

        @Override
        public void notifyTaskCompleted(boolean success, String taskID) {
            client.notifyTaskCompleted(success, taskID);
        }

        @Override
        public void notifyTaskStarted(int taskSize) {
            client.notifyTaskStarted(taskSize);
        }

        @Override
        public void notifyProcessed() {}

        @Override
        public void notifyProcessed(DcObject dco) {}

        @Override
        public boolean isCancelled() {
            return client.isCancelled();
        }

        @Override
        public boolean isReparseFiles() {
            return false;
        }

        @Override
        public int getItemPickMode() {
            return 0;
        }

        @Override
        public List<String> getItemKeys() {
            return null;
        }
    }
}
/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                       (c) 2003 The Data Crow team                          *
 *                              info@datacrow.org                             *
 *                                                                            *
 *                                                                            *
 *       This library is free software; you can redistribute it and/or        *
 *        modify it under the terms of the GNU Lesser General Public          *
 *       License as published by the Free Software Foundation; either         *
 *     version 2.1 of the License, or (at your option) any later version.     *
 *                                                                            *
 *      This library is distributed in the hope that it will be useful,       *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU       *
 *           Lesser General Public License for more details.                  *
 *                                                                            *
 *     You should have received a copy of the GNU Lesser General Public       *
 *    License along with this library; if not, write to the Free Software     *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA   *
 *                                                                            *
 ******************************************************************************/

package org.datacrow.core.utilities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.IOUtils;
import org.datacrow.core.DcConfig;
import org.datacrow.core.DcRepository;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.modules.DcModule;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcAssociate;
import org.datacrow.core.objects.DcField;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcMapping;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Permission;
import org.datacrow.core.settings.DcSettings;
import org.datacrow.core.settings.objects.DcDimension;
import org.datacrow.core.utilities.comparators.DcObjectComparator;

public class CoreUtilities {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(CoreUtilities.class.getName());
    
    private static final FileSystemView fsv = new JFileChooser().getFileSystemView();
    private static final Properties languages = new Properties();
    
    private static final Pattern[] normalizer = {
        Pattern.compile("('|~|\\!|@|#|\\$|%|\\^|\\*|_|\\[|\\{|\\]|\\}|\\||\\\\|;|:|`|\"|<|,|>|\\.|\\?|/|&|_|-)"),
        Pattern.compile("[(,)]")};
    
    
    public static byte[] zip(byte[] bytes) {
        Deflater deflater = new Deflater(9);
        deflater.setLevel(Deflater.BEST_COMPRESSION);
        deflater.setInput(bytes);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8096];

        while (!deflater.finished()) {
            int compressedSize = deflater.deflate(buffer);
            outputStream.write(buffer, 0, compressedSize);
        }

        byte[] data = outputStream.toByteArray();
        
        try {
        	outputStream.close();
        } catch (Exception e) {
        	logger.error("Error closing output stream", e);
        }
        
        return data;
    }

    public static byte[] unzip(byte[] bytes) throws DataFormatException {
    	Inflater inflater = new Inflater();
        inflater.setInput(bytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (!inflater.finished()) {
            int decompressedSize = inflater.inflate(buffer);
            outputStream.write(buffer, 0, decompressedSize);
        }

        byte[] data = outputStream.toByteArray();
        
        try {
        	outputStream.close();
        } catch (Exception e) {
        	logger.error("Error closing output stream", e);
        }
        
        return data;
    }
    
    public static List<DcObject> sort(List<DcObject> items) {
    	boolean mappings = false;
    	
    	if (items == null || items.size() <= 1)
    		return items;
    	
    	List<DcObject> result = new ArrayList<DcObject>(); 
    	List<DcObject> references = new ArrayList<DcObject>();
        DcObject ref;
        for (DcObject reference : items) {
            if (reference.getModule().getType() == DcModule._TYPE_MAPPING_MODULE) {
                ref = ((DcMapping) reference).getReferencedObject();
                
                if (ref != null) {
                    if (ref.getModule().getType() == DcModule._TYPE_ASSOCIATE_MODULE)
                    	((DcAssociate ) ref).setName();
                }
                
                if (ref != null) references.add(ref);
                
                mappings = true;
            } else {
                if (reference.getModule().getType() == DcModule._TYPE_ASSOCIATE_MODULE)
                	((DcAssociate ) reference).setName();
            	
            	references.add(reference);
            }
        }
        
        int sortIdx = references.size() > 0 ? references.get(0).getDefaultSortFieldIdx() : 0;
        Collections.sort(references, new DcObjectComparator(sortIdx));
        
        if (mappings) {
        	for (DcObject reference : references) {
        		for (DcObject mapping : items) {
        		    if (mapping.getValue(DcMapping._B_REFERENCED_ID).equals(reference.getID())) {
        				result.add(mapping);
        				break;
        			}
        		}
        	}
        } else {
        	result.addAll(references);
        }
        
        return result;
    }
    
    public static String getLocalIPAddress() {
    	try {
    		String address = InetAddress.getLocalHost().getHostAddress();
    		return address;
    	} catch (Exception e) {
    		logger.error("Could not retrieve internal IP address", e);
    	}
    	
    	return "";
    }
    
    public static String getExternalIPAddress() {
        BufferedReader in = null;
        String ip = null;
        
        try {
            URL url = new URL("http://checkip.amazonaws.com");
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            ip = in.readLine();
        } catch (Exception e) {
            logger.error("Could not retrieve external IP address", e);
            
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {}
            }
        }
        
        return ip;
    }
    
    public static boolean isDriveTraversable(File drive) {
        return fsv.isTraversable(drive);
    }
    
    public static boolean canRead(File drive) {
        return fsv.isTraversable(drive) && drive.canRead();        
    }
    
    public static String getSystemName(File f) {
        return fsv.getSystemDisplayName(f);
    }
    
    public static String getDatabaseTableName() {
        return "tbl_" + CoreUtilities.getUniqueID().replaceAll("-", "");
    }
    
    public static String getDatabaseColumnName() {
        return "col_" + CoreUtilities.getUniqueID().replaceAll("-", "");
    }
    
    public static int[] getUniqueValues(int[] array) {
    	LinkedHashSet<Integer> set = new LinkedHashSet<Integer>();

	    for (int i = 0; i < array.length; i++)
	        set.add(array[i]);

	    return set.stream().mapToInt(Integer::intValue).toArray();
    }
    
    /**
     * Converts an ordinary string to something which is allowed to be used in a
     * filename or pathname.
     */
    public static String toFilename(String text) {
        String s = text == null ? "" : text.trim().toLowerCase();

        s = s.replaceAll("\n", "");
        s = s.replaceAll("\r", "");
        
        for (int i = 0; i < normalizer.length; i++) {
            Matcher ma = normalizer[i].matcher(s);
            s = ma.replaceAll("");
        }
        
        s = StringUtils.normalize2(s);
        s = s.replaceAll("[\\-]", "");
        s = s.replaceAll(" ", "");
        
        return s.trim();
    }
    
    /**
     * Converts an ordinary string to something which is allowed to be used in a
     * column or table name. It does not check for any preserved names (!). 
     */
    public static String toDatabaseName(String text) {
        String s = toFilename(text);
        
        if (s.length() > 0) {
            char c = s.charAt(0);
            if (Character.isDigit(c))
                s = "db_" + s;      
        }
        
        return s.trim();
    }
    
    public static String getFirstName(String name) {
    	if (name.indexOf(",") > -1) {
    		return name.substring(name.indexOf(",") + 1).trim();
    	} else if (name.indexOf(" ") > -1) {
    		String firstname = name.substring(0, name.indexOf(" ")).trim();
    		if (name.indexOf("(") > -1)
    			firstname += " " + name.substring(name.indexOf("("));
    		
    		return firstname;
    	} else {
    		return "";
    	}
    }
    
    public static String getLastName(String name) {
    	if (name.indexOf(",") > -1) {
    		return name.substring(0, name.indexOf(",")).trim();
    	} else if (name.indexOf(" ") > -1) {
    		String lastname = name.substring(name.indexOf(" ") + 1).trim();
    		if (lastname.indexOf("(") > -1)
    			lastname = lastname.substring(0, lastname.indexOf("(")).trim();
    		
    		return lastname;
    	} else {
    		return name;
    	}
    }
    
    public static String getName(String firstname, String lastname) {
        firstname = firstname == null ? "" : firstname.trim();
        lastname = lastname == null ? "" : lastname.trim();
        return (firstname + " " + lastname).trim();
    }

    
    public static Object getQueryValue(Object o, DcField field) {
        Object value = o;
        
        if (field == null)
        	return null;
        
        // This first check does not make much sense but I honestly do not dare to remove it.. for now..
        if (isEmpty(value) && (field.getModule() != DcModules._PERMISSION && field.getIndex() == Permission._B_FIELD))
            value = null;
        else if ("".equals(value))
            value = null;
        else if (value instanceof DcObject)
            value = ((DcObject) value).getID();
        else if ((field.getValueType() == DcRepository.ValueTypes._DOUBLE) && value instanceof String)
            value = Double.valueOf((String) value);
        else if ((field.getValueType() == DcRepository.ValueTypes._BIGINTEGER ||
                  field.getValueType() == DcRepository.ValueTypes._LONG) &&
                 value instanceof String)
            value = Long.valueOf((String) value);
        else if (value instanceof DcObject)
            value = ((DcObject) value).getID();        

        return value;
    }  
    
    public static String getValidPath(String filename) {
        
        if (filename == null) return "";
        
        String s = filename;
        
        if (filename.indexOf('\\') > -1 || filename.indexOf('/') > -1) {
            String[] mappings = DcSettings.getStringArray(DcRepository.Settings.stDriveMappings);
            if (mappings != null) {
                for (String mapping : mappings) {
                    StringTokenizer st = new StringTokenizer(mapping, "/&/");
                    String drive = (String) st.nextElement();
                    String mapsTo = (String) st.nextElement();
                    
                    if (s.length() > drive.length() && s.substring(0, drive.length()).equalsIgnoreCase(drive)) {
                        s = mapsTo + s.substring(drive.length());
                        break;
                    }
                }
            }

            s = getRelativePath(DcConfig.getInstance().getInstallationDir(), s);
        }
        
        return s;
    }
    
    public static String getRelativePath(String basePath, String targetFile) {
        
        if (targetFile == null || targetFile.startsWith("."))
            return targetFile;
        
        String relativePath = "";
        
        //make them equal first
        if (!DcConfig.getInstance().getPlatform().isWin()) {
            basePath = new File(basePath.replaceAll("\\\\", "\\/")).toString();
            targetFile = new File(targetFile.replaceAll("\\\\", "\\/")).toString();
        } else {
            basePath = new File(basePath.replaceAll("\\/", "\\\\")).toString();
            targetFile = new File(targetFile.replaceAll("\\/", "\\\\")).toString();            
        }
        
        while (basePath.endsWith("/") || basePath.endsWith("\\"))
            basePath = basePath.substring(0, basePath.length() - 1);

        while (targetFile.endsWith("/") || targetFile.endsWith("\\"))
            targetFile = targetFile.substring(0, targetFile.length() - 1);

        if (targetFile.startsWith(basePath)) {
            relativePath = "." + File.separator + targetFile.substring(basePath.length() + 1, targetFile.length());
        } else {
            relativePath = targetFile;
        }
        
        return relativePath;
    }
    
    public static String getOriginalFilename(String filename) {
        String s = filename;
        
        if (s != null) {
            String[] mappings = DcSettings.getStringArray(DcRepository.Settings.stDriveMappings);
            if (mappings != null) {
                for (String mapping : mappings) {
                    StringTokenizer st = new StringTokenizer(mapping, "/&/");
                    String mapsTo = (String) st.nextElement();
                    String drive = (String) st.nextElement();
                    
                    if (s.length() > drive.length() && s.substring(0, drive.length()).equalsIgnoreCase(drive)) {
                        s = mapsTo + s.substring(drive.length());
                        break;
                    }
                }
            }
        }
        
        return s;
    }

    public static boolean isSystemDrive(File drive) {
    	return getSystemDrives().contains(drive);
    }
    
    public static Collection<File> getSystemDrives() {
        Collection<File> drives = new ArrayList<File>();
        for (File file : File.listRoots())
            drives.add(file);
        return drives;
    }
    
    public static Collection<File> getDrives() {
        Collection<File> drives = getSystemDrives();
        String[] dirs = DcSettings.getStringArray(DcRepository.Settings.stDirectoriesAsDrives);
        
        if (dirs != null) {
            for (String dir: dirs)
                drives.add(new File(dir));
        }
        
        return drives;
    }
    
    public static boolean sameImage(byte[] img1, byte[] img2) {
        boolean same = img1.length == img2.length;
        if (same) {
            for (int i = 0; i < img1.length; i++) {
                same = img1[i] == img2[i];
                if (!same)
                    break;
            }
        }
        return same;
    }    
    
    public static Collection<String> getCharacterSets() {
        Collection<String> characterSets = new ArrayList<String>(); 
        for (String name :  Charset.availableCharsets().keySet()) {
            characterSets.add(name);
        }
        return characterSets;
    }
    
    public static String toFileSizeString(Long l) {
        if (l == null) return "";
        
        String s = DcSettings.getString(DcRepository.Settings.stDecimalGroupingSymbol);
        char groupingChar = s != null && s.length() > 0 ? s.charAt(0) : ',';

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator(groupingChar);
        symbols.setInternationalCurrencySymbol("EUR");

        DecimalFormat format = new DecimalFormat("###,###", symbols);
        format.setGroupingSize(3);
        return format.format(l) + " KB";
    }
    
    public static String toString(Double d) {
        if (d == null) return "";
        
        String s = DcSettings.getString(DcRepository.Settings.stDecimalSeparatorSymbol);
        char decimalSep = s != null && s.length() > 0 ? s.charAt(0) : ',';
        s = DcSettings.getString(DcRepository.Settings.stDecimalGroupingSymbol);
        char groupingSep = s != null && s.length() > 0 ? s.charAt(0) : '.';
        
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator(decimalSep);
        symbols.setGroupingSeparator(groupingSep);
        
        
        DecimalFormat format = new DecimalFormat("###,###.00", symbols);
        format.setGroupingSize(3);
        return format.format(d);
    }

    public static Long getSize(File file) {
        return Long.valueOf(file.length());  
    }
    
    /**
     * Creates a unique ID. Can be used for custom IDs in the database.
     * Based on date / time + random number
     * @return unique ID as String
     */
    public static String getUniqueID() {
        return UUID.randomUUID().toString();
    }
    
    public static boolean isSameFile(File src, File tgt) {
        if (CoreUtilities.getSize(src).longValue() == CoreUtilities.getSize(tgt).longValue()) {
            String hash1 = Hash.getInstance().calculateHash(src.toString());
            String hash2 = Hash.getInstance().calculateHash(tgt.toString());
            return hash1.equals(hash2);
        }

        return false;
    }
    
    /**
     * Retrieved the file extension of a file
     * @param f file to get the extension from
     * @return extension or empty string
     */
    public static String getExtension(File f) {
        String name = f.getName().toLowerCase();
        int i = name.lastIndexOf( "." );
        if (i == -1) {
            return "";
        }
        return name.substring( i + 1 );
    }    
    
    public static int getIntegerValue(String s) {
        char[] characters = s.toCharArray();
        String test = "";
        for (int i = 0; i < characters.length; i++) {
            if (Character.isDigit(characters[i])) test += "" + characters[i];
        }
        
        int number = 0;
        try {
            number = Integer.valueOf(test).intValue();
        } catch (Exception ignore) {}
        
        return number;
    }
   
    /**
     * Reads the content of a file (fully)
     * @param file file to retrieve the content from
     * @return content of the file as a byte array
     * @throws Exception
     */
    public static byte[] readFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(is);
        
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            bis.close();
            throw new IOException("File is too large to read " + file.getName());
        }
    
        byte[] bytes = new byte[(int)length];
    
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=bis.read(bytes, offset, bytes.length-offset)) >= 0)
            offset += numRead;

        bis.close();
        is.close();

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        
        return bytes;    
    }

    public static DcImageIcon base64ToImage(String base64) {
        byte[] bytes = Base64.decode(base64.toCharArray());
        return new DcImageIcon(bytes);
    }
    
    public static byte[] getBytes(DcImageIcon icon) {
        return getBytes(icon, DcImageIcon._TYPE_PNG);
    }
    
    public static byte[] getBytes(DcImageIcon icon, int type) {
        return getBytes(icon.getImage(), type);
    }
    
    public static byte[] getBytes(Image image, int type) {
    	BufferedImage bi;
    	if (image instanceof BufferedImage)
    		bi = (BufferedImage) image;
    	else 
    		bi = CoreUtilities.toBufferedImage(new DcImageIcon(image), -1, -1, BufferedImage.TYPE_INT_ARGB);
    	
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        
        byte[] bytes = null;
        try {
            ImageIO.write(bi, (type == DcImageIcon._TYPE_JPEG ? "JPG" : "PNG"), bos);
            bos.flush();
            bytes = baos.toByteArray();
            bi.flush();
        } catch (IOException e) {
            logger.error(e, e);
        } 
        
        try {
            baos.close();
            bos.close();
        } catch (IOException e) {
            logger.error(e, e);
        }
        
        return bytes;
    }
    
    public static boolean isSameImage(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            for (int x = 0; x < img1.getWidth(); x++) {
                for (int y = 0; y < img1.getHeight(); y++) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y))
                        return false;
                }
            }
        } else {
            return false;
        }
        
        return true;
    }    
    
    public static String getTempFolder() {
    	return System.getProperty("java.io.tmpdir");    	
    }
    
    public static void writeToFile(DcImageIcon icon, File file) throws Exception {
        writeScaledImageToFile(icon, file, -1, -1);
    }   

    public static void writeToFile(byte[] b, String filename) throws Exception {
        writeToFile(b, new File(filename));
    } 
    
    public static void writeToFile(byte[] b, File file) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        bos.write(b);
        bos.flush();
        bos.close();
    }   

    public static BufferedImage getScaledImage(DcImageIcon icon) {
        return getScaledImage(icon, 500, 400);
    }    

    public static Image getScaledImage(byte[] bytes, int width, int height) {
        return toBufferedImage(new DcImageIcon(bytes), width, height, BufferedImage.TYPE_INT_ARGB);
    }    
    
    public static BufferedImage getScaledImage(DcImageIcon icon, int width, int height) {
        return toBufferedImage(icon, width, height, BufferedImage.TYPE_INT_ARGB);
    }    
    
    public static void writeScaledImageToFile(DcImageIcon icon, File file) throws Exception {
    	writeScaledImageToFile(icon, file, 500, 400);
    }
    
    public static void writeMaxImageToFile(DcImageIcon icon, File file) throws Exception {
    	DcDimension dimMax = DcSettings.getDimension(DcRepository.Settings.stMaximumImageResolution);
    	writeScaledImageToFile(icon, file, dimMax.getWidth(), dimMax.getHeight());
    }    

    public static void writeScaledImageToFile(DcImageIcon icon, File file, int w, int h) throws Exception {
    	
    	if (!file.getParentFile().exists())
    		file.getParentFile().mkdirs();
    	
        BufferedImage bufferedImage = toBufferedImage(icon, w, h, icon.getType() == DcImageIcon._TYPE_JPEG ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
        
        if (!ImageIO.write(bufferedImage, (icon.getType() == DcImageIcon._TYPE_JPEG ? "JPG" : "PNG"), file)) {
        	logger.error("Image was not stored " + file + ". Most likely the image format is not supported or the image is corrupt");
        }
        
        bufferedImage.flush();
    }
    
    public static String getHexColor(Color color) {
        String hexColor = "#" + Integer.toHexString(color.getRed());
        hexColor += Integer.toHexString(color.getGreen());
        hexColor += Integer.toHexString(color.getBlue()); 
        return hexColor.toUpperCase();
    }
    
    public static boolean isEmpty(Object o) {
        boolean empty = o == null;
        
        if (o instanceof Number) 
            empty = o.equals(Long.valueOf(-1)) || o.equals(Long.valueOf(0));
        else if (!empty && o instanceof String)
            empty = ((String) o).trim().length() == 0;
        else if (!empty && o instanceof Collection)
            empty = ((Collection<?>) o).size() == 0;
        
        return empty;
    }

    public static String getComparableString(Object o) {
        return CoreUtilities.isEmpty(o) ? "" : o instanceof String ? ((String) o) : o.toString();
    }
    
    public static void copy(File source, File target, boolean overwrite) throws IOException {
        
        if (source.equals(target))
            return;
        
        if (!overwrite && target.exists())
            return;
        
        // create the folders if not present
        target.getParentFile().mkdirs();
        
        // native code failed to move the file; do it the custom way
        FileInputStream fis = new FileInputStream(source);
        BufferedInputStream bis = new BufferedInputStream(fis);
    
        FileOutputStream fos = new FileOutputStream(target);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        
        int count = 0;
        int b;
        while ((b = bis.read()) > -1) {
            bos.write(b);
            count++;
            if (count == 2000) {
                bos.flush();
                count = 0;
            }
        }
        
        bos.flush();
        
        fis.close();
        bis.close();
        bos.close();
    }
    
    public static void rename(File currentFile, File newFile, boolean overwrite) throws IOException {
        
        if (currentFile.equals(newFile))
            return;

        if (newFile.exists() && !overwrite)
            return;
        
        if (newFile.getParentFile() != null)
            newFile.getParentFile().mkdirs();
        
        boolean success = currentFile.renameTo(newFile);
        
        if (!success) {
            copy(currentFile, newFile, overwrite);
            currentFile.delete();
        }
    }

    public static String getCurrentDirectory() throws Exception {
    	File fl = new File(".");
    	fl = fl.getCanonicalFile();
        return fl.toString();
    }
    
    /**
     * Gets the content of a file and converts it to a base64 string
     * @param url URL of file
     * @return base64 content of the file
     */
    public static String fileToBase64String(File file) {
        try {
            byte[] b = readFile(file);
            file = null;
            return String.valueOf(Base64.encode(b));
        } catch (Exception e) {
            logger.error("Error while converting content from " + file + " to base64", e);
        }
        return "";
    }
    
    public static BufferedImage toBufferedImage(ImageIcon icon, int rgbType) {
        return toBufferedImage(icon, -1, -1, rgbType);
    }
    
    public static BufferedImage toBufferedImage(ImageIcon icon, int width, int height, int rgbType) {
        
        // make sure the image is loaded
        icon.setImage(icon.getImage());
        Image image = icon.getImage();
        
        int imgW = image.getWidth(null);
        int imgH = image.getHeight(null);
        
        int w = width > 0 ? width : imgW;
        int h = height > 0 ? height : imgH;
        
        if (imgW <= width && imgH <= height) {
            // do not scale down if not needed
            w = imgW;
            h = imgH;
        } else {
            // make sure the image ratio remains the same
            double scaledRatio = (double) w / (double) h;
            double imageRatio = (double) imgW / (double) imgH;
            if (scaledRatio < imageRatio)
                h = (int) (w / imageRatio);
            else
                w = (int) (h * imageRatio);
        }
        
        BufferedImage bi = null;
        if (w > -1 && h > -1) {
	        bi = new BufferedImage(w, h, rgbType);
	        
	        Graphics2D g = bi.createGraphics();
	        g.setComposite(AlphaComposite.Src);
	        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	        g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
	        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	        
	        g.drawImage(image, 0, 0, w, h, null);
	        g.dispose();
	        bi.flush();

        } else {
        	logger.error("The image size -1 is invalid");
        	bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
        
        return bi;
    }
    
    public static String getLanguage(String iso) {
        
        if (languages.isEmpty()) {
            try {
                FileInputStream fis = new FileInputStream(new File(DcConfig.getInstance().getResourcesDir(), "languages.properties"));
                languages.load(fis);
                fis.close();
            } catch (Exception e) {
                logger.error("Could not load languages file", e);
            }
        }
        
        return (String) languages.get(iso);
    }
    
    public static String getTimestamp() {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }

    public static String getLocalTimestamp() {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        DateFormat dfm = new SimpleDateFormat(DcSettings.getString(DcRepository.Settings.stDateFormat) + " HH:mm:ss");
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }    

    public static String toString(Date dt) {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }

    public static Date toDate(String s, String pattern) throws ParseException {
    	DateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
    	return format.parse(s);
    }        
    
    public static Date toDate(String s) {
    	LocalDateTime dt = LocalDateTime.parse(s);
    	return Date.from(dt.toInstant(OffsetDateTime.now().getOffset()));
    }        
    
    public static String readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString(StandardCharsets.UTF_8);
    }
    
    public static byte[] readBytesFromStream(InputStream is) throws IOException {
    	byte[] content = IOUtils.toByteArray(is);
        return content;
    }    
    
    public static void downloadFile(String fromUrl, String localFileName) throws IOException {
        File localFile = new File(localFileName);
        if (localFile.exists()) {
            localFile.delete();
        }
        
        localFile.createNewFile();
        URL url = new URL(fromUrl);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(localFileName));
        URLConnection conn = url.openConnection();
        
        InputStream in = conn.getInputStream();
        byte[] buffer = new byte[1024];

        int numRead;
        while ((numRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, numRead);
        }

        if (in != null) {
            in.close();
        }
        
        if (out != null) {
            out.close();
        }
    }
    
    /**
     * Saves the image in the maximum resolution as set in the settings.
     */
    public static DcImageIcon downloadAndStoreImage(String url) {
    	url = url.replace("http://", "https://");
    	
        try {
			BufferedImage bi = ImageIO.read(new URL(url));
			DcImageIcon icon = new DcImageIcon(bi);
			
			if (icon.getIconHeight() > 50) {
    			// write to temp folder
    			File file = new File(CoreUtilities.getTempFolder(), CoreUtilities.getUniqueID() + ".jpg");
    			CoreUtilities.writeMaxImageToFile(icon, file);
    			
    			// delete the file on exit of Data Crow
    			file.deleteOnExit();
    			
    			// flush the in memory image
    			icon.flush();
    			
    			// send image pointing to local disk storage
    			return new DcImageIcon(file);
			}
        } catch (Exception e) {
            logger.debug("Cannot download image from [" + url + "]", e);
        }
        
        return null;    	
    }
    
    public static DcImageIcon rotateImage(DcImageIcon icon, int degrees) {
	    BufferedImage src = CoreUtilities.toBufferedImage(new DcImageIcon(icon.getImage()), BufferedImage.TYPE_INT_ARGB);
	    AffineTransform at = new AffineTransform();
	    
	    at.rotate(Math.toRadians(degrees), src.getWidth() / 2.0, src.getHeight() / 2.0);
	    AffineTransform translationTransform = findTranslation (at, src);
	    at.preConcatenate(translationTransform);
	    BufferedImage destinationBI = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC).filter(src, null);
	
	    return new DcImageIcon(CoreUtilities.getBytes(new DcImageIcon(destinationBI)));
    }
    
	/*
	 * Find proper translations to keep rotated image correctly displayed
	 */
	private static AffineTransform findTranslation(AffineTransform at, BufferedImage bi) {
		Point2D p2din = new Point2D.Double(0.0, 0.0);
		Point2D p2dout = at.transform(p2din, null);
		double ytrans = p2dout.getY();

		p2din = new Point2D.Double(0, bi.getHeight());
		p2dout = at.transform(p2din, null);
		double xtrans = p2dout.getX();

		AffineTransform tat = new AffineTransform();
		tat.translate(-xtrans, -ytrans);

		return tat;
	}  
}

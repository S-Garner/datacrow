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

package org.datacrow.client.console.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.net.URL;

import javax.swing.JComponent;

import org.datacrow.client.util.Utilities;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.Picture;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.utilities.Base64;
import org.datacrow.core.utilities.CoreUtilities;

/**
 * @author RJ
 */
public class DcPicturePane extends JComponent {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcPicturePane.class.getName());
    
    private boolean scaled = true;
    private Dimension size = null;    
	
	private DcImageIcon picture;
	
	private Image img;
	
    private int imageWidth = -1;
    private int imageHeight = -1;
	
    public DcPicturePane(boolean scaled) {
    	this.scaled = scaled;
    }
    
    public void setScaled(boolean b) {
    	this.scaled = b;
    }
    
    public void setValue(Object o) {
    	if (o == picture)
            return;
        
        if (o instanceof Picture) {
            Picture pic = (Picture) o;
            DcImageIcon img = (DcImageIcon) pic.getValue(Picture._D_IMAGE);
            
            if (img == null && !CoreUtilities.isEmpty(pic.getUrl())) {
                o = new DcImageIcon(pic.getUrl());
            } else {
                o = img;
            }
        }
        
        clear();
        initialize();
        
        try {
            if (o == null) {
                picture = null;
            } else {
                if (o instanceof URL) {
                    URL url = (URL) o;
                    picture = new DcImageIcon(url.getFile());
                } else if (o instanceof String) {
                    String value = (String) o;
                    if (value.endsWith("jpg")) {
                        picture = new DcImageIcon(value);
                    } else {
                        String base64 = (String) o;
                        if (base64.length() > 0)
                            picture = new DcImageIcon(Base64.decode(base64.toCharArray()));
                    }
                } else if (o instanceof DcImageIcon) {
                    picture = (DcImageIcon) o;
                    picture.setImage(picture.getImage());
                }
            }
        } catch (Exception e) {
            logger.error(DcResources.getText("msgCouldNotLoadPicture"), e);
        }
        
        initialize();
        revalidate();
        repaint();    	
    }
    
//    public void set
    
    public boolean hasImage() {
    	return img != null;    	
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        
        super.paintComponent(g);

        if (picture != null) {
            try {
                img = picture.getImage();
                
                // less expensive way to prepare the image (using the default instance)
                if (Utilities.getToolkit().prepareImage(img, imageWidth, imageHeight, this))
                    paintImage(g);
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }
    
	public void clear() {
		if (picture != null)
			picture.getImage().flush();

		if (img != null)
			img.flush();
	}
	
    
    /*
     * Find proper translations to keep rotated image correctly displayed
     */
    public AffineTransform findTranslation(AffineTransform at, BufferedImage bi) {
      Point2D p2din = new Point2D.Double (0.0, 0.0);
      Point2D p2dout = at.transform (p2din, null);
      double ytrans = p2dout.getY();

      p2din = new Point2D.Double(0, bi.getHeight());
      p2dout = at.transform(p2din, null);
      double xtrans = p2dout.getX () ;

      AffineTransform tat = new AffineTransform();
      tat.translate(-xtrans, -ytrans);
      
      return tat;
    }    
    
    public void initialize() {
        
        repaint();
        
        if (picture != null) {
            imageWidth = picture.getIconWidth();
            imageHeight = picture.getIconHeight();
        } else {
            imageWidth = -1;
            imageHeight = -1;
        }
    }   	
    
	
    public void grayscale() {
        img = picture.getImage();
        BufferedImage src = CoreUtilities.toBufferedImage(new DcImageIcon(img), BufferedImage.TYPE_INT_ARGB);
        BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null); 
        update(op, src);
    }
    
    public void sharpen() {
        img = picture.getImage();
        BufferedImage src = CoreUtilities.toBufferedImage(new DcImageIcon(img), BufferedImage.TYPE_INT_ARGB);
        BufferedImageOp op = new ConvolveOp(
                new Kernel(3, 3, new float[] { 0.0f, -0.75f, 0.0f, -0.75f, 4.0f, 
                                              -0.75f, 0.0f, -0.75f, 0.0f }));
        update(op, src);
    }
    
    public void blur() {
        img = picture.getImage();
        BufferedImage src = CoreUtilities.toBufferedImage(new DcImageIcon(img), BufferedImage.TYPE_INT_ARGB);
        BufferedImageOp op = new ConvolveOp(
                new Kernel(3, 3, new float[] {.1111f, .1111f, .1111f, .1111f, .1111f, 
                                              .1111f, .1111f, .1111f, .1111f, }));
        update(op, src);
    }
    
    public void update(BufferedImageOp op, BufferedImage src) {
        picture = new DcImageIcon(CoreUtilities.getBytes(new DcImageIcon(op.filter(src, null))));
        initialize();
        repaint();
        revalidate();
    }
    
    public void rotate(int degrees) {
        img = picture.getImage();
        
        BufferedImage src = CoreUtilities.toBufferedImage(new DcImageIcon(img), BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        
        at.rotate(Math.toRadians(degrees), src.getWidth() / 2.0, src.getHeight() / 2.0);
        AffineTransform translationTransform = findTranslation (at, src);
        at.preConcatenate(translationTransform);
        BufferedImage destinationBI = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC).filter(src, null);

        picture = new DcImageIcon(CoreUtilities.getBytes(new DcImageIcon(destinationBI)));
        initialize();
        repaint();
        revalidate();
    }   
    
    public Object getValue() {
    	int width = picture != null ? picture.getIconWidth() : 0;
    	int height = picture != null ? picture.getIconHeight() : 0;
    	
    	if (width == 0 || height == 0)
    		return null;
    	else
    		return picture;
    }
	
    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        paintImage(getGraphics());
        return true;
    }
    
    private boolean scalingAllowed(int width, int height) {
        return scaled && 
              ((height >= 50 && width >= 50) || 
               (imageWidth > size.width || imageHeight > size.height));
    }    
    
    private void paintImage(Graphics g) {
        
        if (g == null || picture == null || img == null) return;
        
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        int width = imageWidth;
        int height = imageHeight;

        size = getSize(size);
        
        if (scalingAllowed(imageWidth, imageHeight)) {
            width =  Math.min(size.width, imageWidth);
            height = Math.min(size.height, imageHeight);
            double scaledRatio = (double) width / (double) height;
            double imageRatio = (double) imageWidth / (double) imageHeight;
        
            if (scaledRatio < imageRatio) {
                height = (int) (width / imageRatio);
            } else {
                width = (int) (height * imageRatio);
            }
        }

        g.translate((getWidth() - width) / 2, (getHeight() - height) / 2);
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();
    }     
}

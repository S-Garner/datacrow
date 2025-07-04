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
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.JComponent;

import org.datacrow.client.util.Utilities;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.utilities.CoreUtilities;

/**
 * @author RJ
 */
public class DcPicturePane extends JComponent {
	
	private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(DcPicturePane.class.getName());
    
    private boolean scaled = true;
    private Dimension size = null;    
	
	private DcImageIcon imageIcon;
	
    private int imageWidth = -1;
    private int imageHeight = -1;
	
    public DcPicturePane(boolean scaled) {
    	this.scaled = scaled;
    }
    
    public void setScaled(boolean b) {
    	this.scaled = b;
    }
    
    public void setImageIcon(DcImageIcon imageIcon) {
    	this.imageIcon = imageIcon;
        initialize();
    }
    
    public boolean hasImage() {
    	return imageIcon != null;    	
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        
        super.paintComponent(g);

        if (imageIcon != null) {
            try {
                if (Utilities.getToolkit().prepareImage(imageIcon.getImage(), imageWidth, imageHeight, this))
                    paintImage(g);
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }
    
	public void clear() {
		if (imageIcon != null)
			imageIcon.getImage().flush();
	}
    
    public void initialize() {
        
        if (imageIcon != null) {
            imageWidth = imageIcon.getIconWidth();
            imageHeight = imageIcon.getIconHeight();
        } else {
            imageWidth = -1;
            imageHeight = -1;
        }
        
    	revalidate();
        repaint();
    }   	
    
    public void grayscale() {
    	
    	if (imageIcon == null) return;
    	
        BufferedImage src = CoreUtilities.toBufferedImage(imageIcon, BufferedImage.TYPE_INT_ARGB);
        BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null); 
        update(op, src);
    }
    
    public void sharpen() {
    	
    	if (imageIcon == null) return;
    	
        BufferedImage src = CoreUtilities.toBufferedImage(imageIcon, BufferedImage.TYPE_INT_ARGB);
        BufferedImageOp op = new ConvolveOp(
                new Kernel(3, 3, new float[] { 0.0f, -0.75f, 0.0f, -0.75f, 4.0f, 
                                              -0.75f, 0.0f, -0.75f, 0.0f }));
        update(op, src);
    }
    
    public void blur() {
    	
    	if (imageIcon == null) return;
    	
        BufferedImage src = CoreUtilities.toBufferedImage(imageIcon, BufferedImage.TYPE_INT_ARGB);
        BufferedImageOp op = new ConvolveOp(
                new Kernel(3, 3, new float[] {.1111f, .1111f, .1111f, .1111f, .1111f, 
                                              .1111f, .1111f, .1111f, .1111f, }));
        update(op, src);
    }
    
    public void update(BufferedImageOp op, BufferedImage src) {
    	imageIcon = new DcImageIcon(CoreUtilities.getBytes(new DcImageIcon(op.filter(src, null))));
        initialize();
        repaint();
        revalidate();
    }
    
    public void rotate(int degrees) {
    	
    	if (imageIcon == null) return;
    	
    	imageIcon = CoreUtilities.rotateImage(imageIcon, degrees);
    	
        initialize();
        repaint();
        revalidate();
    }
    
    public DcImageIcon getImageIcon() {
    	int width = imageIcon != null ? imageIcon.getIconWidth() : 0;
    	int height = imageIcon != null ? imageIcon.getIconHeight() : 0;
    	
    	if (width == 0 || height == 0)
    		return null;
    	else
    		return imageIcon;
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
    	
        if (g == null || imageIcon == null) return;
        
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
        g.drawImage(imageIcon.getImage(), 0, 0, width, height, null);
        g.dispose();
    }     
}

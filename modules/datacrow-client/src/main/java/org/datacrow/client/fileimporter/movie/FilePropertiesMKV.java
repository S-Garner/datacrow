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

package org.datacrow.client.fileimporter.movie;

import java.io.RandomAccessFile;

import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.log.DcLogger;
import org.datacrow.core.utilities.CoreUtilities;
import org.ebml.io.FileDataSource;
import org.ebml.matroska.MatroskaFile;
import org.ebml.matroska.MatroskaFileTrack;
import org.ebml.matroska.MatroskaFileTrack.TrackType;

class FilePropertiesMKV extends FileProperties {
    
    private transient static final DcLogger logger = DcLogManager.getInstance().getLogger(FilePropertiesMKV.class.getName());

    @Override
	protected void process(RandomAccessFile raf, String filename) throws Exception {
        raf.seek(0);
        
        FileDataSource fds = new FileDataSource(filename);
        
        try {
            MatroskaFile mkf = new MatroskaFile(fds);
    		mkf.readFile();
    
    		double duration = mkf.getDuration();
    		duration = duration > 0 ? duration / 1000 : duration;
    		setDuration((int) duration);
    		
    		if (mkf.getTrackList() != null) {
    		    
        		for (MatroskaFileTrack track : mkf.getTrackList()) {
        		    if (track.getTrackType() ==  TrackType.VIDEO) {
        		        setVideoResolution(track.getVideo().getDisplayWidth() + "x" + track.getVideo().getDisplayHeight());
        		        setVideoCodec(track.getCodecID());
        		        setName(track.getName());
        		        
        		        if (track.getLanguage() != null) {
            		        String language = CoreUtilities.getLanguage(track.getLanguage());
            		        language = language == null || language.length() == 0 ? track.getLanguage() : language;
            		        setLanguage(language);
        		        }
        		    } else if (track.getTrackType() ==  TrackType.SUBTITLE) {
        		        String subtitles = getSubtitles();
        		        subtitles += subtitles.length() > 0 ? ", " : "";
        		        
                        String language = CoreUtilities.getLanguage(track.getName());
                        language = language == null || language.length() == 0 ? track.getName() : track.getName();
        		        subtitles += language;
        		        setSubtitles(subtitles);
        		        
        		    } else if (track.getTrackType() ==  TrackType.AUDIO) {
        		        setAudioChannels(track.getAudio().getChannels());
        		        setAudioCodec(track.getCodecID());
        		    }
        		}
    		}

    		setContainer("MKV (Matroska)");

        } catch (Exception e) {
            logger.error("Failed to parse MKV file " + filename, e);
        } finally {
            fds.close();
        }
	}
}

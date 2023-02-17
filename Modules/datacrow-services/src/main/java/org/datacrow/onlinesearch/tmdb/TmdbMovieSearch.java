/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.net                             *
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

package org.datacrow.onlinesearch.tmdb;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.datacrow.core.DcRepository;
import org.datacrow.core.DcRepository.ExternalReferences;
import org.datacrow.core.http.HttpConnectionException;
import org.datacrow.core.http.HttpConnectionUtil;
import org.datacrow.core.log.DcLogManager;
import org.datacrow.core.modules.DcModules;
import org.datacrow.core.objects.DcAssociate;
import org.datacrow.core.objects.DcImageIcon;
import org.datacrow.core.objects.DcMediaObject;
import org.datacrow.core.objects.DcObject;
import org.datacrow.core.objects.helpers.Movie;
import org.datacrow.core.services.IOnlineSearchClient;
import org.datacrow.core.services.OnlineSearchUserError;
import org.datacrow.core.services.OnlineServiceError;
import org.datacrow.core.services.Region;
import org.datacrow.core.services.SearchMode;
import org.datacrow.core.services.SearchTask;
import org.datacrow.core.services.SearchTaskUtilities;
import org.datacrow.core.services.Servers;
import org.datacrow.core.services.plugin.IServer;
import org.datacrow.core.utilities.CoreUtilities;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.enumeration.SearchType;
import com.omertron.themoviedbapi.model.Genre;
import com.omertron.themoviedbapi.model.artwork.Artwork;
import com.omertron.themoviedbapi.model.credits.MediaCreditCast;
import com.omertron.themoviedbapi.model.credits.MediaCreditCrew;
import com.omertron.themoviedbapi.model.movie.MovieInfo;
import com.omertron.themoviedbapi.results.ResultList;

/**
 * Class for handling searches over TheMovieDatabase's API, powered by https://github.com/Omertron/api-themoviedb
 *
 * @author Robert Jan Van Der Waals - Initial implementation for api 3.x
 * @author FlagCourier - Conversion to api-themoviedb 4.x and general cleanup.
 */
public class TmdbMovieSearch extends SearchTask {

    public static final String ORIGNAL_SIZE = "original";
    private static final Logger logger = DcLogManager.getLogger(TmdbMovieSearch.class.getName());
    private TheMovieDbApi tmdb;

    public TmdbMovieSearch(
            IOnlineSearchClient listener, 
            IServer server, 
            Region region,
            SearchMode mode,
            String query,
            Map<String, Object> additionalFilters) {
        
        super(listener, server, region, mode, query, additionalFilters);
        
        try {
            String apiKey = Servers.getInstance().getApiKey("tmdb");
            tmdb = new TheMovieDbApi(apiKey);
        } catch (MovieDbException e) {
            logger.error(e, e);
        }
    }

    @Override
    public String getWhiteSpaceSubst() {
        return " ";
    }
    
    @Override
    public DcObject query(DcObject dco) throws Exception {
        return getItem(dco, true);
    }
    
    @Override
    protected DcObject getItem(URL url) throws Exception {
        return null;
    }

    @Override
    protected DcObject getItem(Object key, boolean full) throws Exception {
        Movie movie = (Movie) key;
        
        String movieId = movie.getExternalReference(ExternalReferences._TMDB);
        MovieInfo movieInfo = tmdb.getMovieInfo(Integer.parseInt(movieId), getRegion().getCode(), "images,casts,list,crew");
        
        if (full) {
            setImages(movieInfo, movie);
            setGenres(movieInfo, movie);
            setCast(movieInfo, movie);
            setCrew(movieInfo, movie);
        }
        
        return movie;
    }
    
    private void setImages(MovieInfo movieInfo, Movie movie) {
        try {
            byte[] img;
            String imgUrl;
            for (Artwork aw : movieInfo.getImages()) {
                imgUrl = aw.getFilePath();
                
                if (CoreUtilities.isEmpty(imgUrl)) continue;

                switch (aw.getArtworkType()) {
                    case POSTER:
                        img = HttpConnectionUtil.retrieveBytes(tmdb.createImageUrl(imgUrl, ORIGNAL_SIZE));
                        movie.setValue(Movie._X_PICTUREFRONT, new DcImageIcon(img));
                        break;
                    case BACKDROP:
                        img = HttpConnectionUtil.retrieveBytes(imgUrl);
                        movie.setValue(Movie._Y_PICTUREBACK, new DcImageIcon(img));
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            logger.error(e, e);
        }
    }
    
    private void setCast(MovieInfo movieInfo, Movie movie) {
        try {
            DcObject dco;
            byte[] img;
            URL imgUrl;
            for (MediaCreditCast pc : movieInfo.getCast()) {
                dco = movie.createReference(Movie._I_ACTORS, pc.getName());
                if (dco.isNew()
                        && DcModules.get(DcModules._MOVIE).getSettings().getBoolean(DcRepository.ModuleSettings.stOnlineSearchSubItems)
                        && !CoreUtilities.isEmpty(pc.getArtworkPath())) {

                    imgUrl = tmdb.createImageUrl(pc.getArtworkPath(), ORIGNAL_SIZE);
                    img = HttpConnectionUtil.retrieveBytes(imgUrl);
                    dco.setValue(DcAssociate._D_PHOTO, new DcImageIcon(img));
                }
            }
        } catch (Exception e) {
            logger.debug("Could not retrieve actors for " + movie, e);
        }
    }
    
    private void setCrew(MovieInfo movieInfo, Movie movie) {
        try {
            DcObject dco;
            byte[] img;
            URL imgUrl;
            for (MediaCreditCrew pc : movieInfo.getCrew()) {

                if (pc.getJob() != null && pc.getJob().equalsIgnoreCase("director")) {
                    dco = movie.createReference(Movie._J_DIRECTOR, pc.getName());

                    if (dco.isNew()
                            && DcModules.get(DcModules._MOVIE).getSettings().getBoolean(DcRepository.ModuleSettings.stOnlineSearchSubItems)
                            && !CoreUtilities.isEmpty(pc.getArtworkPath())) {

                        imgUrl = tmdb.createImageUrl(pc.getArtworkPath(), ORIGNAL_SIZE);
                        img = HttpConnectionUtil.retrieveBytes(imgUrl);
                        dco.setValue(DcAssociate._D_PHOTO, new DcImageIcon(img));
                    }
                }
            }
        } catch (MovieDbException | HttpConnectionException e) {
            logger.debug("Could not retrieve crew for " + movie, e);
        }
    }
    
    private void setGenres(MovieInfo movieInfo, Movie movie) {
        try {
            for (Genre genre : movieInfo.getGenres()) {
            	movie.createReference(Movie._H_GENRES, genre.getName());
            }
        } catch (Exception e) {
            logger.debug("Could not retrieve genres for " + movie, e);
        }
    }

    @Override
    protected void preSearchCheck() {
        SearchTaskUtilities.checkForIsbn(this);
    }
    
    @Override
    protected Collection<Object> getItemKeys() throws OnlineSearchUserError, OnlineServiceError {
        Collection<Object> keys = new ArrayList<>();
        
        try {

            int pg = 0;
            String language = "";
            int year = 0;
            boolean adult = true;
            int primeRelYr = 0;
            SearchType searchType = SearchType.PHRASE;
            
            waitBetweenRequest();
            
            ResultList<MovieInfo> movieList =
                    tmdb.searchMovie(getQuery(), pg, language, adult, year, primeRelYr, searchType);
    
            String date;
            Movie movie;
            int count = 0;
    
            for (MovieInfo movieInfo : movieList.getResults()) {
                movieInfo = tmdb.getMovieInfo(movieInfo.getId(), getRegion().getCode());
    
                movie = new Movie();
                movie.setValue(DcMediaObject._A_TITLE, movieInfo.getTitle());
                movie.setValue(Movie._G_WEBPAGE, movieInfo.getHomepage());
                movie.setValue(Movie._F_TITLE_LOCAL, movieInfo.getOriginalTitle());
                movie.setValue(DcMediaObject._B_DESCRIPTION, movieInfo.getOverview());
                
                setServiceInfo(movie);
                
                date = movieInfo.getReleaseDate();
                if (!CoreUtilities.isEmpty(date) && date.length() > 4) {
                    movie.setValue(DcMediaObject._C_YEAR, date.substring(0, 4));
                }
                    
                if (movieInfo.getRuntime() > 0)
                    movie.setValue(Movie._L_PLAYLENGTH, (movieInfo.getRuntime() * 60));
                
                movie.addExternalReference(ExternalReferences._TMDB, String.valueOf(movieInfo.getId()));
                keys.add(movie);
                
                count++;
                if (count == getMaximum()) break;
            }
        } catch (Exception e) {
            throw new OnlineServiceError(e);
        }
        
        return keys;
    }
}

package org.datacrow.onlinesearch.tmdb;

import org.datacrow.core.objects.DcObject;

public class TmdbSearchResult {

	private DcObject dco;
	private String movieId;
    
    public String getMovieId() {
		return movieId;
	}

	public void setMovieId(String movieId) {
		this.movieId = movieId;
	}

	public TmdbSearchResult(DcObject dco) {
        this.dco = dco;
    }
    
    public DcObject getDco() {
        return dco;
    }
}

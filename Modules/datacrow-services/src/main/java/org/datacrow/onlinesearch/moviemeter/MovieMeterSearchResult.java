package org.datacrow.onlinesearch.moviemeter;

import org.datacrow.core.objects.DcObject;

public class MovieMeterSearchResult {

	private DcObject dco;
	private String movieId;
    
    public String getMovieId() {
		return movieId;
	}

	public void setMovieId(String movieId) {
		this.movieId = movieId;
	}

	public MovieMeterSearchResult(DcObject dco) {
        this.dco = dco;
    }
    
    public DcObject getDco() {
        return dco;
    }
}

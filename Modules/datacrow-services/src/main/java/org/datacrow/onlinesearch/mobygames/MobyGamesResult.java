package org.datacrow.onlinesearch.mobygames;

import java.util.ArrayList;
import java.util.Collection;

import org.datacrow.core.objects.DcObject;

public class MobyGamesResult {
    
    private DcObject dco;
    
    private Collection<String> screenshots = new ArrayList<>();
    
    public MobyGamesResult(DcObject dco) {
        this.dco = dco;
    }
    
    public DcObject getDco() {
        return dco;
    }
    
    public Collection<String> getScreenshotLinks() {
        return screenshots;
    }
    
    public void addScreenshot(String link) {
        screenshots.add(link);
    }
}

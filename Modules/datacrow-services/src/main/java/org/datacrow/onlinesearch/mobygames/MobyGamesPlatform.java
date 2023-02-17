package org.datacrow.onlinesearch.mobygames;

public class MobyGamesPlatform {
    
    private String id;
    private String description;
    
    public MobyGamesPlatform(String id, String description) {
        this.id = id;
        this.description = description;
    }
    
    public String getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }
    
    
    @Override
    public int hashCode() {
        return getId().hashCode();
    }
    
    @Override
    public String toString() {
        return description;
    }
}

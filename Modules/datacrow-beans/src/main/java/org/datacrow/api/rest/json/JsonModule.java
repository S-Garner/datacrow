package org.datacrow.api.rest.json;

import org.datacrow.core.modules.DcModule;

public class JsonModule {
    
    private int index;
    private String label;
    private byte[] icon;
    
    public JsonModule(DcModule module) {
        this.index = module.getIndex();
        this.label = module.getName();
        this.icon = module.getIcon32().getBytes();
    }
    
    public JsonModule(int index, String label, byte[] icon) {
        this.index = index;
        this.label = label;
        this.icon = icon;
    }

    public int getIndex() {
        return index;
    }

    public String getLabel() {
        return label;
    }

    public byte[] getIcon() {
        return icon;
    }
}

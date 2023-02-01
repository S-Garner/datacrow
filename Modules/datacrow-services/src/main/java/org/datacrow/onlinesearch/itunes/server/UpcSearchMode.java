package org.datacrow.onlinesearch.itunes.server;

import org.datacrow.core.objects.helpers.MusicAlbum;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.SearchMode;

public class UpcSearchMode extends SearchMode {

    public UpcSearchMode() {
        super(MusicAlbum._P_EAN);
    }

    @Override
    public String getDisplayName() {
        return DcResources.getText("lblUPCSearchMode");
    }

    @Override
    public boolean singleIsPerfect() {
        return true;
    }

    @Override
    public boolean keywordSearch() {
        return false;
    }
}

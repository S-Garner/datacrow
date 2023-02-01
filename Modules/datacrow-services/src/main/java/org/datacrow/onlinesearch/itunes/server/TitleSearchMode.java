package org.datacrow.onlinesearch.itunes.server;

import org.datacrow.core.objects.helpers.MusicAlbum;
import org.datacrow.core.resources.DcResources;
import org.datacrow.core.services.SearchMode;

public class TitleSearchMode extends SearchMode {

    public TitleSearchMode() {
        super(MusicAlbum._A_TITLE);
    }

    @Override
    public String getDisplayName() {
        return DcResources.getText("lblKeywordSearchMode");
    }

    @Override
    public boolean singleIsPerfect() {
        return false;
    }

    @Override
    public boolean keywordSearch() {
        return true;
    }
}

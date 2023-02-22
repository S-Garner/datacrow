package org.datacrow.onlinesearch.openlibrary;

import org.datacrow.core.services.SearchMode;

public class KeywordSearch extends SearchMode {

	public KeywordSearch(int fieldBinding) {
		super(fieldBinding);
	}

	@Override
	public String getDisplayName() {
		return "lblKeywordSearchMode";
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

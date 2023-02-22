package org.datacrow.onlinesearch.openlibrary;

import org.datacrow.core.services.SearchMode;

public class IsbnSearch extends SearchMode {

	public IsbnSearch(int fieldBinding) {
		super(fieldBinding);
	}

	@Override
	public String getDisplayName() {
		return "lblSearchIsbn";
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

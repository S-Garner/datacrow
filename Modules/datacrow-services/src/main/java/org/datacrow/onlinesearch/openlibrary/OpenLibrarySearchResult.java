/******************************************************************************
 *                                     __                                     *
 *                              <-----/@@\----->                              *
 *                             <-< <  \\//  > >->                             *
 *                               <-<-\ __ /->->                               *
 *                               Data /  \ Crow                               *
 *                                   ^    ^                                   *
 *                              info@datacrow.org                             *
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

package org.datacrow.onlinesearch.openlibrary;

import java.util.Map;

import org.datacrow.core.objects.DcObject;

public class OpenLibrarySearchResult {

	private DcObject dco;
    private String workId;
    private String editionId;
    private String mainCoverId;
    private Map<?, ?> editionData;
    private Map<?, ?> workData;
    
    public Map<?, ?> getEditionData() {
		return editionData;
	}

	public void setEditionData(Map<?, ?> editionData) {
		this.editionData = editionData;
	}

	public void setWorkData(Map<?, ?> workData) {
		this.workData = workData;
	}
	
	public Map<?, ?> getWorkData() {
		return workData;
	}

	public String getEditionId() {
		return editionId;
	}

	public void setEditionId(String editionId) {
		this.editionId = editionId;
	}

	public OpenLibrarySearchResult(DcObject dco) {
        this.dco = dco;
    }
    
	public String getWorkId() {
		return workId;
	}

	public void setWorkId(String workId) {
		this.workId = workId;
	}

	public String getMainCoverId() {
		return mainCoverId;
	}

	public void setMainCoverId(String mainCoverId) {
		this.mainCoverId = mainCoverId;
	}

	public DcObject getDco() {
        return dco;
    }
}

/*
 *     Created by Angel Leon (@gubatron)
 *     Copyright (c) 2011-2026, FrostWire(R). All rights reserved.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.limewire.search.frostwire.search;

import org.limewire.search.frostwire.util.UrlUtils;

public final class SearchPerformerFactory {
    private SearchPerformerFactory() {
    }

    public static ISearchPerformer createSearchPerformer(
            long token,
            String keywords,
            SearchPattern pattern,
            CrawlingStrategy crawlingStrategy,
            int timeout) {
        return new SearchPerformer(token, keywords, UrlUtils.encode(keywords), pattern, crawlingStrategy, timeout);
    }
}

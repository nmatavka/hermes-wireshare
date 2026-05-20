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

import java.io.IOException;
import java.net.SocketTimeoutException;

public class SearchTimeoutException extends IOException {
    private final String performerName;
    private final String domain;
    private final String url;
    private final int timeoutMs;

    public SearchTimeoutException(String performerName, String domain, String url, int timeoutMs, SocketTimeoutException cause) {
        super(formatMessage(performerName, domain, url, timeoutMs), cause);
        this.performerName = performerName;
        this.domain = domain;
        this.url = url;
        this.timeoutMs = timeoutMs;
    }

    public SearchTimeoutException(String performerName, SocketTimeoutException cause) {
        this(performerName, "unknown", "unknown", 0, cause);
    }

    private static String formatMessage(String performerName, String domain, String url, int timeoutMs) {
        StringBuilder sb = new StringBuilder("Search timeout");
        if (timeoutMs > 0) {
            sb.append(" (").append(timeoutMs).append("ms)");
        }
        sb.append(" from performer: ").append(performerName);
        if (domain != null && !"unknown".equals(domain)) {
            sb.append(" (domain: ").append(domain).append(")");
        }
        if (url != null && !"unknown".equals(url)) {
            sb.append("\n  URL: ").append(truncateUrl(url, 100));
        }
        return sb.toString();
    }

    private static String truncateUrl(String url, int maxLength) {
        return url.length() <= maxLength ? url : url.substring(0, maxLength) + "...";
    }

    public static boolean isSearchTimeout(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        return throwable instanceof SocketTimeoutException ||
                throwable instanceof SearchTimeoutException ||
                isSearchTimeout(throwable.getCause());
    }

    public String getPerformerName() {
        return performerName;
    }

    public String getDomain() {
        return domain;
    }

    public String getUrl() {
        return url;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }
}

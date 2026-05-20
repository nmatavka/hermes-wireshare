/*
 *     Created by Angel Leon (@gubatron), Alden Torres (aldenml)
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

package org.limewire.search.frostwire.util;

import org.limewire.search.frostwire.util.http.HttpClient;
import org.limewire.search.frostwire.util.http.JdkHttpClient;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author gubatron
 * @author aldenml
 */
public class HttpClientFactory {
    private HttpClientFactory() {
    }

    public static HttpClient newInstance() {
        return new JdkHttpClient();
    }

    public static HttpClient getInstance(HttpContext context) {
        return new JdkHttpClient();
    }

    public enum HttpContext {
        SEARCH,
        DOWNLOAD,
        MISC
    }
}

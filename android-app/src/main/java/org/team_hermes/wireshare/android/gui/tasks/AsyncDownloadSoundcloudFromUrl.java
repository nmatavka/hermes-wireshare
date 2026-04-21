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

package org.team_hermes.wireshare.android.gui.tasks;

import android.content.Context;

import org.team_hermes.wireshare.android.R;
import org.team_hermes.wireshare.android.gui.activities.MainActivity;
import org.team_hermes.wireshare.android.gui.dialogs.ConfirmSoundcloudDownloadDialog;
import org.team_hermes.wireshare.android.gui.util.UIUtils;
import org.team_hermes.wireshare.android.gui.SoftwareUpdater;
import org.team_hermes.wireshare.android.util.SystemUtils;
import org.team_hermes.wireshare.search.soundcloud.SoundcloudUtils;
import org.team_hermes.wireshare.search.soundcloud.SoundcloudSearchResult;
import org.team_hermes.wireshare.util.HttpClientFactory;
import org.team_hermes.wireshare.util.Logger;
import org.team_hermes.wireshare.util.http.HttpClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aldenml
 * @author gubatron
 */
public final class AsyncDownloadSoundcloudFromUrl {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(AsyncDownloadSoundcloudFromUrl.class);

    public AsyncDownloadSoundcloudFromUrl(Context ctx, String soundcloudUrl) {
        SystemUtils.postToHandler(SystemUtils.HandlerThreadName.DOWNLOADER, () -> {
            List<SoundcloudSearchResult> results = doInBackground(soundcloudUrl);
            SystemUtils.postToUIThread(() -> onPostExecute(ctx, soundcloudUrl, results));
        });
    }

    private static List<SoundcloudSearchResult> doInBackground(final String soundcloudUrl) {
        List<SoundcloudSearchResult> results = new ArrayList<>();
        try {
            String url = soundcloudUrl;
            if (soundcloudUrl.contains("?in=")) {
                url = soundcloudUrl.substring(0, url.indexOf("?in="));
            }
            String clientId = SoftwareUpdater.getSoundCloudClientId();
            String appVersion = SoftwareUpdater.getSoundCloudAppVersion();
            String resolveURL = SoundcloudUtils.resolveUrl(url, clientId, appVersion);
            HttpClient client = HttpClientFactory.getInstance(HttpClientFactory.HttpContext.DOWNLOAD);
            String json = client.get(resolveURL, 10000);
            results = SoundcloudUtils.fromJson(json, true, clientId, appVersion);
        } catch (Throwable e) {
            LOG.error("AsyncDownloadSoundcloudFromUrl::doInBackground: Error downloading from Soundcloud", e);
        }
        return results;
    }

    private static void onPostExecute(Context ctx, final String soundcloudUrl, List<SoundcloudSearchResult> results) {
        if (ctx == null) {
            return;
        }
        if (results.isEmpty()) {
            UIUtils.showLongMessage(ctx, R.string.sorry_could_not_find_valid_download_location_at, soundcloudUrl);
            return;
        }

        MainActivity activity = (MainActivity) ctx;
        ConfirmSoundcloudDownloadDialog dlg = createConfirmListDialog(ctx, results);
        dlg.show(activity.getSupportFragmentManager());
    }

    private static ConfirmSoundcloudDownloadDialog createConfirmListDialog(Context ctx, List<SoundcloudSearchResult> results) {
        String title = ctx.getString(R.string.confirm_download);
        String whatToDownload = ctx.getString((results.size() > 1) ? R.string.playlist : R.string.track);
        String totalSize = UIUtils.getBytesInHuman(getTotalSize(results));
        String text = ctx.getString(R.string.are_you_sure_you_want_to_download_the_following, whatToDownload, totalSize);

        //AbstractConfirmListDialog
        return ConfirmSoundcloudDownloadDialog.newInstance(ctx, title, text, results);
    }

    private static long getTotalSize(List<SoundcloudSearchResult> results) {
        long totalSizeInBytes = 0;
        for (SoundcloudSearchResult sr : results) {
            totalSizeInBytes += sr.getSize();
        }
        return totalSizeInBytes;
    }
}

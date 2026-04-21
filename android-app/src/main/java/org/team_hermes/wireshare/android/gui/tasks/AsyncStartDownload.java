/*
 *     Created by Angel Leon (@gubatron), Alden Torres (aldenml),
 *  *            Marcelina Knitter (@marcelinkaaa)
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

import static org.team_hermes.wireshare.android.util.SystemUtils.postToHandler;
import static org.team_hermes.wireshare.android.util.SystemUtils.postToUIThreadAtFront;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import org.team_hermes.wireshare.android.BuildConfig;
import org.team_hermes.wireshare.android.R;
import org.team_hermes.wireshare.android.gui.dialogs.HandpickedTorrentDownloadDialogOnFetch;
import org.team_hermes.wireshare.android.gui.transfers.ExistingDownload;
import org.team_hermes.wireshare.android.gui.transfers.InvalidDownload;
import org.team_hermes.wireshare.android.gui.transfers.InvalidTransfer;
import org.team_hermes.wireshare.android.gui.transfers.TorrentFetcherDownload;
import org.team_hermes.wireshare.android.gui.transfers.TransferManager;
import org.team_hermes.wireshare.android.gui.util.UIUtils;
import org.team_hermes.wireshare.android.offers.Offers;
import org.team_hermes.wireshare.android.util.SystemUtils;
import org.team_hermes.wireshare.search.SearchResult;
import org.team_hermes.wireshare.search.torrent.TorrentCrawledSearchResult;
import org.team_hermes.wireshare.search.torrent.TorrentSearchResult;
import org.team_hermes.wireshare.transfers.BaseHttpDownload;
import org.team_hermes.wireshare.transfers.BittorrentDownload;
import org.team_hermes.wireshare.transfers.Transfer;
import org.team_hermes.wireshare.transfers.TransferState;
import org.team_hermes.wireshare.util.Logger;
import org.team_hermes.wireshare.util.Ref;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author gubatron
 * @author aldenml
 */
public class AsyncStartDownload {

    private static final Logger LOG = Logger.getLogger(AsyncStartDownload.class);

    public AsyncStartDownload(final Context ctx, final SearchResult sr, final String message) {
        //async(ctx, AsyncStartDownload::doInBackground, sr, message, AsyncStartDownload::onPostExecute);
        WeakReference<Context> ctxRef = Ref.weak(ctx);
        postToHandler(SystemUtils.HandlerThreadName.DOWNLOADER, () -> run(ctxRef, sr, message));
    }

    private void run(WeakReference<Context> ctxRef, final SearchResult sr, final String message) {
        LOG.info("AsyncStartDownload:run posted and now running to handler", true);
        try {
            if (!Ref.alive(ctxRef)) {
                Ref.free(ctxRef);
                return;
            }
            final Transfer transfer = doInBackground(ctxRef.get(), sr, message);
            if (transfer == null) {
                Ref.free(ctxRef);
                return;
            }
            postToUIThreadAtFront(() -> {
                if (!Ref.alive(ctxRef)) {
                    Ref.free(ctxRef);
                    return;
                }
                try {
                    onPostExecute(ctxRef.get(), sr, message, transfer);
                } catch (Throwable t) {
                    LOG.error(t.getMessage(), t);
                } finally {
                    Ref.free(ctxRef);
                }
            });
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    public AsyncStartDownload(final Context ctx, final SearchResult sr) {
        this(ctx, sr, null);
    }

    private static Transfer doInBackground(final Context ctx, final SearchResult sr, final String message) {
        Transfer transfer = null;
        try {
            // Check both old TorrentSearchResult interface and new CompositeFileSearchResult with isTorrent()
            boolean isTorrent = false;
            String torrentUrl = null;

            if (sr instanceof TorrentSearchResult && !(sr instanceof TorrentCrawledSearchResult)) {
                // Old architecture: TorrentSearchResult
                isTorrent = true;
                torrentUrl = ((TorrentSearchResult) sr).getTorrentUrl();
            } else if (sr instanceof org.team_hermes.wireshare.search.CompositeFileSearchResult) {
                // New architecture: CompositeFileSearchResult with torrent metadata
                org.team_hermes.wireshare.search.CompositeFileSearchResult csr = (org.team_hermes.wireshare.search.CompositeFileSearchResult) sr;
                if (csr.isTorrent()) {
                    isTorrent = true;
                    torrentUrl = csr.getTorrentUrl().orElse(null);
                }
            }

            if (isTorrent && torrentUrl != null) {
                transfer = TransferManager.instance().downloadTorrent(torrentUrl,
                        new HandpickedTorrentDownloadDialogOnFetch((AppCompatActivity) ctx, false), sr.getDisplayName());
            } else {
                transfer = TransferManager.instance().download(sr);
                if (!(transfer instanceof InvalidDownload)) {
                    if (ctx instanceof Activity) {
                        ((Activity) ctx).runOnUiThread(() -> {
                            try {
                                UIUtils.showTransfersOnDownloadStart(ctx);
                            } catch (Throwable t) {
                                if (BuildConfig.DEBUG) {
                                    throw t;
                                }
                                LOG.error("doInBackground() " + t.getMessage(), t);
                            }
                        });
                    }
                }
            }
        } catch (Throwable e) {
            LOG.warn("Error adding new download from result: " + sr, e);
            e.printStackTrace();
        }
        return transfer;
    }

    private static void onPostExecute(final Context ctx, final SearchResult sr, final String message, final Transfer transfer) {
        if (transfer != null) {
            if (!(transfer instanceof InvalidTransfer)) {
                TransferManager tm = TransferManager.instance();
                if (tm.isBittorrentDownloadAndMobileDataSavingsOn(transfer)) {
                    UIUtils.showLongMessage(ctx, R.string.torrent_transfer_enqueued_on_mobile_data);
                    ((BittorrentDownload) transfer).pause();
                } else {
                    if (tm.isBittorrentDownloadAndMobileDataSavingsOff(transfer)) {
                        UIUtils.showLongMessage(ctx, R.string.torrent_transfer_consuming_mobile_data);
                    }

                    if (message != null) {
                        UIUtils.showShortMessage(ctx, message);
                    }
                }
                UIUtils.showTransfersOnDownloadStart(ctx);
            } else if (!(transfer instanceof ExistingDownload)) {
                UIUtils.showLongMessage(ctx, ((InvalidTransfer) transfer).getReasonResId());
            }
        }
    }
}

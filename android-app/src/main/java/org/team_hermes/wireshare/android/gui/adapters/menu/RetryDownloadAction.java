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

package org.team_hermes.wireshare.android.gui.adapters.menu;

import android.content.Context;

import org.team_hermes.wireshare.android.R;
import org.team_hermes.wireshare.android.gui.transfers.InvalidTransfer;
import org.team_hermes.wireshare.android.gui.transfers.TorrentFetcherDownload;
import org.team_hermes.wireshare.android.gui.transfers.TransferManager;
import org.team_hermes.wireshare.android.gui.util.UIUtils;
import org.team_hermes.wireshare.android.gui.views.MenuAction;
import org.team_hermes.wireshare.transfers.Transfer;

/**
 * @author gubatron
 * @author aldenml
 * @author marcelinkaaa
 */
public class RetryDownloadAction extends MenuAction {

    private final Transfer transfer;

    public RetryDownloadAction(Context context, Transfer transfer) {
        super(context, R.drawable.contextmenu_icon_download, R.string.retry, UIUtils.getAppIconPrimaryColor(context));
        this.transfer = transfer;
    }

    @Override
    public void onClick(Context context) {
        TransferManager.instance().remove(transfer);

        if (transfer instanceof InvalidTransfer) {
            retryInvalidTransfer();
        } else if (transfer instanceof TorrentFetcherDownload) {
            retryTorrentFetcherDownload();
        }
    }

    private void retryTorrentFetcherDownload() {
        TorrentFetcherDownload tfd = (TorrentFetcherDownload) transfer;
        TransferManager.instance().downloadTorrent(
                tfd.getTorrentDownloadInfo().getTorrentUrl(),
                tfd.getTorrentFetcherListener(),
                tfd.getName());
    }

    private void retryInvalidTransfer() {
        InvalidTransfer it = (InvalidTransfer) transfer;
        TransferManager.instance().download(it.getSearchResult());
    }
}

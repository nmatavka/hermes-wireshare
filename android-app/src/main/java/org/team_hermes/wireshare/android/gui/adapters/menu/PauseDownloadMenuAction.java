/*
 *     Created by Angel Leon (@gubatron), Alden Torres (aldenml), Marcelina Knitter (@marcelinkaaa)
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
import org.team_hermes.wireshare.android.gui.util.UIUtils;
import org.team_hermes.wireshare.android.gui.views.MenuAction;
import org.team_hermes.wireshare.android.gui.views.TimerObserver;
import org.team_hermes.wireshare.transfers.BittorrentDownload;

/**
 * @author gubatron
 * @author aldenml
 * @author marcelinkaaa
 */
public final class PauseDownloadMenuAction extends MenuAction {

    private final BittorrentDownload download;

    public PauseDownloadMenuAction(Context context, BittorrentDownload download) {
        super(context, R.drawable.contextmenu_icon_pause_transfer, R.string.pause_torrent_menu_action, UIUtils.getAppIconPrimaryColor(context));
        this.download = download;
    }

    @Override
    public void onClick(Context context) {
        if (!download.isPaused()) {
            download.pause();
            if (context instanceof TimerObserver) {
                ((TimerObserver) context).onTime();
            }
        }
    }
}

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

package org.team_hermes.wireshare.android.gui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;

import org.team_hermes.wireshare.android.R;
import org.team_hermes.wireshare.android.core.ConfigurationManager;
import org.team_hermes.wireshare.android.core.Constants;
import org.team_hermes.wireshare.android.gui.NetworkManager;
import org.team_hermes.wireshare.android.gui.adapters.menu.CancelMenuAction;
import org.team_hermes.wireshare.android.gui.adapters.menu.CopyToClipboardMenuAction;
import org.team_hermes.wireshare.android.gui.adapters.menu.PauseDownloadMenuAction;
import org.team_hermes.wireshare.android.gui.adapters.menu.ResumeDownloadMenuAction;
import org.team_hermes.wireshare.android.gui.adapters.menu.SeedAction;
import org.team_hermes.wireshare.android.gui.adapters.menu.SendBitcoinTipAction;
import org.team_hermes.wireshare.android.gui.adapters.menu.SendFiatTipAction;
import org.team_hermes.wireshare.android.gui.transfers.UIBittorrentDownload;
import org.team_hermes.wireshare.android.gui.views.AbstractFragment;
import org.team_hermes.wireshare.bittorrent.BTEngine;
import org.team_hermes.wireshare.bittorrent.PaymentOptions;
import org.team_hermes.wireshare.transfers.TransferState;

/**
 * @author gubatron
 * @author aldenml
 * @author marcelinkaaa
 */
public final class TransferDetailFragment extends AbstractFragment {

    private UIBittorrentDownload uiBittorrentDownload;
    private MenuItem pauseResumeMenuItem;

    public TransferDetailFragment() {
        super(R.layout.fragment_transfer_detail);
    }

    public void setUiBittorrentDownload(UIBittorrentDownload uiBittorrentDownload) {
        this.uiBittorrentDownload = uiBittorrentDownload;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.fragment_transfer_detail_menu, menu);
                pauseResumeMenuItem = menu.findItem(R.id.fragment_transfer_detail_menu_pause_resume_seed);
            }

            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                if (uiBittorrentDownload == null) {
                    return;
                }
                updatePauseResumeSeedMenuAction();
                MenuItem fiatMenuItem = menu.findItem(R.id.fragment_transfer_detail_menu_donate_fiat);
                MenuItem bitcoinMenuItem = menu.findItem(R.id.fragment_transfer_detail_menu_donate_bitcoin);
                if (!uiBittorrentDownload.hasPaymentOptions()) {
                    fiatMenuItem.setVisible(false);
                    bitcoinMenuItem.setVisible(false);
                } else {
                    PaymentOptions po = uiBittorrentDownload.getPaymentOptions();
                    fiatMenuItem.setVisible(po.paypalUrl != null);
                    bitcoinMenuItem.setVisible(po.bitcoin != null);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                Activity activity = getActivity();
                int itemId = item.getItemId();
                PaymentOptions paymentOptions = uiBittorrentDownload.getPaymentOptions();
                if (itemId == R.id.fragment_transfer_detail_menu_delete) {
                    new CancelMenuAction(activity, uiBittorrentDownload, true, true).onClick(activity);
                } else if (itemId == R.id.fragment_transfer_detail_menu_pause_resume_seed) {
                    if (isPausable()) {
                        new PauseDownloadMenuAction(activity, uiBittorrentDownload).onClick(activity);
                    } else if (isSeedable()) {
                        new SeedAction(activity, uiBittorrentDownload).onClick(activity);
                    } else if (isResumable()) {
                        new ResumeDownloadMenuAction(activity, uiBittorrentDownload, R.string.resume_torrent_menu_action).onClick(activity);
                    }
                    updatePauseResumeSeedMenuAction();
                } else if (itemId == R.id.fragment_transfer_detail_menu_clear) {
                    new CancelMenuAction(activity, uiBittorrentDownload, false, false).onClick(activity);
                } else if (itemId == R.id.fragment_transfer_detail_menu_copy_magnet) {
                    new CopyToClipboardMenuAction(activity,
                            R.drawable.contextmenu_icon_magnet,
                            R.string.transfers_context_menu_copy_magnet,
                            R.string.transfers_context_menu_copy_magnet_copied,
                            uiBittorrentDownload.magnetUri() + BTEngine.getInstance().magnetPeers()
                    ).onClick(activity);
                } else if (itemId == R.id.fragment_transfer_detail_menu_copy_infohash) {
                    new CopyToClipboardMenuAction(activity,
                            R.drawable.contextmenu_icon_copy,
                            R.string.transfers_context_menu_copy_infohash,
                            R.string.transfers_context_menu_copy_infohash_copied,
                            uiBittorrentDownload.getInfoHash()
                    ).onClick(activity);
                } else if (itemId == R.id.fragment_transfer_detail_menu_donate_fiat) {
                    new SendFiatTipAction(activity, paymentOptions.paypalUrl).onClick(activity);
                } else if (itemId == R.id.fragment_transfer_detail_menu_donate_bitcoin) {
                    new SendBitcoinTipAction(activity, paymentOptions.bitcoin).onClick(activity);
                } else {
                    return false;
                }
                return true;
            }
        }, getViewLifecycleOwner());
    }

    private boolean isPausable() {
        return uiBittorrentDownload != null && uiBittorrentDownload.getState() != TransferState.FINISHED && (!uiBittorrentDownload.isPaused() || uiBittorrentDownload.getState() == TransferState.SEEDING);
    }

    private boolean isResumable() {
        if (isPausable()) {
            return false;
        }
        boolean wifiIsUp = NetworkManager.instance().isDataWIFIUp();
        boolean bittorrentOnMobileData = !ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_NETWORK_USE_WIFI_ONLY);
        return wifiIsUp || bittorrentOnMobileData;
    }

    private boolean isSeedable() {
        return uiBittorrentDownload != null && uiBittorrentDownload.getState() == TransferState.FINISHED;
    }

    public void updatePauseResumeSeedMenuAction() {
        if (pauseResumeMenuItem == null) {
            return;
        }
        if (isPausable()) {
            pauseResumeMenuItem.setIcon(R.drawable.action_bar_pause);
        }
        if (isResumable()) {
            pauseResumeMenuItem.setIcon(R.drawable.action_bar_resume);
            if (isSeedable()) {
                pauseResumeMenuItem.setIcon(R.drawable.action_bar_seed);
            }
        }
    }

}

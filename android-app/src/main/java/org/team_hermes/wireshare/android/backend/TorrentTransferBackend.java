package org.team_hermes.wireshare.android.backend;

import org.team_hermes.wireshare.android.gui.transfers.TransferManager;
import org.team_hermes.wireshare.transfers.BittorrentDownload;
import org.team_hermes.wireshare.transfers.Transfer;

import java.util.ArrayList;
import java.util.List;

public final class TorrentTransferBackend implements TransferBackend {
    public static final String ID = "torrent";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<TransferItemModel> getTransfers() {
        List<Transfer> transfers = TransferManager.instance().getTransfers();
        List<TransferItemModel> models = new ArrayList<>(transfers.size());
        for (Transfer transfer : transfers) {
            String protocol = transfer instanceof BittorrentDownload ? "bittorrent" : "http";
            models.add(new TransferItemModel(
                    ID,
                    protocol,
                    transfer.getDisplayName(),
                    String.valueOf(transfer.getState()),
                    transfer.getSize(),
                    transfer.getProgress()
            ));
        }
        return models;
    }
}

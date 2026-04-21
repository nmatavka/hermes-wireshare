package org.team_hermes.wireshare.android.backend;

import java.util.List;

public interface TransferBackend {
    String id();

    boolean isEnabled();

    List<TransferItemModel> getTransfers();
}

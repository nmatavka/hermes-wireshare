package org.team_hermes.wireshare.android.backend;

import java.util.Collections;
import java.util.List;

public final class GnutellaTransferBackend implements TransferBackend {
    public static final String ID = "gnutella";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public List<TransferItemModel> getTransfers() {
        return Collections.emptyList();
    }
}

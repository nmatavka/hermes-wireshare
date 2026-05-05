package org.team_hermes.wireshare.android.backend;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AndroidBackends {
    private static final List<SearchBackend> SEARCH_BACKENDS = Collections.unmodifiableList(Arrays.asList(
            new TorrentSearchBackend(),
            new GnutellaSearchBackend()
    ));

    private static final List<TransferBackend> TRANSFER_BACKENDS = Collections.unmodifiableList(Arrays.asList(
            new TorrentTransferBackend(),
            new GnutellaTransferBackend()
    ));

    private AndroidBackends() {
    }

    public static List<SearchBackend> searchBackends() {
        return SEARCH_BACKENDS;
    }

    public static List<TransferBackend> transferBackends() {
        return TRANSFER_BACKENDS;
    }
}

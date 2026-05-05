package org.team_hermes.wireshare.android.backend;

public final class SearchResultModel {
    public final String backendId;
    public final String protocol;
    public final String title;
    public final String source;
    public final String detailsUrl;
    public final long size;

    public SearchResultModel(String backendId,
                             String protocol,
                             String title,
                             String source,
                             String detailsUrl,
                             long size) {
        this.backendId = backendId;
        this.protocol = protocol;
        this.title = title;
        this.source = source;
        this.detailsUrl = detailsUrl;
        this.size = size;
    }
}

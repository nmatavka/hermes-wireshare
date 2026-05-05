package org.limewire.ui.compose

enum class SearchSortMode {
    RELEVANCE,
    TYPE,
    NAME,
    FROM,
    FILENAME,
    EXTENSION,
    SIZE,
    SOURCES,
    FRIENDS,
    LENGTH,
    QUALITY,
    BITRATE,
    TRACK,
    ARTIST,
    ALBUM,
    GENRE,
    YEAR,
    AUTHOR,
    COMPANY,
    PLATFORM,
    DESCRIPTION,
    FILES,
    TRACKERS
}

enum class SearchColumn {
    NAME,
    TYPE,
    FROM,
    FILENAME,
    EXTENSION,
    SIZE,
    SOURCES,
    FRIENDS,
    LENGTH,
    QUALITY,
    BITRATE,
    TRACK,
    ARTIST,
    ALBUM,
    GENRE,
    YEAR,
    AUTHOR,
    COMPANY,
    PLATFORM,
    DESCRIPTION,
    FILES,
    TRACKERS
}

enum class LibrarySortMode {
    NAME,
    FILENAME,
    EXTENSION,
    TYPE,
    SIZE,
    ACTIVITY,
    HITS,
    UPLOADS,
    UPLOAD_ATTEMPTS,
    UPDATED,
    LOCATION,
    LENGTH,
    BITRATE,
    TRACK,
    ARTIST,
    ALBUM,
    GENRE,
    YEAR,
    AUTHOR,
    COMPANY,
    PLATFORM,
    DESCRIPTION,
    FILES,
    TRACKERS
}

enum class LibraryColumn {
    NAME,
    FILENAME,
    EXTENSION,
    TYPE,
    SIZE,
    ACTIVITY,
    HITS,
    UPLOADS,
    UPLOAD_ATTEMPTS,
    UPDATED,
    LOCATION,
    LENGTH,
    BITRATE,
    TRACK,
    ARTIST,
    ALBUM,
    GENRE,
    YEAR,
    AUTHOR,
    COMPANY,
    PLATFORM,
    DESCRIPTION,
    FILES,
    TRACKERS
}

enum class TransferFilterMode {
    ALL,
    ACTIVE,
    FINISHED,
    STALLED
}

enum class DownloadSortMode {
    ORDER_ADDED,
    TIME_LEFT,
    FILE_TYPE,
    EXTENSION,
    STATUS,
    NAME,
    PROGRESS,
    RATE,
    SOURCES
}

enum class DownloadColumn {
    NAME,
    ORDER_ADDED,
    TIME_LEFT,
    FILE_TYPE,
    EXTENSION,
    STATUS,
    PROGRESS,
    RATE,
    SOURCES
}

enum class UploadSortMode {
    ORDER_STARTED,
    TIME_LEFT,
    FILE_TYPE,
    EXTENSION,
    USER_NAME,
    STATUS,
    NAME,
    UPLOADED,
    RATE,
    PEERS
}

enum class UploadColumn {
    NAME,
    ORDER_STARTED,
    TIME_LEFT,
    FILE_TYPE,
    EXTENSION,
    USER_NAME,
    STATUS,
    UPLOADED,
    RATE,
    PEERS
}

package org.limewire.core.settings;

import org.limewire.setting.BooleanSetting;
import org.limewire.setting.FloatSetting;
import org.limewire.setting.IntSetting;
import org.limewire.setting.LongSetting;
import org.limewire.setting.StringArraySetting;
import org.limewire.setting.StringSetting;

/**.
 * Settings for filters
 */
public class FilterSettings extends LimeProps {
    
    private FilterSettings() {}

    public static final BooleanSetting USE_NETWORK_FILTER =
        FACTORY.createBooleanSetting("USE_NETWORK_FILTER", true);
    
    /**
     * Sets whether or not search results including "adult content" are
     * banned in What's New queries.
     */
    public static final BooleanSetting FILTER_WHATS_NEW_ADULT =
        FACTORY.createBooleanSetting("FILTER_WHATS_NEW_ADULT", true);
    
    /**
     * Sets whether or not search results including "adult content" are
     * banned.
     */
    public static final BooleanSetting FILTER_ADULT =
        FACTORY.createBooleanSetting("FILTER_ADULT", false);
    
    /**
     * Sets whether or not known spam and malware URNs are banned.
     */
    public static final BooleanSetting FILTER_URNS =
        FACTORY.createBooleanSetting("FILTER_URNS", true);
    
    /**
     * An array of URNs that should not be displayed (local setting).
     */
    public static final StringArraySetting FILTERED_URNS_LOCAL = 
        FACTORY.createStringArraySetting("FILTERED_URNS_LOCAL", new String[0]);
    
    /**
     * An array of URNs that should not be displayed (remote setting).
     */
    public static final StringArraySetting FILTERED_URNS_REMOTE =
        FACTORY.createRemoteStringArraySetting("FILTERED_URNS_REMOTE",
                new String[0]);
    
    /**
     * Sets whether or not results with filtered URNs are considered spam. 
     */
    public static final BooleanSetting FILTERED_URNS_ARE_SPAM =
        FACTORY.createRemoteBooleanSetting("FILTERED_URNS_ARE_SPAM", true);

    /**
     * An array of base32-encoded hashes of spam templates.
     */
    public static final StringArraySetting SPAM_TEMPLATES =
        FACTORY.createRemoteStringArraySetting("SPAM_TEMPLATES",
                new String[0]);

    /**
     * An array of approximate sizes of spam files.
     */
    public static final StringArraySetting SPAM_SIZES =
        FACTORY.createRemoteStringArraySetting("SPAM_SIZES",
                new String[0]);

    /**
     * Sets whether or not duplicate pings and queries are filtered.
     */
    public static final BooleanSetting FILTER_DUPLICATES =
        FACTORY.createBooleanSetting("FILTER_DUPLICATES", true);
    
    /**
     * The size of the <code>RepetitiveQueryFilter</code>: higher values make
     * the filter more sensitive, 0 disables it entirely.
     */
    public static final IntSetting REPETITIVE_QUERY_FILTER_SIZE =
        FACTORY.createRemoteIntSetting("REPETITIVE_QUERY_FILTER_SIZE", 10);

    /**
     * Sets whether or not greedy queries are filtered.
     */
    public static final BooleanSetting FILTER_GREEDY_QUERIES =
        FACTORY.createBooleanSetting("FILTER_GREEDY_QUERIES", true);
    
    /**
     * An array of IP addresses that the user has banned.
     */
    public static final StringArraySetting BLACK_LISTED_IP_ADDRESSES =
        FACTORY.createStringArraySetting("BLACK_LISTED_IP_ADDRESSES", new String[0]);
    
    /**
     * An array of IP addresses that the user has allowed.
     */  
    public static final StringArraySetting WHITE_LISTED_IP_ADDRESSES =
        FACTORY.createStringArraySetting("WHITE_LISTED_IP_ADDRESSES", new String[0]);
    
    /**
     * An array of words that the user has banned from appearing in
     * search results.
     */
    public static final StringArraySetting BANNED_WORDS =
        FACTORY.createStringArraySetting("BANNED_WORDS",
                new String[]{".wmv", ".wma", ".mov", "accelerator", "apple", "astalavista",
                "banged", "bibcam", "browse", "corrupt", "crack", "cumshot", "demonoid",
                "diaper", "dogging", "downloader", "downloads", "dvdrig", "efree", "fans",
                "ffmm", "fixed", "gangbang", "gay", "gift", "gorillaz", "humiliation",
                "incomplete", "ipod", "isohunt", "keygen", "keymaker", "limewire",
                "marketing", "muffdive", "norton", "orgy", "original", "patch", "popsicle",
                "pregnant", "preview", "pussy", "recipe", "rmx", "samsung", "screens",
                "serialz", "shorties", "spreading", "sucking", "titfucked", "toolbar",
                ".tor", "torrent", "Almighty", "verified", "vertor", "candide", ".zip",
                ".rar", ".qt", " (Greatest Hits)", " (High Quality)", " (hi def).mov",
                " (new album).wma", " (Radio Edit)", " (uploaded.by.wahpee)",
                " black_x).zip", " free music.wma", " hot new track.mp3", " ORiON).zip",
                " PBAY", " PBR.", " Radio EDIT.wma", " rare record.wma", " SND].zip",
                " TSRh.zip", " torrents download.zip", " Uncut Track",
                " unreleased studio edition.", " unreleased version.snd", " Verified ",
                " [upload.by", "( NEW ALBUM )", "()", "(Bittorrent complete)", "(BuBanee)",
                "(Complete).wma", "(complete", "(confirmed", "(confirmed complete)",
                "(conteo David_PL99)", "(EXPLICIT)", "(EZTV)  ", "(Full Version)",
                "(GOOD).", "(Good Copy)", "(Greatest Hits Mix)", "(good audio)",
                "(hd).mov", "(hi def)", "(including crack by",
                "(instrumental version).wma", "(jute)", "(keygen inside).zip",
                "(keygen is included).zip", "(kimmiboi)", "(less_mucca)", "(NYC ReMix)",
                "(new album).wma", "(Radio Edit).mov", "(unreleased live record).mp3",
                ")(fix).zip", ".and", ".Horrorspoke", ".r00", ".use", ".wav",
                ".yuridia.zip", "10yo", "11yo", "256k stereo.mp3", "2yo", "3yo", "4yo",
                "5yo", "5000passwords", "6yo", "7yo", "8yo", "9yo", "Anuzbitt", "ARESTRA",
                "aardbob", "adultfriendfinder", "adultpeak", "allcoolmusic", "Azoogle",
                "BEST SONG EVER", "BestQualiti", "bestbartersoftware", "BRRip.mov",
                "collegemegaparties", "complete.mov", "Coolhousing", "DivXNL",
                "downloadmedia", "edirectclub", "esaveclub", "Evar.torrent",
                "edition}.zip", "eXe.torrent", "extratorrent", "FFF.zip", "freeclub",
                "freemusic", "Gnoozle", "GoDaddy", "goudkov", "Greatest Hits Remix",
                "himegimi", "i2cams", "IPAD2", "IWANNADOWNLOAD", "ifreeclub", "iphone",
                "isohunt >", "isohunt..diabolic.zip", "Jironimo", "joethehob0",
                "justin-be ", "justin-girl ", "Kingpass", "keygen for [", "keymaker by ",
                "live.wma", "mininova", "monova", "mp3baby", "myaffiliatead", "p2pads",
                "p2pblast", "PBAY.torrent", "PBR", "PBR.torrent", "patched", "piratebay",
                "puzzledesktop", "quality", "quibus", "RadarSync", "RedDragon.rar",
                "redlightcenter", "reesexyworld", "SaveNow", "screamsaver", "seonomad",
                "Serialy", "ShareAccelerator", "sharewarning", "Sponsored", "sudoku",
                "sumotracker", "TechKiing", "teddycash", "thebestofnet", "Track.mov",
                "kickasstorrent", "torrentdownloads", "torrentfreak", "torrentfunk",
                "torrenthound", "torrentmatrix", "torrentpond", "torrentportal",
                "torrentpump", "torrentsource", "torrentzap", "touslesfilmsx", "Verified ",
                "Victor.torrent", "VersaTel", "", "whentoaskforhelp", "wonderfultracks",
                "ZAPSHARES", "zerotracker", "zibbik", "zip.avi", "zipam.", "[cracked by ",
                "[downloader]", "[FLV].zip", "[fixed].zip", "[instrumentals].zip",
                "[iso.hunt]  ", "[jute]", "[keygen]", "[nova]  ", "[rarbg][",
                "[torrents.to]", "[UPLOAD BY ", "[uploaded", "[uploaded by Mike]  ",
                "[valid", "[valid till ", "[very good quality]", "[wifey]", "[xc] ",
                "[y].zip", "] .rar", "][1x07].zip", "][fixed]", "][keygen]", "][myth]",
                "][orchestra]", "][prestige].zip", "{fixed}", "{full version}.torrent",
                "frostwire", "{original}.torrent", "{oui}.zip"});
    
    /**
     * An array of extensions that the user has banned from appearing in
     * search results.
     */
    public static final StringArraySetting BANNED_EXTENSIONS =
        FACTORY.createStringArraySetting("BANNED_EXTENSIONS",
                new String[]{".asf", ".asx", ".au", ".htm", ".html", ".mht", ".vbs",
                ".wax", ".wm", ".wma", ".wmd", ".wmv", ".wmx", ".wmz", ".wvx"});
    
    /**
     * Whether to filter queries containing hashes.
     */
    public static final BooleanSetting FILTER_HASH_QUERIES =
        FACTORY.createBooleanSetting("FILTER_HASH_QUERIES_2", true);
    
    public static final IntSetting MIN_MATCHING_WORDS =
        FACTORY.createRemoteIntSetting("MIN_MATCHING_WORDS",0);
    
    /**
     * An array of IP addresses that LimeWire will respond to.  
     */
    public static final StringArraySetting CRAWLER_IP_ADDRESSES =
        FACTORY.createRemoteStringArraySetting("CRAWLER_IPS", new String[]{"*.*.*.*"});
    
    /**
     * An array of hostile IP addresses.  
     */
    public static final StringArraySetting HOSTILE_IPS =
        FACTORY.createStringArraySetting("HOSTILE_IPS_2", new String[] {
                "1.160.204.*", "1.160.232.*", "1.161.218.*", "1.161.88.*", "1.162.138.*",
                "1.162.54.*", "1.164.0.0/16", "1.168.15.*", "1.169.199.*", "1.171.0.0/16",
                "1.172.188.*", "1.173.105.*", "1.33.112.*", "1.34.9.*", "12.38.31.216/29", "14.201.87.102",
                "101.141.98.*", "103.224.131.*", "106.174.1.*", "108.185.184.*", "108.30.181.*",
                "111.240.0.0/12", "112.120.210.*", "114.24.0.0/14", "114.25.82.*", "114.26.19.*",
                "114.26.65.*", "114.27.196.*", "114.27.86.*", "114.36.0.0/15", "114.40.0.0/16",
                "114.42.0.0/15", "114.47.0.0/16", "114.48.0.0/16", "114.51.0.0/16",
                "115.165.216.*", "116.118.170.*", "117.55.0.0/16", "118.160.0.0/16", "118.168.0.0/15",
                "118.232.0.0/15", "119.238.154.229", "119.246.231.*", "119.247.58.*",
                "12.1.42.0/23", "12.197.204.*", "12.197.253.*", "120.137.198.*",
                "120.28.8.16/28", "120.75.19.*", "121.1.52.*", "121.3.31.*", "121.54.0.0/22",
                "121.54.64.0/22", "121.91.91.*", "122.254.62.*", "123.202.135.*",
                "123.98.128.0/20", "124.212.165.66", "124.24.230.0/23", "124.97.37.*",
                "125.0.0.0/14", "125.205.100.0/22", "125.230.0.0/16", "125.30.110.*",
                "125.30.112.*", "125.60.128.0/17", "128.108.0.0/16", "129.47.0.0/16",
                "130.117.0.0/16", "131.104.0.0/16", "134.129.0.0/16", "14.38.138.113",
                "142.164.0.0/16", "142.166.0.0/15", "142.177.136.0/22", "142.177.184.0/21",
                "142.177.236.0/22", "142.177.90.*", "142.204.87.*", "144.26.129.*", "147.186.0.0/16",
                "147.197.190.*", "152.9.102.*", "153.31.0.0/16", "153.181.47.*", "154.45.216.*",
                "156.34.240.0/22", "158.103.0.*", "159.253.128.0/19", "161.31.226.*",
                "161.31.240.*", "162.23.0.0/16", "162.33.0.0/16", "162.45.0.0/16", "164.112.0.0/16",
                "164.128.37.*", "164.128.38.0/23", "164.128.40.0/21", "164.128.48.0/20",
                "164.128.64.0/18", "164.141.0.0/16", "165.142.0.0/16", "165.187.0.0/16",
                "166.64.0.0/16", "168.152.0.0/16", "168.215.140.0/23", "173.192.155.*",
                "172.250.152.*", "173.178.149.*", "173.192.155.*", "173.193.51.*", "173.193.64.0/19",
                "173.193.103.*", "173.45.64.0/18", "174.101.41.84", "174.129.0.0/16", "174.131.182.240",
                "174.132.0.0/15", "174.136.192.0/18", "174.142.128.0/17", "174.32.189.*",
                "174.34.128.0/18", "174.36.61.162", "174.88.1.234", "176.32.83.90", "180.10.85.83",
                "180.10.93.211", "182.235.97.*", "183.178.239.*", "184.172.0.0/16", "180.54.147.140",
                "184.173.0.0/17", "184.173.128.0/18", "184.173.216.0/21", "184.72.0.0/15",
                "184.75.212.0/23", "184.75.214.*", "184.75.221.*", "184.75.223.*", "184.173.143.0/27",
                "188.142.66.5", "188.165.220.*", "188.165.230.*", "188.178.233.55",
                "189.13.147.*", "189.13.36.0/22", "192.190.61.*", "192.190.66.*", "192.95.225.*",
                "193.121.83.*", "193.164.132.0/23", "193.252.228.*", "193.6.115.*", "194.179.107.*",
                "194.89.205.*", "195.162.203.*", "195.210.194.*", "195.229.230.*", "195.8.182.*",
                "196.12.244.128/26", "196.40.10.*", "198.14.228.0/23", "199.120.31.*", "198.190.209.*",
                "199.127.249.*", "199.127.253.*", "200.100.209.*", "200.148.38.*",
                "200.32.196.32/27", "201.157.14.0/23", "201.8.117.*", "201.83.118.*", "201.9.128.0/17",
                "202.208.32.0/19", "203.111.232.0/21", "203.121.250.*", "203.25.230.0/23",
                "203.82.79.96/28", "203.82.91.96/28", "203.84.170.*", "203.87.176.0/20",
                "203.87.192.0/20", "204.10.88.0/21", "204.11.216.0/21", "204.111.0.0/16",
                "204.13.164.0/22", "204.236.128.0/17", "204.51.128.0/17", "204.52.215.*",
                "204.8.32.0/22", "205.139.208.0/22", "205.144.218.*", "205.146.0.0/16",
                "205.177.0.0/16", "205.209.128.0/18", "205.211.145.*", "205.229.233.*",
                "205.252.0.0/16", "206.108.253.*", "206.123.249.*", "206.123.24.*", "206.132.0.0/18",
                "206.161.0.0/16", "206.188.129.*", "206.193.224.0/20", "206.220.174.0/23",
                "206.222.0.0/19", "206.225.103.*", "207.43.55.*", "206.48.0.*", "207.150.176.0/20",
                "207.171.0.0/18", "207.176.0.0/17", "207.182.128.0/19", "207.189.232.*",
                "207.210.64.0/18", "207.226.0.0/16", "207.248.32.0/20", "207.99.0.0/18",
                "208.100.0.0/18", "208.101.0.0/18", "208.101.82.*", "208.103.122.160/29",
                "208.107.164.*", "208.109.0.0/16", "208.116.0.0/18", "208.122.0.0/18",
                "208.122.192.0/19", "208.53.128.0/18", "208.54.240.0/20", "208.64.24.0/21",
                "208.66.72.0/21", "208.88.224.0/22", "208.9.112.0/21", "208.93.0.0/21",
                "208.98.0.0/18", "208.99.192.0/19", "208.99.64.0/20", "209.123.0.0/16",
                "209.145.88.*", "209.190.0.0/17", "209.195.128.0/18", "209.200.0.0/18",
                "209.205.246.0/23", "209.237.224.0/19", "209.51.160.0/19", "209.51.192.0/19",
                "209.59.110.*", "209.8.0.0/15", "209.87.248.*", "209.97.192.0/19", "210.171.146.48",
                "210.228.60.*", "211.1.219.*", "211.18.159.171", "212.120.109.*", "212.19.195.*",
                "212.76.37.*", "212.31.111.*","213.121.151.*", "213.140.0.0/19",
                "213.167.96.*", "213.22.162.*", "213.254.232.0/22", "213.47.248.0/22",
                "216.105.184.0/22", "216.118.64.0/18", "216.130.160.0/19",
                "216.130.176.0/20", "216.130.64.0/19", "216.139.208.0/20", "216.139.224.0/19",
                "216.14.112.0/20", "216.151.128.0/19", "216.158.128.0/19", "216.159.201.*",
                "216.169.96.0/19", "216.17.100.0/22", "216.17.104.0/21", "216.171.176.0/20",
                "216.174.143.*", "216.18.192.0/19", "216.205.217.*", "216.218.128.0/17",
                "216.221.96.*", "216.230.150.*", "216.230.230.38/31", "216.240.128.0/19",
                "216.243.0.0/19", "216.25.240.0/20", "216.255.176.0/20", "216.37.148.*", "216.37.237.*",
                "216.46.133.*", "216.66.0.0/18", "216.66.64.0/19", "216.67.224.0/19",
                "216.69.164.0/22", "216.7.80.0/20", "216.84.49.*", "216.86.144.0/20", "217.171.128.0/23",
                "217.239.2.2", "218.108.16.*", "218.222.250.*", "218.223.197.*", "219.107.128.*",
                "220.128.0.0/12", "220.129.213.*", "219.84.0.0/16", "220.136.0.0/16",
                "220.142.0.0/15", "222.73.192.0/18", "223.143.46.*", "24.106.140.*", "24.114.252.*",
                "24.138.128.0/18", "24.172.68.*", "24.213.149.*", "24.224.100.123", "24.227.222.*",
                "24.229.179.*", "24.231.64.0/19", "24.65.77.*", "24.76.95.*", "24.80.116.*",
                "36.224.0.0/12", "38.104.0.0/14", "38.109.0.0/16", "38.110.0.0/15", "38.96.0.0/13",
                "4.42.244.*","41.221.16.0/20", "50.18.227.125", "50.196.56.94", "50.22.128.0/17",
                "50.22.158.128/28", "50.22.186.0/28", "50.22.186.16/29", "50.22.64.0/20", "50.23.0.0/17",
                "50.48.18.*", "50.58.238.*", "50.97.156.*", "58.168.29.*", "58.188.13.*", "58.88.42.*",
                "59.147.133.*", "59.147.135.*", "59.84.123.250", "60.190.220.0/22", "61.194.157.198",
                "61.194.158.108", "61.63.32.0/19", "63.138.0.0/15", "63.216.0.0/14",
                "63.246.128.0/19", "64.111.192.0/19", "64.120.0.0/17", "64.124.0.0/15",
                "64.150.2.0/27", "64.20.32.0/19", "64.203.191.*", "64.208.0.0/16", "64.209.0.0/17",
                "64.21.0.0/17", "64.21.128.0/18", "64.210.128.0/19", "64.213.84.0/22",
                "64.231.202.*", "64.247.0.0/18", "64.25.180.*", "64.251.0.0/19",
                "64.27.0.0/19", "64.34.0.0/16", "64.40.96.0/19", "64.46.32.0/19",
                "64.5.64.0/18", "64.56.64.0/21", "64.59.64.0/18", "64.6.132.*",
                "64.62.128.0/17", "64.71.128.0/18", "64.72.112.0/20", "64.89.16.0/20",
                "64.92.224.0/20", "65.19.128.0/18", "65.199.18.*", "65.206.51.*",
                "65.246.22.*", "65.254.32.0/19", "65.49.0.0/18", "65.56.0.0/14",
                "65.72.0.0/16", "65.98.0.0/17", "66.103.32.0/19", "66.109.16.0/20",
                "66.135.32.0/19", "66.154.96.0/19", "66.155.211.*", "66.160.128.0/18",
                "66.160.192.0/20", "66.165.205.*", "66.171.74.0/23", "66.177.120.193", "66.199.176.0/23",
                "66.207.254.*", "66.211.0.0/20", "66.212.128.0/19", "66.212.224.0/19",
                "66.216.30.0/23", "66.216.56.0/21", "66.220.0.0/19", "66.232.96.0/19",
                "66.240.192.0/18", "66.246.0.0/16", "66.254.96.0/19", "66.29.0.0/17",
                "66.37.48.0/20", "66.45.224.0/19", "66.63.160.0/19", "66.79.160.0/19",
                "66.90.64.0/18", "67.159.0.0/18", "67.18.0.0/15", "67.196.0.0/16",
                "67.201.0.0/18", "67.202.0.0/18", "67.205.64.0/18", "67.209.224.0/20",
                "67.213.215.177", "67.219.96.0/19", "67.55.64.0/18", "67.56.0.0/15",
                "67.93.262.*", "68.178.128.0/17", "68.180.0.0/22", "68.184.56.170", "68.56.88.51",
                "69.10.128.0/19", "69.10.32.0/19", "69.146.145.*", "69.147.224.0/19",
                "69.171.160.0/21", "69.171.171.*", "69.4.225.*", "69.4.228.84",
                "69.41.160.0/19", "69.42.128.0/19", "69.42.64.0/19", "69.46.0.0/19",
                "69.50.160.0/19", "69.56.128.0/17", "69.57.160.0/19", "69.59.16.0/20",
                "69.64.64.0/19", "69.66.252.*", "69.72.128.0/17", "69.85.192.0/18",
                "69.9.160.0/19", "70.15.80.0/21", "70.30.239.145", "70.32.32.0/19",
                "70.38.38.*", "70.38.54.*", "70.38.64.0/18", "70.47.0.0/16", "70.49.239.*",
                "70.69.180.*", "70.83.206.27", "70.84.0.0/14", "70.99.0.0/16",
                "71.197.241.122", "71.214.186.*", "71.219.37.68", "71.238.152.*",
                "71.240.88.*", "71.6.128.0/17", "71.68.34.*", "72.11.128.0/19",
                "72.158.46.*", "72.16.214.*", "72.167.0.0/16", "72.172.64.0/19",
                "72.174.87.0/27", "72.22.0.0/19", "72.22.192.0/20", "72.242.239.*",
                "72.249.170.0/23", "72.35.224.0/20", "72.36.128.0/17", "72.37.128.0/17",
                "72.44.32.0/19", "72.46.128.0/19", "72.5.222.243", "72.51.192.*",
                "72.51.32.0/20", "72.52.64.0/18", "74.110.99.*", "74.13.181.*",
                "74.194.71.*", "74.195.51.*", "74.206.224.0/19", "74.208.0.0/18",
                "74.216.0.0/16", "74.222.0.0/20", "74.43.221.*", "74.52.0.0/14",
                "74.54.0.0/16", "74.60.158.*", "74.63.64.0/19", "74.79.242.*",
                "75.101.0.0/17", "75.101.128.0/17", "75.111.97.*", "75.125.0.0/16",
                "75.126.0.0/16", "75.164.67.*", "75.209.52.0/22", "75.40.34.169",
                "76.114.35.182", "76.164.192.0/19", "76.164.224.0/20", "76.23.189.227",
                "76.74.128.0/17", "76.76.0.0/20", "77.245.48.0/20", "78.111.64.0/20",
                "78.129.128.0/17", "78.49.18.0/23", "79.114.230.*", "79.13.68.*",
                "80.67.3.14", "80.80.20.*", "81.152.104.*", "81.152.16.*", "81.169.128.0/20",
                "81.208.64.0/18", "81.23.32.0/19", "81.241.136.*", "81.4.78.*", "82.132.136.192/26",
                "82.208.40.0/22", "83.110.232.*", "83.142.224.0/21", "83.78.155.*",
                "83.79.152.*", "83.79.188.*", "84.196.11.75", "84.196.82.177",
                "85.17.0.0/16", "85.176.128.0/18", "85.18.0.0/16", "85.214.0.0/15",
                "85.25.0.0/16", "85.88.0.0/19", "85.92.156.0/22", "86.96.226.*",
                "87.117.192.0/18", "87.236.192.0/21", "87.255.32.0/19", "88.191.0.0/16",
                "88.246.142.219", "88.85.64.0/19", "89.18.160.*", "89.244.221.*",
                "89.246.0.0/18", "89.187.225.59", "90.193.8.*", "90.200.66.*", "91.121.0.0/16",
                "91.180.214.132", "91.189.104.0/21", "91.210.56.0/22", "91.34.192.0/18",
                "92.48.64.0/18", "93.174.92.0/22", "94.23.0.0/19", "94.76.192.0/19",
                "97.74.0.0/16", "98.142.220.0/23", "98.228.15.122" });
    
    /**
     * How many alts to allow per response.
     */
    public static final IntSetting MAX_ALTS_PER_RESPONSE =
        FACTORY.createIntSetting("MAX_ALTS_PER_RESPONSE_2", 11);

    /**
     * How many responses to allow per QueryReply message.
     */
    public static final IntSetting MAX_RESPONSES_PER_REPLY =
        FACTORY.createRemoteIntSetting("MAX_RESPONSES_PER_REPLY", 10);
    
    /**
     * Base32-encoded, deflated, bencoded description of dangerous file types.
     * See DangerousFileTypeEncoder.
     */
    public static final StringSetting DANGEROUS_FILE_TYPES =
        FACTORY.createStringSetting("DANGEROUS_FILE_TYPES_2",
                "PCOF3DWRBLBTACCFX6UIZVGRBX7SMMYWALNGITKJWO7Z73VBB55TT2HRUKTIALGCAIMNX2QYQBTRYM46NKCUMR3XXDX6GFZETUYVUT6FGJKE2JLR5QLOJQMANPMQAC2ZUTIW56BUIB4C5ABV3OG7PUI4G3WS7R2IFTGACVOCDV5U4XDOMPJDNWKDMG4YIZDFK5Z4AWFLMSFEOLU3BVJGS5UUYLR4625UVVPVM2SNOE");

    /**
     * Base32-encoded, deflated, bencoded description of mime types.
     * See MimeTypeEncoder.
     */
    public static final StringSetting MIME_TYPES =
        FACTORY.createRemoteStringSetting("MIME_TYPES",
                "PCOL2UB3J3CDAEGNCW4MCVTUXPCITYZEEOND2AAVLMQJVFMJE5SMIOBWREB3XF7ADAKJIHAENJXIGEIRJCVS3KGQJCRTO37GZXHQRMBMJLIN4MZVHKIOXT3WJOV77B2T6BAAMXMMTAWC55OYD2QTE4NAGRXMIHJWFDADCU4KERKI56ENZLWAXSMI5KRJ5IKUNFNPZUCKZS23FTTH3GX2HMSCRSGT3MYQMAVNA7PPUY7M5EVQTYGLTTNATO55LI6VPBMFARMR3ZX23T5XEODMTL7CR3EAVWVQ6732DABOAQHX6IPCBPJNHV7JVF6T66P6JBPJFGZXV2QEWQRUT4OE2I2LHBPV6XRBM5YHCLLS3B2DILX44BXAO3IX6GQE75AJVN7Q");

    private final static long ONE_HOUR = 60 * 60 * 1000;
    private final static long ONE_DAY = 24 * ONE_HOUR;

    /**
     * The minimum interval in milliseconds between checking for updates to the
     * URN blacklist.
     */
    public static final LongSetting MIN_URN_BLACKLIST_UPDATE_INTERVAL =
        FACTORY.createRemoteLongSetting("MIN_URN_BLACKLIST_UPDATE_INTERVAL",
                ONE_DAY);

    /**
     * The maximum interval in milliseconds between checking for updates to the
     * URN blacklist.
     */
    public static final LongSetting MAX_URN_BLACKLIST_UPDATE_INTERVAL =
        FACTORY.createRemoteLongSetting("MAX_URN_BLACKLIST_UPDATE_INTERVAL",
                28 * ONE_DAY);

    /**
     * The URLs to check for URN blacklist updates.
     *
     * !!! NOTE: 5.4 and before have 'FilterSettings.urnBlacklistUpdateUrls' as their SIMPP key!!!
     *
     */
    public static final StringArraySetting URN_BLACKLIST_UPDATE_URLS =
        FACTORY.createRemoteStringArraySetting("URN_BLACKLIST_UPDATE_URLS",
                new String[]{"http://static.list.limewire.com/list/2"});

    /**
     * The local time of the last check for URN blacklist updates.
     */
    public static final LongSetting LAST_URN_BLACKLIST_UPDATE =
        FACTORY.createLongSetting("LAST_URN_BLACKLIST_UPDATE", 0L);

    /**
     * The local time of the next check for URN blacklist updates (the check
     * will be performed at the first launch after this time).
     */
    public static final LongSetting NEXT_URN_BLACKLIST_UPDATE =
        FACTORY.createLongSetting("NEXT_URN_BLACKLIST_UPDATE", 0L);

    /**
     * Minimum number of responses to check for similar alts.
     */
    public static final IntSetting SAME_ALTS_MIN_RESPONSES =
        FACTORY.createRemoteIntSetting("SAME_ALTS_MIN_RESPONSES", 2);

    /**
     * Minimum number of alts per response to check for similar alts.
     */
    public static final IntSetting SAME_ALTS_MIN_ALTS =
        FACTORY.createRemoteIntSetting("SAME_ALTS_MIN_ALTS", 1);

    /**
     * Minimum fraction of alts that must overlap for a reply to be dropped.
     */
    public static final FloatSetting SAME_ALTS_MIN_OVERLAP =
        FACTORY.createRemoteFloatSetting("SAME_ALTS_MIN_OVERLAP", 0.5f);

    /**
     * Whether replies in which all the responses have similar alts should be
     * marked as spam.
     */
    public static final BooleanSetting SAME_ALTS_ARE_SPAM =
        FACTORY.createRemoteBooleanSetting("SAME_ALTS_ARE_SPAM", true);    

    /** Whether to enable the ClientGuidFilter. */
    public static final BooleanSetting CLIENT_GUID_FILTER =
        FACTORY.createRemoteBooleanSetting("CLIENT_GUID_FILTER", true);
}

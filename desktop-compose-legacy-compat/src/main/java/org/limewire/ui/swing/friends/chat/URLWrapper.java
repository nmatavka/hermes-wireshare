package org.limewire.ui.swing.friends.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class URLWrapper {
    private static Pattern regex;

    static boolean isURL(String input) {
        Matcher matcher = getRegex().matcher(input.toLowerCase());
        return matcher.matches();
    }

    static String createAnchorTag(String url, String text) {
        StringBuilder builder = new StringBuilder();
        builder.append("<a href=\"");
        if (!url.matches("magnet://.*") && !url.matches("http[s]?://.*")) {
            builder.append("http://");
        }
        builder.append(url).append("\">").append(text).append("</a>");
        return builder.toString();
    }

    private static Pattern getRegex() {
        if (regex == null) {
            regex = buildRegex();
        }
        return regex;
    }

    private static Pattern buildRegex() {
        String subDomain = "(?i:[a-z0-9]|[a-z0-9][-a-z0-9]*[a-z0-9])";
        String topDomains = "(?x-i:com\\b|aero\\b|asia\\b|biz\\b|cat\\b|coop\\b|edu\\b|gov\\b|in(?:t|fo)\\b|jobs\\b|mil\\b|mobi\\b|museum\\b|name\\b|net\\b|org\\b|pro\\b|tel\\b|travel\\b|[a-z][a-z]\\b)";
        String hostname = "(?:" + subDomain + "\\.)+" + topDomains;
        String notIn = ";\"'<>()\\[\\]\\{\\}\\s\\x7F-\\xFF";
        String notEnd = ".,?";
        String anywhere = "[^" + notIn + notEnd + "]";
        String embedded = "[" + notEnd + "]";
        String urlPath = "/" + anywhere + "*(" + embedded + "+" + anywhere + "+)*";
        String url = "(?x:\\b((?:magnet|ftp|http s?)://[-\\w]+(\\.\\w[-\\w]*)+|" + hostname + ")(?:\\d+)?(?:" + urlPath + ")?)";
        return Pattern.compile(url);
    }
}

/*
 * Provenance note:
 * This file adapts parsing behavior from the local JED2K donor tree,
 * primarily org.dkf.jed2k.EMuleLink. It is now carried as WireShare-owned
 * source and the build must not depend on that donor folder at compile time.
 */

package org.limewire.ed2k.jed2k;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.limewire.ed2k.api.Ed2kLinkTargetType;

public final class EmuleLink {

    private final Ed2kLinkTargetType type;
    private final String stringValue;
    private final long numberValue;
    private final String hashValue;

    private EmuleLink(Ed2kLinkTargetType type, String stringValue, long numberValue, String hashValue) {
        this.type = type;
        this.stringValue = stringValue;
        this.numberValue = numberValue;
        this.hashValue = hashValue;
    }

    public static EmuleLink parse(String rawValue) {
        if (rawValue == null) {
            throw new IllegalArgumentException("ED2K link is empty.");
        }

        final String decoded;
        try {
            decoded = URLDecoder.decode(rawValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 decoding is unavailable.", e);
        }

        String[] parts = decoded.split("\\|");
        if (parts.length < 4 || !"ed2k://".equals(parts[0]) || !"/".equals(parts[parts.length - 1])) {
            throw new IllegalArgumentException("Malformed ED2K link.");
        }

        if ("server".equalsIgnoreCase(parts[1]) && parts.length == 5) {
            return new EmuleLink(
                Ed2kLinkTargetType.SERVER,
                parts[2],
                parsePositiveLong(parts[3], "Invalid ED2K server port."),
                null
            );
        }

        if ("serverlist".equalsIgnoreCase(parts[1]) && parts.length == 4) {
            return new EmuleLink(Ed2kLinkTargetType.SERVERLIST, parts[2], 0L, null);
        }

        if ("nodeslist".equalsIgnoreCase(parts[1]) && parts.length == 4) {
            return new EmuleLink(Ed2kLinkTargetType.NODESLIST, parts[2], 0L, null);
        }

        if ("file".equalsIgnoreCase(parts[1]) && parts.length >= 6) {
            return new EmuleLink(
                Ed2kLinkTargetType.FILE,
                decodeComponent(parts[2]),
                parsePositiveLong(parts[3], "Invalid ED2K file size."),
                parts[4]
            );
        }

        throw new IllegalArgumentException("Unsupported ED2K link type.");
    }

    public Ed2kLinkTargetType getType() {
        return type;
    }

    public String getStringValue() {
        return stringValue;
    }

    public long getNumberValue() {
        return numberValue;
    }

    public String getHashValue() {
        return hashValue;
    }

    private static long parsePositiveLong(String value, String errorMessage) {
        try {
            long parsed = Long.parseLong(value);
            if (parsed < 0) {
                throw new IllegalArgumentException(errorMessage);
            }
            return parsed;
        } catch (NumberFormatException failure) {
            throw new IllegalArgumentException(errorMessage, failure);
        }
    }

    private static String decodeComponent(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 decoding is unavailable.", e);
        }
    }
}

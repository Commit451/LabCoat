package com.commit451.gitlab.util;

import com.squareup.okhttp.Headers;

/**
 * Gets the next page link from a link header
 * http://www.w3.org/wiki/LinkHeader
 * Created by Jawnnypoo on 11/5/2015.
 */
public class LinkHeaderResolver {

    private static final String NEXT_PAGE_PREFIX = "rel=\"next\"";

    public static String getNextPageUrl(Headers headers) {
        if (headers != null && headers.get("Link") != null && headers.get("Link").contains(NEXT_PAGE_PREFIX)) {
            String linkHeader = headers.get("Link");
            String[] links = linkHeader.split(",");
            for (String link : links) {
                if (link.contains(NEXT_PAGE_PREFIX)) {
                    return extractUrlFromLink(link);
                }
            }
        }
        return null;
    }

    private static String extractUrlFromLink(String link) {
        int indexOfBracket = link.indexOf('<');
        int indexOfLastBracket = link.indexOf('>');
        return link.substring(indexOfBracket+1, indexOfLastBracket);
    }
}

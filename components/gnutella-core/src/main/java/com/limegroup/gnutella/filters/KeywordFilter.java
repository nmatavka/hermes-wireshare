package com.limegroup.gnutella.filters;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.limewire.core.api.search.SearchResult;
import org.limewire.core.settings.FilterSettings;

import com.google.common.collect.ImmutableList;
import com.limegroup.gnutella.Response;
import com.limegroup.gnutella.filters.response.ResponseFilter;
import com.limegroup.gnutella.filters.response.SearchResultFilter;
import com.limegroup.gnutella.messages.Message;
import com.limegroup.gnutella.messages.QueryReply;
import com.limegroup.gnutella.messages.QueryRequest;
import com.limegroup.gnutella.xml.LimeXMLDocument;

/** 
 * A filter that blocks queries and responses matching certain banned keywords.
 */
public class KeywordFilter implements SpamFilter, ResponseFilter, SearchResultFilter {

    static final String[] ADULT_WORDS = {
        "abby winters", "adult", "amateur nude", "amatrice new", "anal", "anul",
        "ass", "bang teen", "bangbros", "bdsm", "beach-nude", "bestiality", "bibcam",
        "blow", "bondage", "boob", "booty talk ebony", "boy + boy", "boy boy",
        "brazzers", "breached ip camera", "bukkake", "candygirl", "casting couch teen",
        "celebrity nude", "centerfold", "cinemakid", "clitoris", "club seventeen",
        "cock", "cum", "cunt", "dick", "dildo", "erotrix", "eurocreme",
        "exploited black teen", "facial","fantasia model", "fantasia-model",
        "femjoy", "fm teen", "ftv girls", "fuck", "galitsin-news", "gangbang",
        "handjob", "headjob", "hegre art", "hegre-art", "hentai", "horny", "hussyfan",
        "incest", "incezt", "jenna", "karinaworld", "kate's playground next",
        "kdv", "kiddy porn", "kinder", "lolita network", "lolitabeach",
        "ls model", "ls-magazine", "ls-model", "lsg model", "lsm",
        "magazine lsm", "masturbat", "mbla", "met art", "met-art",
        "metart", "milf", "molested", "msn not stickam", "mummy edit", "naakt nackt",
        "nackt", "naked gymnast", "naked news", "naked on stage", "naked sport",
        "naked women", "naked-gymnast", "newstar", "nipple", "nubile", "nude beach",
        "nude bellydance", "nude exhib", "nude foto", "nude in public", "nude photo",
        "nude scene", "nude sports", "nude yoga", "nude-in-public", "nudebeach",
        "online naked", "orgasm", "paraphili", "pedo", "penis", "pink teen", "pjk",
        "playboy", "porn", "posing naked", "pr0nstars", "premature", "preteen",
        "profileasian", "profileblond", "pthc", "ptsc", "public nudity", "pussy",
        "qqaazz", "qsh", "qwerty", "r@ygold", "rape", "reallola", "russian slaves",
        "scroops", "sex", "sf-model", "slut", "sodom", "spermaholic", "squirt",
        "stickam", "strapon", "strappon", "stripper", "studio siberian mouse",
        "suck", "teen nacked", "teen nackt", "teen naked", "teen nude",
        "teens like it big", "teenburg", "teenfuns", "teeniepalace", "teenmodel",
        "teens nackt", "teentraps", "tits", "tittie", "titty", "top black model",
        "topless teen", "ttl model", "twat", "uncensored naturist", "underage",
        "vagina", "video angel", "vladmodel", "voyeurweb", "whore", "xpuss",
        "xxx", "young male nudist", "young video model", "youngvideomodel", 
        "yovo", "zoophilia"
    };

    /** INVARIANT: strings in ban contain only lowercase */
    private final List<String> ban;

    KeywordFilter() {
        this(FilterSettings.FILTER_ADULT.getValue(), true);
    }

    protected KeywordFilter(boolean banAdult, boolean banPersonal) {
        ImmutableList.Builder<String> builder =
            new ImmutableList.Builder<String>();
        if(banAdult) {
            for(String word : ADULT_WORDS) {
                builder.add(word);
            }
        }
        if(banPersonal) {
            for(String word : FilterSettings.BANNED_WORDS.get()) {
                builder.add(word);
            }
            for(String ext : FilterSettings.BANNED_EXTENSIONS.get()) {
                builder.add(ext);
            }
        }
        ban = builder.build();
    }

    KeywordFilter(Collection<String> words) {
        ImmutableList.Builder<String> builder =
            new ImmutableList.Builder<String>();
        for(String word : words) {
            builder.add(word.toLowerCase(Locale.US));
        }
        ban = builder.build();
    }

    @Override // SpamFilter
    public boolean allow(Message m) {
        if (m instanceof QueryRequest) 
            return !matches(((QueryRequest)m).getQuery());
        else
            return true;
    }

    @Override // ResponseFilter
    public boolean allow(QueryReply qr, Response response) {
        return !matches(response.getName());
    }
    
    @Override // ResultFilter
    public boolean allow(SearchResult result, LimeXMLDocument document) {
        return !matches(result.getFileNameWithoutExtension());
    }
    
    /** 
     * Returns true if phrase matches any of the banned words.
     */
    protected boolean matches(String phrase) {
        String canonical = phrase.toLowerCase(Locale.US);
        for(String word : ban) {
            if(canonical.indexOf(word) != -1)
                return true;
        }
        return false;
    }
}

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
    	//CP words to filter
		"1yo", "2yo", "3yo", "4yo", "5yo","6yo", "7yo", "8yo", "9yo", "10yo", 
		"11yo", "12yo", "13yo", "14yo", "15yo","16yo", "17yo", "babyj", 
		"babyshi*", "bibcam", "childlover","cinemakid", "hussyfan","kdv", "kdquality", 
		"kiddy porn", "kingpass", "kinder*", "lolita*", "ls model", "ls-magazine", 
		"ls-model", "lsg model", "lsm", "masha", "magazine lsm", "mbla", "opva", "pedo*", 
		"preteen", "preteenz", "pthc*", "ptsc", "r@ygold", "reelkiddymov", "underage",
		// Adult words to filter
		"abby winters", "adult", "amateur nude", "amatrice new", "anal", "anul",
        "ass", "arse", "bang teen", "bangbros", "bdsm", "beach-nude", "bestiality", 
        "blowjob", "bondage", "boob*", "booty talk ebony", "boy + boy", "boy boy",
        "brazzers", "breached ip camera", "bukkake", "candygirl", "casting couch teen",
        "celebrity nude", "centerfold",  "clitoris", "club seventeen",
        "cock", "cum", "cumshot", "cunt", "defloration", "dick", "dildo", "dogsex", "erotrix", 
        "eurocreme", "exploited black teen", "facial", "fantasia model", "fantasia-model", "fisting",
        "femjoy", "fm teen", "ftv girls", "fuck*", "galitsin-news", "gangbang", "gay", "gilf",
        "handjob", "headjob", "hegre art", "hegre-art", "hentai", "horny", 
        "incest", "incezt", "jenna", "jerking", "karinaworld", "kate's playground next", "kink*",
        "lesbian", "masturbat*", "mature", "met art", "met-art",
        "metart", "milf", "molested", "msn not stickam", "mummy edit", "naakt nackt",
        "nackt", "naked gymnast", "naked news", "naked on stage", "naked sport",
        "naked women", "naked-gymnast", "newstar", "nipple", "nubile", "nude beach",
        "nude bellydance", "nude exhib", "nude foto", "nude in public", "nude photo",
        "nude scene", "nude sports", "nude yoga", "nude-in-public", "nudebeach",
        "online naked", "orgasm", "paraphili*", "penatrat*", "penis", "pink teen", "pjk",
        "playboy", "porn", "posing naked", "pr0nstars", "preggo", "pregnant", "premature", 
        "profileasian", "profileblond", "public nudity", "pussy",
        "qqaazz", "qsh", "qwerty", "rape", "reallola", "rimjob", "russian slaves",
        "scroops", "sex", "sf-model", "slut", "sodom*", "spermaholic", "squirt*",
        "stickam", "strapon", "strappon", "stripper", "studio siberian mouse",
        "suck", "sucking", "teen nacked", "teen nackt", "teen naked", "teen nude",
        "teens like it big", "teenburg", "teenfuns", "teeniepalace", "teenmodel",
        "teens nackt", "teentraps", "tit", "tittie", "titty", "top black model",
        "topless teen", "ttl model", "twat", "uncensored naturist", 
        "vagina", "video angel", "vladmodel", "voyeurweb", "whore", "xpuss",
        "xxx*", "young male nudist", "young video model", "youngvideomodel", 
        "yovo", "zoophilia", "zooskool"
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
                builder.add(word.toLowerCase(Locale.US));
            }
            for(String ext : FilterSettings.BANNED_EXTENSIONS.get()) {
                builder.add(ext.toLowerCase(Locale.US));
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
            if (word.endsWith("*")) { 			// filter if phrase contains keyword 
            	if (canonical.indexOf(word.replace("*","" )) != -1 ) {
            		return true;
            	}
            } else if (word.startsWith(".")) { 	// filter by file extensions
            	if (canonical.endsWith(word)) {
            		return true;
            	}            	
            } else { 							// filter by keyword
	        	int idx = canonical.indexOf(word);
	            while (idx != -1){				// scan for multiple occurrences in string
	            	if((idx == 0 || "abcdefghijklmnopqrstuvwxyz".indexOf(canonical.charAt(idx - 1)) == -1) // start of word boundary check
		              && (word.length() + idx == canonical.length() || "abcdefghijklmnopqrtuvwxyz".indexOf(canonical.charAt(word.length() + idx)) == -1)) {// end of word boundary check
		                return true;
		            }
	            	idx = canonical.indexOf(word,idx + 1);	
	        	}
            }
        }
        return false;
    }
}

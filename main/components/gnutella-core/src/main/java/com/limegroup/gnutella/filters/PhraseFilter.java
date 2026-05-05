package com.limegroup.gnutella.filters;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.limewire.core.api.search.SearchResult;

import com.google.common.collect.ImmutableList;
import com.limegroup.gnutella.Response;
import com.limegroup.gnutella.filters.response.ResponseFilter;
import com.limegroup.gnutella.filters.response.SearchResultFilter;
import com.limegroup.gnutella.messages.Message;
import com.limegroup.gnutella.messages.QueryReply;
import com.limegroup.gnutella.messages.QueryRequest;
import com.limegroup.gnutella.xml.LimeXMLDocument;

/** 
 * A filter that blocks queries and responses matching certain banned phrases.
 */
public class PhraseFilter implements SpamFilter, ResponseFilter, SearchResultFilter {
    
    /** INVARIANT: strings in ban contain only lowercase */
    private final List<String> ban;
	static final String[] SPAM_WORDS = {
   		//Spam words to filter
		"256k stereo.mp3", "5000passwords", "black_x).zip", "brrip.mov", "complete.mov", 
		"complete).wma", "crack.zip", "diabolic.zip", "edition}.zip", "evar.torrent", 
		"exe.torrent", "fff.zip", "fix).zip", "free music.wma", "greatest hits mix",
		"keygen.zip", "keygen inside.zip", "keygen is included).zip", "hd).mov", 
		"hi def).mov", "hot new track.mp3", "installer.zip", "new album).wma", "orion).zip", 
		"patch.zip", "radio edit.wma", "radio edit).mov", "rare record.wma", "reddragon.rar", 
		"snd].zip", "tsrh.zip", "unreleased live record).mp3", "victor.torrent", "yuridia.zip", "zip.avi"
	};
	
    PhraseFilter() {
        ban = createDefaultList();
    }
    
    PhraseFilter(String... phrases) {
        ban = ImmutableList.copyOf(phrases);
    }
    
    private List<String> createDefaultList() {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
    	for(String word : SPAM_WORDS) {
            builder.add(word.toLowerCase(Locale.US));
        }
        return builder.build();
    }

    String canonical(String word) {
        return word.toLowerCase(Locale.US).intern();
    }

    @Override // SpamFilter
    public boolean allow(Message m) {
        if (m instanceof QueryRequest) 
            return !isBanned(((QueryRequest)m).getQuery());
        else
            return true;
    }

    @Override // ResponseFilter
    public boolean allow(QueryReply qr, Response response) {
        if(isBanned(response.getName())) {
            return false;
        } else {
            LimeXMLDocument doc = response.getDocument();
            return doc == null || allowDoc(doc);
        }
    }

    @Override
    public boolean allow(SearchResult result, LimeXMLDocument document) {
        if(isBanned(result.getFileNameWithoutExtension())) {
            return false;
        } else {
            return document == null || allowDoc(document);
        }
    }
    
    /** Returns true if input matches any of the banned phrases. */
    public boolean isBanned(String input) {
        String canonical = input.toLowerCase(Locale.US);
        for(String word : ban) {
            if (word.endsWith("*")) { 			// filter if phrase contains keyword 
            	if (canonical.indexOf(word.replace("*","" )) != -1 ) {
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
    
    /** Returns true if none of the filters matched. */
    private boolean allowDoc(LimeXMLDocument doc) {
        for(Entry<String, String> entry : doc.getNameValueSet()) {
            if(isBanned(entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}

package com.limegroup.gnutella.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.limewire.collection.Cancellable;
import org.limewire.concurrent.ExecutorsHelper;
import org.limewire.core.settings.ConnectionSettings;
import org.limewire.core.settings.UltrapeerSettings;
import org.limewire.logging.Log;
import org.limewire.logging.LogFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.limegroup.gnutella.ConnectionManager;
import com.limegroup.gnutella.ConnectionServices;
import com.limegroup.gnutella.Endpoint;
import com.limegroup.gnutella.Statistics;
import com.limegroup.gnutella.http.HttpClientListener;
import com.limegroup.gnutella.http.HttpExecutor;
import com.limegroup.gnutella.util.LimeWireUtils;

/**
 * Last-ditch bootstrapping method: HTTP. 
 */
class TcpBootstrapImpl implements TcpBootstrap {

    private static final Log LOG = LogFactory.getLog(TcpBootstrapImpl.class);

    private final ExecutorService bootstrapQueue =
        ExecutorsHelper.newProcessingQueue("TCP Bootstrap");    
    private final HttpExecutor httpExecutor;
    private final Provider<HttpParams> defaultParams;
    private final ConnectionServices connectionServices;
    private final ConnectionManager connectionManager;
    private final Statistics statistics;
    private final List<URI> GWChosts = new ArrayList<URI>();
    private final List<String> BannedGWCs = new ArrayList<String>();
    private final List<String> GWCservers = new ArrayList<String>();
    private final List<String> Probation = new ArrayList<String>();
    private final String DefaultGWC = "http://wireshare.sourceforge.net/gwc/gwc.php";
    private enum Flag {
    	Fetching,Pinging,Updating,NeedsValidating,Validating,Validated;
    }
    private Flag HostsValidity = Flag.NeedsValidating;
    static int OneDay = 24*60*60*1000;
    @Inject
    TcpBootstrapImpl(HttpExecutor httpExecutor,
            @Named("defaults") Provider<HttpParams> defaultParams,
            ConnectionServices connectionServices,
            ConnectionManager connectionManager,
            Statistics statistics
            ) {
        this.httpExecutor = httpExecutor;
        this.defaultParams = defaultParams;
        this.connectionServices = connectionServices;
        this.connectionManager = connectionManager;
        this.statistics = statistics;

        GWChosts.add(URI.create(DefaultGWC));
        
        String[] BannedGWCservers = ConnectionSettings.BANNED_GWEBCACHE_SERVERS.get();
        for(String server : BannedGWCservers) {
        	BannedGWCs.add(server.trim());
        }
        
        String[] servers = ConnectionSettings.GWEBCACHE_SERVERS.get();
        Collections.shuffle(Arrays.asList(servers));
    	for(String server : servers) {
            if (!server.trim().equals(DefaultGWC) && !BannedGWCs.contains(server.trim())) {
            	add(URI.create(server.trim()),GWChosts);
            	GWCservers.add(server);
            }
        }
    	
        if(LOG.isDebugEnabled()) {
            LOG.debug("Loaded " + GWChosts.size() + " GWebCache servers");
        }
    }

    boolean add(URI Addr, List<URI> arr) {
        return arr.add(Addr);
    }
    
    boolean remove(URI Addr, List<URI> arr) {
        return arr.remove(Addr);
    }
    
    @Override
    public synchronized boolean fetchHosts(Bootstrapper.Listener listener){ 
        List<HttpUriRequest> requests = new ArrayList<HttpUriRequest>();
        Map<HttpUriRequest, URI> requestToHost = new HashMap<HttpUriRequest, URI>();
        for(URI host : GWChosts) {
            host = URI.create(host.toString() + "?get=1" + 
            		"&net=gnutella" + 
        			"&client=" + LimeWireUtils.QHD_VENDOR_NAME + 
        			"&version=" + LimeWireUtils.getLimeWireVersion() 
        			);
            HttpUriRequest request = newRequest(host); 
            requests.add(request);
            requestToHost.put(request,host);
        }

        if(requests.isEmpty()) {
        	if (LOG.isDebugEnabled()) 
        		LOG.debug("No GWC host caches to try");
            return false;
        }

        HttpParams params = new BasicHttpParams();
        params = new DefaultedHttpParams(params, defaultParams.get());

        if(LOG.isDebugEnabled())
            LOG.debug("Fetching from GWC host 1 of " + requests.size() + " caches");

        httpExecutor.executeAny(new Listener(requestToHost, listener, Flag.Fetching),
                bootstrapQueue, requests, params,
                new Cancellable() {
            public boolean isCancelled() {
            	Boolean Cancelled = (connectionServices.getNumInitializedConnections() > 0);
            	if (Cancelled && LOG.isDebugEnabled() ) LOG.debug("Fetching cancelled. We are already connected.");
                return Cancelled;
            }
        });
        return true;
    }
    
    @Override
    public synchronized boolean pingHosts(Bootstrapper.Listener listener){ 
        if (HostsValidity.equals(Flag.Validated)) { 
        	if (LOG.isDebugEnabled()) 
        		LOG.debug("Pinging not performed. No new hosts to try.");
        	return false;
        } else {
        	HostsValidity = Flag.Validating;	
		}
        recoverProbation();
        List<HttpUriRequest> requests = new ArrayList<HttpUriRequest>();
        Map<HttpUriRequest, URI> requestToHost = new HashMap<HttpUriRequest, URI>();
        for(URI host : GWChosts) {
            host = URI.create(host.toString() + "?ping=1" + 
            		"&net=gnutella" +
        			"&client=" + LimeWireUtils.QHD_VENDOR_NAME + 
        			"&version=" + LimeWireUtils.getLimeWireVersion() 
        			);
            HttpUriRequest request = newRequest(host); 
            requests.add(request);
            requestToHost.put(request,host);
        }

        if(requests.isEmpty()) {
        	if (LOG.isDebugEnabled()) 
        		LOG.debug("No GWC host caches to try");
            return false;
        }

        HttpParams params = new BasicHttpParams();
        params = new DefaultedHttpParams(params, defaultParams.get());

        if(LOG.isDebugEnabled())
            LOG.debug("Pinging GWC host 1 of " + requests.size() + " caches");

        httpExecutor.executeAny(new Listener(requestToHost, listener, Flag.Pinging),
                bootstrapQueue, requests, params,
                new Cancellable() {
            public boolean isCancelled() {
                return false;
            }
        });
        return true;
    }
    
    @Override
    public synchronized boolean UpdateGWC(String addr, Bootstrapper.Listener listener) {
        List<HttpUriRequest> requests = new ArrayList<HttpUriRequest>();
        Map<HttpUriRequest, URI> requestToHost = new HashMap<HttpUriRequest, URI>();
        recoverProbation();
        String url = "";
        String randomHost = "";
        if (HostsValidity.equals(Flag.Validated)) { 
	        if (!GWCservers.isEmpty()) {
	        	Collections.shuffle(GWCservers);
	        	randomHost = GWCservers.get(new Random().nextInt(GWCservers.size()));
	        }
        } else {
        	HostsValidity = Flag.Validating;	
    		url = "&ping=1";
		}
		for(URI host : GWChosts) {
			if (!randomHost.isEmpty()) {
				if (!host.toString().equals(randomHost)) url = "&url=" + URLEncode(randomHost); else url = "";
			}
			host = URI.create(host.toString() + "?update=1&ip=" + URLEncode(addr) + url +
        			"&net=gnutella" + 
        			"&client=" + LimeWireUtils.QHD_VENDOR_NAME + 
        			"&version=" + LimeWireUtils.getLimeWireVersion() +
        			"&uptime=" + statistics.calculateDailyUptime() + 
        			"&x_leaves=" + connectionManager.getNumInitializedClientConnections() +
        			"&x_max=" + UltrapeerSettings.MAX_LEAVES.getValue()
        			);
            HttpUriRequest request = newRequest(host);
            requests.add(request);
            requestToHost.put(request, host);
        }

        if(requests.isEmpty()) {
        	if (LOG.isDebugEnabled()) 
        		LOG.debug("No GWC host caches to try");
            return false;
        }

        HttpParams params = new BasicHttpParams();
        params = new DefaultedHttpParams(params, defaultParams.get());

        if(LOG.isDebugEnabled())
            LOG.debug("Updating GWC host 1 of " + requests.size() + " caches");

        httpExecutor.executeAny(new Listener(requestToHost, listener, Flag.Updating),
                bootstrapQueue, requests, params, 
                new Cancellable() {
            public boolean isCancelled() {
                return false;
            }
        });
        return true;
	}
    
    private String URLEncode(String str) {
    	try {
    		return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "" ;
		}
    }
    
    private HttpUriRequest newRequest(URI host) {
        HttpGet get = new HttpGet(host);
        get.addHeader("Cache-Control", "no-cache");
        get.addHeader("User-Agent", LimeWireUtils.getHttpServer());
        get.addHeader("Connection", "close");
        return get;
    }

    private void recoverProbation() {
    	if (!Probation.isEmpty()) {
    		long now = System.currentTimeMillis();
	    	for(String record : Probation) { 
	    		String[] Field = record.split(";");
	    		if (!GWCservers.contains(Field[0]) && Long.decode(Field[2]) < now) {
    				GWCservers.add(Field[0]);
    				add(URI.create(Field[0]),GWChosts);
    				HostsValidity = Flag.NeedsValidating;
	    		}
	    	}
    	}
    }
    
    private void removeProbation(String url) {
    	if (!Probation.isEmpty()) {
	    	for(String record : Probation) { 
	    		String[] Field = record.split(";");
	    		if (Field[0].equals(url)){
	    			Probation.remove(record);
	    			if (LOG.isDebugEnabled()) 
	    				LOG.debug("Host cache: " + url + " recovered from probation" );
	    			break;
	    		}
	    	}
    	}
    }
    
    private int parseResponse(HttpResponse response, Bootstrapper.Listener listener, Flag ReqType, String url) {
        if(response.getEntity() == null) {
            LOG.warn("No response entity!");
            return 0;
        }
        String line = null;
        boolean UpdateOK = false;
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        try {
            InputStream in = response.getEntity().getContent();
            String charset = EntityUtils.getContentCharSet(response.getEntity());
            if(charset == null) charset = HTTP.DEFAULT_CONTENT_CHARSET;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
            while((line = reader.readLine()) != null && line.length() > 0) {
            	String[] words = line.trim().split( line.contains(",") ? "," : "\\|" );
                if(words != null && words.length > 0) {
                    try {
                    	if (words[0].startsWith("<")) {
                    		removeCache(url, true);
                    		break;
                    	} else if (words[0].equals("H")) {
	                    	Endpoint host = new Endpoint(words[1], true);
	                        if(LOG.isDebugEnabled()) 
	                        	LOG.debug("Received " + host);
	                        endpoints.add(host);
                    	} else if (words[0].equals("U")) {
                			if (!GWCservers.contains(words[1]) && !words[1].startsWith(DefaultGWC) && !BannedGWCs.contains(words[1])) {
                    			add(URI.create(words[1]),GWChosts);
                    			GWCservers.add(words[1]);
                    			ConnectionSettings.GWEBCACHE_SERVERS.set(GWCservers.toArray(new String[0]));
                    			HostsValidity = Flag.NeedsValidating;
                    		}
                    	} else if (words[0].equals("I")) { 		
                    		if (words[1].equals("update") ) {
                				if (words[2].equals("OK")) {
                					UpdateOK = true;
                					if (LOG.isDebugEnabled()) 
                						LOG.debug(line);
                				} else if (words[2].toLowerCase().equals("warning") ) {
                					if (LOG.isDebugEnabled()) 
                						LOG.debug(line);
                        		}
                    		} else if (words[1].equals("access") ) {
                    			if (LOG.isDebugEnabled()) 
                    				LOG.debug(line);
                    		} else if (words[1].equals("pong") ) {
                    			if (LOG.isDebugEnabled()) 
                    				LOG.debug(line);
                    			if (words.length > 3) {
                    				boolean remove = true;
                    				for (String nets : words[3].split("-")) {
                    					if (nets.equals("gnutella")) {
                    						remove = false;
                    						break;
                    					}
                    				}
	                    			if (remove) removeCache(url, true);
                    			}
                    		} else if (words[1].toLowerCase().equals("warning") ) {
                    			if (LOG.isDebugEnabled()) 
                    				LOG.debug(line);
                    			if (words[2].toLowerCase().startsWith("invalid host")) removeCache(url, true);
                    		} else if (words[1].equals("NO-HOSTS")) {
                    			if (LOG.isDebugEnabled()) 
                    				LOG.debug(line);
                    			removeCache(url, true);
                    		}
                    	} else if (words[0].equals("OK")) { 	
                    		if (ReqType.equals(Flag.Updating)) UpdateOK=true;
                    		if (LOG.isDebugEnabled()) 
                    			LOG.debug(line);
                    	} else if (words[0].startsWith("PONG")) { 
                    		if (LOG.isDebugEnabled()) 
                    			LOG.debug(line);
                    	} else if (words[0].startsWith("ERROR:")) { 
                    		if (LOG.isDebugEnabled()) 
                    			LOG.debug(line);
                    		if (words[0].toLowerCase().contains("network not supported")) removeCache(url, true);
                    	} else if (words[0].startsWith("ERROR")) { 
                    		if (LOG.isDebugEnabled()) 
                    			LOG.debug(line);
                    	} else if (words[0].startsWith("WARNING")) { 
                    		if (LOG.isDebugEnabled()) 
                    			LOG.debug(line);
                    	} else {
	                    	Endpoint host = new Endpoint(words[0], true);
	                        if(LOG.isDebugEnabled()) 
	                        	LOG.debug("Received " + host);
	                        endpoints.add(host);
                        };
                    } catch(IllegalArgumentException e) {
                        LOG.error("Malformed line: " + line + " (" + e.getCause() + ")");
                    }
                }
            }
        } catch(IOException e) {
            LOG.error("IOX", e);
        }
        if (ReqType.equals(Flag.Updating) && !UpdateOK) removeCache(url, true);
        if(!endpoints.isEmpty()) {
        	if (LOG.isDebugEnabled()) 
        		LOG.debug(endpoints.size() + " endpoints received");
            return listener.handleHosts(endpoints);
        } else {
            if (ReqType.equals(Flag.Fetching) && LOG.isDebugEnabled()) 
            	LOG.debug("No endpoints received");
            return 0;
        }
    }
    
    private void removeCache(String url, boolean Ban) {
    	if (!url.equals(DefaultGWC)) {
        	if (GWCservers.contains(url)) {
        		GWCservers.remove(url);
    			remove(URI.create(url),GWChosts); 
			}
        	if (!Ban) {
				long NextCheck = System.currentTimeMillis() + OneDay;
				boolean Updated = false;
	    		if (!Probation.isEmpty()) {
	    	    	for(String record : Probation) { 
	    	    		String[] Field = record.split(";");
	    	    		if (Field[0].equals(url)){
	    	    			Probation.remove(record);
	    	    			Integer intTries = Integer.decode(Field[1]);
	    	    			if (intTries < 7) {
	    	    				intTries++;
		    	    			Field[1] = intTries.toString();
		    	    			Field[2] = Long.toString(NextCheck);
		    	    			Probation.add(String.join(";",Field));
		    	    			Updated = true;	    			
		    	    			if (LOG.isDebugEnabled()) 
		    	    				LOG.debug("Host cache: " + url + " probation extened" );
	    	    			} else { 
	    	    				Ban = true;
	    	    			}
		    	    		break;
	    	    		}
	    	    	}
	        	}        		
	    		if (!Updated && !Ban) {
	    			Probation.add(url + ";1;" + NextCheck);
	    			if (LOG.isDebugEnabled()) 
	    				LOG.debug("Host cache: " + url + " added to probation" );
	    		}
        	}
	    	if (Ban) {
	    		if (!BannedGWCs.contains(url)) {
	    			BannedGWCs.add(url);
	    			ConnectionSettings.BANNED_GWEBCACHE_SERVERS.set(BannedGWCs.toArray(new String[0]));
	    		}	
				List<String> PermHosts = new ArrayList<String>(Arrays.asList(ConnectionSettings.GWEBCACHE_SERVERS.get()));
				if (PermHosts.contains(url)) {
		    		PermHosts.remove(url);
		    		ConnectionSettings.GWEBCACHE_SERVERS.set(PermHosts.toArray(new String[0]));
		    		if (LOG.isDebugEnabled()) 
	    				LOG.debug("Removed cache:" + url);
				}
	    	} 
    	}
    }
    
    private void removeCache(String url) {
    	removeCache(url, false);
    }
    
    private class Listener implements HttpClientListener {
        private final Map<HttpUriRequest, URI> hosts;
        private final Bootstrapper.Listener listener;
        private int Contacted;
        private Flag ReqType;
        
        Listener(Map<HttpUriRequest, URI> hosts, Bootstrapper.Listener listener, Flag ReqType) {
            this.hosts = hosts;
            this.listener = listener;
            this.ReqType = ReqType;
            this.Contacted = 0;
        }

        @Override
        public boolean requestComplete(HttpUriRequest request, HttpResponse response) {
        	if(LOG.isDebugEnabled())
                LOG.debug("Completed request: " + request.getRequestLine());
            int received = 0;
            String url = request.getURI().toString().substring(0,request.getURI().toString().indexOf("?"));
            if (response.getStatusLine().getStatusCode() == 200) {
            	removeProbation(url);
            	received = parseResponse(response, listener, ReqType, url);
            } else if (response.getStatusLine().getStatusCode() >= 400 && response.getStatusLine().getStatusCode() < 600) {
            	removeCache(url);
            }
            httpExecutor.releaseResources(response);
            return UpdateStatus(received < 10);
        }

        @Override
        public boolean requestFailed(HttpUriRequest request, HttpResponse response, IOException exc) {
        	if(LOG.isDebugEnabled()) {
                LOG.debug("Failed request: " + request.getRequestLine());
                if(response != null)
                    LOG.debug("Response " + response);
                if(exc != null)
                    LOG.debug(exc);
            }
        	String url = request.getURI().toString().substring(0,request.getURI().toString().indexOf("?"));
            removeCache(url);
            httpExecutor.releaseResources(response);
            return UpdateStatus(true);
        }

        @Override
        public boolean allowRequest(HttpUriRequest request) {
            // Do not allow the request if we don't know about it
            synchronized(TcpBootstrapImpl.this) {
                return hosts.containsKey(request);
            }
        }
        
        private boolean UpdateStatus(boolean Continue) {
        	Contacted++;
        	if (Contacted == hosts.size()) { 
            	if (HostsValidity.equals(Flag.Validating)) HostsValidity = Flag.Validated;
        	} else if (Continue && LOG.isDebugEnabled()){
            	if (ReqType.equals(Flag.Fetching)) {
            		LOG.debug("Fetching from GWC host " + (Contacted + 1) + " of " + hosts.size() + " caches");
            	} else if (ReqType.equals(Flag.Pinging)) {
            		LOG.debug("Pinging GWC host " + (Contacted + 1) + " of " + hosts.size() + " caches");
            	} else if (ReqType.equals(Flag.Updating)) {
            		LOG.debug("Updating GWC host " + (Contacted + 1) + " of " + hosts.size() + " caches");
            	}
            }
            return Continue;
        }
    }
}

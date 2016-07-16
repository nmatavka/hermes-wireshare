package com.limegroup.gnutella.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        GWChosts.add(URI.create("http://wireshare.sourceforge.net/gwc/gwc.php"));
        String[] GWCservers = ConnectionSettings.GWEBCACHE_SERVERS.get();
        for(String server : GWCservers) {
            add(URI.create(server.trim()),GWChosts);
        }
        if(LOG.isDebugEnabled()) {
            LOG.debug("Loaded " + GWCservers.length + " GWebCache servers");
        }
   }

    boolean add(URI Addr, List<URI> arr) {
        return arr.add(Addr);
    }
    
    void addServer(String uri, String[] servers) {
		String[] tmp = new String[servers.length+1];
		for (int cnt=0 ;cnt<servers.length; cnt++) {
			tmp[cnt] = servers[cnt].trim();
		}
		tmp[servers.length] = uri;
		ConnectionSettings.GWEBCACHE_SERVERS.set(tmp);
    }
    
    @Override
    public synchronized boolean fetchHosts(Bootstrapper.Listener listener){ 
        List<HttpUriRequest> requests = new ArrayList<HttpUriRequest>();
        Map<HttpUriRequest, URI> requestToHost = new HashMap<HttpUriRequest, URI>();
        for(URI host : GWChosts) {
            host = URI.create(host.toString() + "?get=1&net=gnutella" + 
        			"&client=" + LimeWireUtils.QHD_VENDOR_NAME + 
        			"&version=" + LimeWireUtils.getLimeWireVersion() 
        			);
            HttpUriRequest request = newRequest(host); 
            requests.add(request);
            requestToHost.put(request,host);
        }

        if(requests.isEmpty()) {
            LOG.debug("No TCP host caches to try");
            return false;
        }

        HttpParams params = new BasicHttpParams();
        params = new DefaultedHttpParams(params, defaultParams.get());

        if(LOG.isDebugEnabled())
            LOG.debug("Trying 1 of " + requests.size() + " TCP host caches");

        httpExecutor.executeAny(new Listener(requestToHost, listener),
                bootstrapQueue, requests, params,
                new Cancellable() {
            public boolean isCancelled() {
                return (connectionServices.getNumInitializedConnections() > 0);
            }
        });
        return true;
    }
    
    @Override
    public boolean UpdateGWC(String addr, Bootstrapper.Listener listener) {
        List<HttpUriRequest> requests = new ArrayList<HttpUriRequest>();
        Map<HttpUriRequest, URI> requestToHost = new HashMap<HttpUriRequest, URI>();
        //String IP = ((String[]) addr.split(":"))[0];
		for(URI host : GWChosts) {
        	host = URI.create(host.toString() + "?update=1&ip=" + URLEncode(addr) + 
        			"&net=gnutella&client=" + LimeWireUtils.QHD_VENDOR_NAME + 
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
            LOG.debug("No TCP host caches to try");
            return false;
        }

        HttpParams params = new BasicHttpParams();
        params = new DefaultedHttpParams(params, defaultParams.get());

        if(LOG.isDebugEnabled())
            LOG.debug("Updating 1 of " + requests.size() + " Gnutella Web Caches");

        httpExecutor.executeAny(new Listener(requestToHost, listener),
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

    private int parseResponse(HttpResponse response, Bootstrapper.Listener listener) {
        if(response.getEntity() == null) {
            LOG.warn("No response entity!");
            return 0;
        }

        String line = null;
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        try {
            InputStream in = response.getEntity().getContent();
            String charset = EntityUtils.getContentCharSet(response.getEntity());
            if(charset == null)
                charset = HTTP.DEFAULT_CONTENT_CHARSET;
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(in, charset));
            while((line = reader.readLine()) != null && line.length() > 0) {
            	String[] words = line.trim().split( line.contains(",") ? "," : "\\|" );
                if(words != null && words.length > 0) {
                    try {
                        if (words[0].equals("H")) {
	                    	Endpoint host = new Endpoint(words[1], true);
	                        if(LOG.isDebugEnabled())
	                            LOG.debug("Received " + host);
	                        endpoints.add(host);
                    	} else if (words[0].equals("U")) {
                			boolean NoAdd=false;
                			String[] servers = ConnectionSettings.GWEBCACHE_SERVERS.get();
                			for(String server : servers) {
								if (server.equals(words[1])) {
									NoAdd=true;
								}
                    		}
                    		if (!NoAdd) {
                    			add(URI.create(words[1]),GWChosts);
                    			addServer(words[1].toString(), servers);
                    		}
                    	} else if (words[0].equals("I")) { 		
                    		if (words[1].equals("update") ) {
                    				if (words[2].equals("OK")) 
                    					LOG.debug(line);
                    		}else if (words[1].equals("access") ) {
                    			
                    		}
                    	} else if (words[0].equals("OK")) { 	
                    		LOG.debug(line);
                    	} else if (words[0].startsWith("ERROR")) { 
                    		LOG.debug(line);
                    	} else if (words[0].startsWith("WARNING")) { 
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

        if(!endpoints.isEmpty()) {
            return listener.handleHosts(endpoints);
        } else {
            LOG.debug("No endpoints received");
            return 0;
        }
    }

    private class Listener implements HttpClientListener {
        private final Map<HttpUriRequest, URI> hosts;
        private final Bootstrapper.Listener listener;

        Listener(Map<HttpUriRequest, URI> hosts, Bootstrapper.Listener listener) {
            this.hosts = hosts;
            this.listener = listener;
        }

        @Override
        public boolean requestComplete(HttpUriRequest request, HttpResponse response) {
            if(LOG.isDebugEnabled())
                LOG.debug("Completed request: " + request.getRequestLine());
            int received = parseResponse(response, listener);
            httpExecutor.releaseResources(response);
            return received < 10;
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
            httpExecutor.releaseResources(response);
            return true;
        }

        @Override
        public boolean allowRequest(HttpUriRequest request) {
            // Do not allow the request if we don't know about it
            synchronized(TcpBootstrapImpl.this) {
                return hosts.containsKey(request);
            }
        }
    }
}

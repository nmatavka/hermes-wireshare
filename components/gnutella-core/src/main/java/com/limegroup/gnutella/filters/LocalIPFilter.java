package com.limegroup.gnutella.filters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.core.settings.FilterSettings;
import org.limewire.core.settings.InstallSettings;
import org.limewire.inject.EagerSingleton;
import org.limewire.io.Expand;
import org.limewire.io.IOUtils;
import org.limewire.io.IP;
import org.limewire.util.CommonUtils;
import org.limewire.util.Version;
import org.limewire.util.VersionFormatException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Blocks messages and hosts based on IP address.  
 */
@EagerSingleton
public final class LocalIPFilter extends AbstractIPFilter {
    
    private static final Log LOG = LogFactory.getLog(LocalIPFilter.class);
    
    private volatile IPList badHosts;
    private volatile IPList goodHosts;
    /** List contained in hostiles.txt if any.  Loaded on startup only */ 
    private final IPList hostilesTXTHosts = new IPList();
    
    private final IPFilter hostileNetworkFilter;
    private final ScheduledExecutorService ipLoader;
    /** Marker for whether or not hostiles need to be loaded. */
    private volatile boolean shouldLoadHostiles;
    
    private volatile long whitelistings; // # of times we whitelisted an ip 
    private volatile long blacklistings; // # of times we blacklisted an ip 
    private volatile long netblockings;  // # of times net blacklisted an ip 
    private volatile long implicitings;  // # of times an ip was implicitly allowed
    
    /** Constructs an IPFilter that automatically loads the content. */
    @Inject
    public LocalIPFilter(@Named("hostileFilter") IPFilter hostileNetworkFilter, 
            @Named("backgroundExecutor") ScheduledExecutorService ipLoader) {
        this.hostileNetworkFilter = hostileNetworkFilter;
        this.ipLoader = ipLoader;
        
        //File hostiles = new File(CommonUtils.getUserSettingsDir(), "hostiles.txt");
        shouldLoadHostiles = InstallSettings.SECURITY_LEVEL.get() > 0 || InstallSettings.SECURITY_UPDATE.getValue(); //hostiles.exists();
        
        refreshHosts();
    }
    
    @Override
    public void refreshHosts() {
        refreshHosts(null);
    }
    
    @Override
    public void refreshHosts(final LoadCallback callback) {
        Runnable load = new Runnable() {
            public void run() {
                hostileNetworkFilter.refreshHosts();
                refreshHostsImpl();
                if (callback != null)
                    callback.spamFilterLoaded();
            }
        };
        if (!shouldLoadHostiles) 
            load.run();
        else 
            ipLoader.execute(load);
    }
    
    /** Does the work of setting new good  & bad hosts. */
    private void refreshHostsImpl() {
        LOG.debug("Refreshing local IP filter");
        
        // Load the local blacklist, stripping out invalid entries
        IPList newBad = new IPList();
        String[] allHosts = FilterSettings.BLACK_LISTED_IP_ADDRESSES.get();
        ArrayList<String> valid = new ArrayList<String>(allHosts.length);
        for (int i=0; i<allHosts.length; i++) {
            if(newBad.add(allHosts[i]))
                valid.add(allHosts[i]);
        }
        if(valid.size() != allHosts.length) {
            allHosts = valid.toArray(new String[0]);
            FilterSettings.BLACK_LISTED_IP_ADDRESSES.set(allHosts);
        }
        
        // Load the local whitelist, stripping out invalid entries
        IPList newGood = new IPList();
        allHosts = FilterSettings.WHITE_LISTED_IP_ADDRESSES.get();
        valid = new ArrayList<String>(allHosts.length);
        for (int i=0; i<allHosts.length; i++) {
            if(newGood.add(allHosts[i]))
                valid.add(allHosts[i]);
        }
        if(valid.size() != allHosts.length) {
            allHosts = valid.toArray(new String[0]);
            FilterSettings.WHITE_LISTED_IP_ADDRESSES.set(allHosts);
        }

        // Load data from hostiles.txt (if it wasn't already loaded!)...
        if(shouldLoadHostiles) {
            shouldLoadHostiles = false;
            Version currversion = null;
            File hostiles = new File(CommonUtils.getUserSettingsDir(), "hostiles.txt");
            try {
            	currversion = new Version(getVersion("https://wireshare.sourceforge.net/WSSecurityUpdates/version"));
            } catch (VersionFormatException impossible){};
            if ( currversion != null ) {
            	LOG.debug("Current security version online = v" + currversion);
            	LOG.debug("Installed security version = v" + InstallSettings.SECURITY_VERSION.get());
            	try {
            		if (currversion.compareTo( new Version(InstallSettings.SECURITY_VERSION.get())) > 0 
            				|| !hostiles.exists()
            				|| InstallSettings.SECURITY_UPDATE.getValue()) {
						String url = "https://wireshare.sourceforge.net/WSSecurityUpdates/";
						String Hostiles = CommonUtils.getUserSettingsDir() + "\\hostiles.zip";
						LOG.debug("Updating security files...");
						boolean Success = true;
					    switch (InstallSettings.SECURITY_LEVEL.get()) {
					    case 4:
					    	//Success = getHostiles(url + "HostilesFull.zip", Hostiles);
					    	//break;
					    case 3:
					    	Success = getHostiles(url + "HostilesNJ.zip", Hostiles);
					    	break;
					    case 2:
					    	//Success = getHostiles(url + "HostilesLight.zip", Hostiles);
					    	//break;
					    case 1:
					    	Success = getHostiles(url + "HostilesLightNJ.zip", Hostiles);
					    	break;
					    default:
					    	Success = false;
					    }
					    if (Success) {
					    	LOG.debug("Security files updated.");
					    	InstallSettings.SECURITY_VERSION.set(currversion.toString());
					    	InstallSettings.SECURITY_UPDATE.setValue(false);
					    } else {
					    	LOG.debug("Failed to update security files.");	
					    }
					}
				} catch (VersionFormatException e) {
					LOG.debug(e.getMessage());
				}
            }
            LOG.debug("Loading hostiles.txt");
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(hostiles));
                String read = null;
                while( (read = reader.readLine()) != null) {
                    if (!read.trim().isEmpty()) hostilesTXTHosts.add(read);
                }
            } catch(IOException e) {
                LOG.debug("Error loading hostiles.txt", e);
            } finally {
                IOUtils.close(reader);
            }
        }
        
        badHosts = new MultiIPList(newBad, hostilesTXTHosts);
        goodHosts = newGood;
    }
    
    private void Extract(String zipFilePath, File Path) throws FileNotFoundException{
    	FileInputStream in = new FileInputStream(zipFilePath);
	    try {
			Expand.expandFile(in, Path, true, null);
			in.close();
			File zip = new File(zipFilePath);
			zip.delete();
		} catch (IOException e) {
		}
    }
    
    private String getVersion(String urlToRead) {
    	String result = null;
    	try {
	        URL url = new URL(urlToRead);
	        URLConnection conn = url.openConnection();
	        if (conn instanceof HttpURLConnection) {
	            HttpURLConnection hconn = (HttpURLConnection) conn;
	            hconn.setInstanceFollowRedirects(false);
	            int response = hconn.getResponseCode();
	            boolean redirect = (response >= 300 && response <= 399);
	            if (redirect) {
	                String loc = conn.getHeaderField("Location");
	                if (loc.startsWith("http", 0)) {
	                    url = new URL(loc);
	                } else {
	                    url = new URL(url, loc);
	                }
	                conn = (HttpURLConnection) url.openConnection();
	            }
	        }
	        if (conn instanceof HttpsURLConnection) {
	     	   HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
	     	   SSLContext sc;
	     	   sc = SSLContext.getInstance("TLS");
	     	   sc.init(null, null, new java.security.SecureRandom());
	     	   httpsConn.setSSLSocketFactory(sc.getSocketFactory());
	        }
           BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
           result = rd.readLine();
           rd.close();
        } catch (IOException e) {
        } catch (Exception e) {
        }
        return result;
    }
    
    private boolean getHostiles(String strURL, String localFilename){
    	try {
			downloadFromUrl(strURL, localFilename);
			Extract(localFilename,CommonUtils.getUserSettingsDir());
			return true;
		} catch (IOException e) {
			return false;
		}
    }
    	
    private void downloadFromUrl(String strURL, String localFilename) throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;
        URL url = new URL(strURL);
        try {
            URLConnection conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection hconn = (HttpURLConnection) conn;
                hconn.setInstanceFollowRedirects(false);
                int response = hconn.getResponseCode();
                boolean redirect = (response >= 300 && response <= 399);
                if (redirect) {
                    String loc = conn.getHeaderField("Location");
                    if (loc.startsWith("http", 0)) {
                        url = new URL(loc);
                    } else {
                        url = new URL(url, loc);
                    }
                    conn = (HttpURLConnection) url.openConnection();
                }
            }
            if (conn instanceof HttpsURLConnection) {
         	   HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
         	   SSLContext sc;
         	   sc = SSLContext.getInstance("TLS");
         	   sc.init(null, null, new java.security.SecureRandom());
         	   httpsConn.setSSLSocketFactory(sc.getSocketFactory());
            }

            is = conn.getInputStream();               //get connection inputstream
            fos = new FileOutputStream(localFilename);   //open outputstream to local file

            byte[] buffer = new byte[4096];              //declare 4KB buffer
            int len;

            //while we have available data, continue downloading and storing to local file
            while ((len = is.read(buffer)) > 0) {  
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
        } catch (Exception e) {
		} finally {
            try {
                if (is != null) {
                    is.close();
                }
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }
 
    /** Determines if any blacklisted hosts exist. */
    @Override
    public boolean hasBlacklistedHosts() {
        return 
          (FilterSettings.USE_NETWORK_FILTER.getValue() && hostileNetworkFilter.hasBlacklistedHosts())
          || !badHosts.isEmpty();
    }
    
    @Override
    protected boolean allowImpl(IP ip) {
        if (goodHosts.contains(ip)) {
            whitelistings++;
            return true;
        }

        if (badHosts.contains(ip)) {
            blacklistings++;
            return false;
        }

        if (FilterSettings.USE_NETWORK_FILTER.getValue() && !hostileNetworkFilter.allow(ip)) {
            netblockings++;
            return false;
        }

        implicitings++;
        return true;
    }
}
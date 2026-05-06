package org.limewire.ui.desktop.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.limewire.service.ErrorService;
import org.limewire.util.CommonUtils;
import org.limewire.util.NativeLibraryLoader;
import org.limewire.util.OSUtils;

/**
 * A collection of utility methods for OSX.
 * These methods should only be called if run from OSX,
 * otherwise ClassNotFoundErrors may occur.
 * 
 * Clients may use the method isNativeLibraryLoadedCorrectly() 
 * to check whether the native library loaded correctly.
 * If not, they may choose to disable certain user interface
 * features to reflect this state.
 * 
 * <p>
 * To determine if the Cocoa Foundation classes are present,
 * use the method CommonUtils.isCocoaFoundationAvailable().
 */
public class MacOSXUtils {
    
    /**
     * The application bundle identifier for the LimeWire application that is packed into its Info.plist config file.
     */
    public static final String LIMEWIRE_APPLICATION_BUNDLE_IDENTIFIER = "com.wireshare.gnutella";
    
    /**
     * The name of the app that launches.
     */
    private static final String APP_NAME = "WireShare.app";

    private static boolean nativeLibraryLoadedCorrectly = false;
    private static boolean nativeBindingFailureLogged = false;
    
    private static final Logger LOG = Logger.getLogger(MacOSXUtils.class.getName());
    
    static {
        if (OSUtils.isMacOSX()) {
            try {
                nativeLibraryLoadedCorrectly = NativeLibraryLoader.loadFirstAvailable(
                        "MacOSXUtils",
                        "lib/native/libMacOSXUtils.dylib",
                        "nativelibs/osx/libMacOSXUtils.dylib",
                        "build/native/osx/libMacOSXUtils.dylib");
            } catch (UnsatisfiedLinkError err) {
                ErrorService.error(err, "java.library.path=" + System.getProperty("java.library.path") + "\n\n" + "trace dependencies=" + MacOSXUtils.traceLibraryDependencies("MacOSXUtils.jnilib"));
            }
        }
    }
    
    /**
     * This returns a boolean indicating whether an exception occurred when loading the native library.
     * @return true if the native library loaded without any errors.
     */
    public static boolean isNativeLibraryLoadedCorrectly() {
        return nativeLibraryLoadedCorrectly;
    }

    private static boolean hasNativeLibrary() {
        return nativeLibraryLoadedCorrectly;
    }

    private static void handleNativeBindingFailure(UnsatisfiedLinkError ule) {
        nativeLibraryLoadedCorrectly = false;
        if (!nativeBindingFailureLogged) {
            nativeBindingFailureLogged = true;
            LOG.log(Level.WARNING, "Disabling MacOSXUtils native helpers after missing Launch Services binding", ule);
        }
    }

    private MacOSXUtils() {}
    
    /**
    * If a given library is not loading for some users on OS-X, this method
    * can be used to trace what other libraries this library is dependent
    * on and whether those libraries are present on the user's system.
    */
    public static String traceLibraryDependencies(String libraryName) {       
        StringBuffer traceResultsBuffer = new StringBuffer("ls command output: ");
        String lsCommand = "ls " + System.getProperty("user.dir");
        traceResultsBuffer.append("(").append(lsCommand).append(") "); 
        traceResultsBuffer.append( getCommandOutput(lsCommand) );
        traceResultsBuffer.append( "\n" );

        String otoolCommand = "otool -L " + System.getProperty("user.dir") + "/" + libraryName;
        traceResultsBuffer.append("otool command output: ");
        traceResultsBuffer.append( getCommandOutput(otoolCommand) );
        traceResultsBuffer.append( "\n" );
               
        return traceResultsBuffer.toString();
    }
    
    /**
    * This method runs a system command and returns the command's output as a string.
    *
    */
    private static String getCommandOutput(String command) {
        StringBuffer outputBuffer = new StringBuffer("");

        try {
            // start the command running
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(command);
    
            // put a BufferedReader on the command output
            InputStream inputstream = process.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
    
            // read the command output   
            String line;
            while ((line = bufferedreader.readLine()) != null) {
                outputBuffer.append(line).append("\n");
            }
        
            // check for command failure
            try {
                if (process.waitFor() != 0) {
                    outputBuffer.append("exit value = ");
                    outputBuffer.append(process.exitValue());
                }
            }
            catch (InterruptedException e) {
            }
        } catch (IOException exc) {
        }
        
        return outputBuffer.toString();
    }

    
    /**
     * Modifies the loginwindow.plist file to either include or exclude
     * starting up LimeWire.
     */
    public static void setLoginStatus(boolean allow) {
        if (!hasNativeLibrary()) {
            return;
        }
        try {
            SetLoginStatusNative(allow);
        } catch(UnsatisfiedLinkError ule) {
            handleNativeBindingFailure(ule);
        }
    }
    
    /**
     * Gets the full user's name.
     */
    public static String getUserName() {
        if (!hasNativeLibrary()) {
            return CommonUtils.getUserName();
        }
        try {
            return GetCurrentFullUserName();
        } catch(UnsatisfiedLinkError ule) {
            // No big deal, just return user name.
            handleNativeBindingFailure(ule);
            
            return CommonUtils.getUserName();
        }
    }
    
    /**
     * Retrieves the app directory & name.
     * If the user is not running from the bundled app as we named it,
     * defaults to /Applications/WireShare/ as the directory of the app.
     */
    public static String getAppDir() {
        String appDir = "/Applications/WireShare/";
        String path = CommonUtils.getCurrentDirectory().getPath();
        int app = path.indexOf("WireShare.app");
        if(app != -1)
            appDir = path.substring(0, app);
        return appDir + APP_NAME;
    }

    /**
     * This sets LimeWire as the default handler for this file type.
     * @param fileType -- the file extension for the file. this will be used to look up the file type's UTI (universal type identifier)
     */
    public static void setLimewireAsDefaultFileTypeHandler(String fileType) {
        if (!hasNativeLibrary()) {
            return;
        }
        try {
            SetDefaultFileTypeHandler(fileType, LIMEWIRE_APPLICATION_BUNDLE_IDENTIFIER);
        } catch(UnsatisfiedLinkError ule) {
            handleNativeBindingFailure(ule);
        }
    }

    /**
     * This sets LimeWire as the default handler for this URL scheme.
     * @param url scheme -- the designator for the protocol, e.g. magnet
     */
    public static void setLimewireAsDefaultURLSchemeHandler(String urlScheme) {
        if (!hasNativeLibrary()) {
            return;
        }
        try {
            SetDefaultURLSchemeHandler(urlScheme, LIMEWIRE_APPLICATION_BUNDLE_IDENTIFIER);
        } catch(UnsatisfiedLinkError ule) {
            handleNativeBindingFailure(ule);
        }
    }

    /**
     * This checks whether LimeWire is the default handler for this file type.
     * @param fileType -- the file extension for the file. this will be used to look up the file type's UTI (universal type identifier)
     * @return true if LimeWire is the default handler for this file type
     */
    public static boolean isLimewireDefaultFileTypeHandler(String fileType) {
        if (!hasNativeLibrary()) {
            return true;
        }
        try {
            return IsApplicationTheDefaultFileTypeHandler(fileType, LIMEWIRE_APPLICATION_BUNDLE_IDENTIFIER);
        } catch(UnsatisfiedLinkError ule) {
            handleNativeBindingFailure(ule);
        }
        
        return true;
    }

    /**
     * This checks whether LimeWire is the default handler for this URL scheme.
     * @param urlScheme -- the designator for the protocol, e.g. magnet
     * @return true if LimeWire is the default handler for this URL scheme
     */
    public static boolean isLimewireDefaultURLSchemeHandler(String urlScheme) {
        if (!hasNativeLibrary()) {
            return true;
        }
        try {
            return IsApplicationTheDefaultURLSchemeHandler(urlScheme, LIMEWIRE_APPLICATION_BUNDLE_IDENTIFIER);
        } catch(UnsatisfiedLinkError ule) {
            handleNativeBindingFailure(ule);
        }
        
        return true;
    }
    
    /**
     * This checks whether any applications are registered as handlers for this fileType in the OS-X
     * launch services database.
     * 
     * @param fileType -- the file extension for the file. this will be used to look up the file type's UTI (universal type identifier)
     * @return true if any application is registered as a handler for this file type
     */
    public static boolean isFileTypeHandled(String fileType) {
        if (!hasNativeLibrary()) {
            return true;
        }
        try {
            return IsFileTypeHandled(fileType);
        } catch(UnsatisfiedLinkError ule) {
            handleNativeBindingFailure(ule);
        }
        
        return true;
    }

    /**
     * This checks whether any applications are registered as handlers for this URL scheme in the OS-X
     * launch services database.
     * 
     * @param urlScheme -- the designator for the protocol, e.g. magnet
     * @return true if any application is registered as a handler for this URL scheme
     */
    public static boolean isURLSchemeHandled(String urlScheme) {
        if (!hasNativeLibrary()) {
            return true;
        }
        try {
            return IsURLSchemeHandled(urlScheme);
        } catch(UnsatisfiedLinkError ule) {
            handleNativeBindingFailure(ule);
        }
        
        return true;
    }

    /**
     * This tries to change the file type handler for the given file type from LimeWire to another application.
     * Basically, it just changes the default handler application to the first application in the list
     * returned by launch services that isn't LimeWire. It might fail if no other handlers are registered for this file type.  
     * The list of handlers that are used internally in this method should not be shown to users as they are probably not understandable
     * by users.  For example LimeWire is represented by the application bundle identifier com.limegroup.gnutella.
     * 
     * @param fileType -- the file extension for the file. this will be used to look up the file type's UTI (universal type identifier)
     */
    public static void tryChangingDefaultFileTypeHandler(String fileType) {
        if (!hasNativeLibrary()) {
            return;
        }
        try {
            String[] handlers = GetAllHandlersForFileType(fileType);
            if (handlers != null) {
                for (String handler : handlers) {
                    if (!handler.equals(LIMEWIRE_APPLICATION_BUNDLE_IDENTIFIER)) {
                        SetDefaultFileTypeHandler(fileType, handler);
                        break;
                    }
                }
            }
        } catch(UnsatisfiedLinkError ule) {
            handleNativeBindingFailure(ule);
        }
    }

    /**
     * This method returns true if there are any other applications on the user's system that have
     * registered themselves as handlers for the given file type.
     * 
     * @param fileType -- the file extension for the file. this will be used to look up the file type's UTI (universal type identifier)
     */

    public static boolean canChangeDefaultFileTypeHandler(String fileType) {
        if (!hasNativeLibrary()) {
            return false;
        }
        try {
            String[] handlers = GetAllHandlersForFileType(fileType);
            if (handlers != null) {
                for (String handler : handlers) {
                    if (!handler.equals(LIMEWIRE_APPLICATION_BUNDLE_IDENTIFIER)) {
                        return true;
                    }
                }
            }
        } catch(UnsatisfiedLinkError ule) {
            handleNativeBindingFailure(ule);
        }
        
        return false;
    }
    
    /**
     * This tries to change the URL scheme handler for the given file type from LimeWire to another application.
     * Basically, it just changes the default handler application to the first application in the list
     * returned by launch services that isn't LimeWire. It might fail if no other handlers are registered for this URL scheme.  
     * The list of handlers that are used internally in this method should not be shown to users as they are probably not understandable
     * by users.  For example LimeWire is represented by the application bundle identifier com.limegroup.gnutella.
     * 
     * @param urlScheme -- the designator for the protocol, e.g. magnet
     */
    public static void tryChangingDefaultURLSchemeHandler(String urlScheme) {
        if (!hasNativeLibrary()) {
            return;
        }
        try {
            String[] handlers = GetAllHandlersForURLScheme(urlScheme);
            if (handlers != null) {
                for (String handler : handlers) {
                    if (!handler.equals(LIMEWIRE_APPLICATION_BUNDLE_IDENTIFIER)) {
                        SetDefaultURLSchemeHandler(urlScheme, handler);
                        break;
                    }
                }
            }
        } catch(UnsatisfiedLinkError ule) {
            handleNativeBindingFailure(ule);
        }
    }

    /**
     * This method returns true if there are any other applications on the user's system that have
     * registered themselves as handlers for the given URL scheme.
     * 
     * @param urlScheme -- the designator for the protocol, e.g. magnet
     */
    public static boolean canChangeDefaultURLSchemeHandler(String urlScheme) {
        if (!hasNativeLibrary()) {
            return false;
        }
        try {
            String[] handlers = GetAllHandlersForURLScheme(urlScheme);
            if (handlers != null) {
                for (String handler : handlers) {
                    if (!handler.equals(LIMEWIRE_APPLICATION_BUNDLE_IDENTIFIER)) {
                        return true;
                    }
                }
            }
        } catch(UnsatisfiedLinkError ule) {
            handleNativeBindingFailure(ule);
        }
        
        return false;
    }
    
    /**
     * Uses OS-X's launch services API to check whether any application has registered itself
     * as a handler for this file type. 
     */
    private static final native boolean IsFileTypeHandled(String fileType);

    /**
     * Uses OS-X's launch services API to check whether any application has registered itself
     * as a handler for this URL scheme. 
     */
    private static final native boolean IsURLSchemeHandled(String urlScheme);

    /**
     * Uses OS-X's launch services API to check whether the given application is the default handler for this
     * file type.
     */
    private static final native boolean IsApplicationTheDefaultFileTypeHandler(String fileType, String applicationBundleIdentifier);

    /**
     * Uses OS-X's launch services API to check whether the given application is the default handler for this
     * URL scheme.
     */
    private static final native boolean IsApplicationTheDefaultURLSchemeHandler(String urlScheme, String applicationBundleIdentifier);

    /**
     * Uses OS-X's launch services API to set the given application as the default handler for this file type.
     */
    private static final native int SetDefaultFileTypeHandler(String fileType, String applicationBundleIdentifier);

    /**
     * Uses OS-X's launch services API to set the given application as the default handler for this URL scheme.
     */
    private static final native int SetDefaultURLSchemeHandler(String urlScheme, String applicationBundleIdentifier);
    
    /**
     * Uses OS-X's launch services API to get all the handlers for this file type.
     */
    private static final native String[] GetAllHandlersForFileType(String fileType); 

    /**
     * Uses OS-X's launch services API to get all the handlers for this URL scheme.
     */
    private static final native String[] GetAllHandlersForURLScheme(String ulrScheme); 
    
    /**
     * Gets the full user's name.
     */
    private static final native String GetCurrentFullUserName();
    
    /**
     * [Un]registers LimeWire from the startup items list.
     */
    private static final native void SetLoginStatusNative(boolean allow);
}

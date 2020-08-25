package org.limewire.rudp;

import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

/*
 * Begin Java 1.8 Additions
 */
import java.net.SocketAddress;
import java.net.SocketOption;
import java.io.IOException;
/*
 * End Java 1.8 Additions
 */

import org.limewire.nio.AbstractNBSocket;

public abstract class AbstractNBSocketChannel extends SocketChannel {
    
    public AbstractNBSocketChannel(SelectorProvider provider) {
        super(provider);
    }

    @Override
    public abstract AbstractNBSocket socket(); 

    /*
     * Begin Java 1.8 Additions
     */	
	@Override
	public SocketAddress getRemoteAddress()
                              throws IOException{
		throw new IOException("not supported");			  
	}
	
	@Override
	public SocketAddress getLocalAddress()
                              throws IOException{
		throw new IOException("not supported");			  
	}
	
	@Override
	public SocketChannel shutdownOutput()
                              throws IOException{
		throw new IOException("not supported");			  
	}
	
	@Override
	public SocketChannel shutdownInput()
                              throws IOException{
		throw new IOException("not supported");			  
	}
	
	@Override
	public  <T> SocketChannel setOption(SocketOption<T> name,
                          T value)
                                 throws IOException {
		throw new IOException("not supported");		
	}
	
	@Override
	public  SocketChannel bind(SocketAddress local)
                            throws IOException {
		throw new IOException("not supported");							
	}
	/*
	 * End Java 1.8 Additions
	 */
}

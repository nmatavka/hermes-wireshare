package org.limewire.rudp;

import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import java.net.SocketAddress;
import java.net.SocketOption;
import java.io.IOException;

import java.util.Set;
import org.limewire.nio.AbstractNBSocket;

public abstract class AbstractNBSocketChannel extends SocketChannel {
    
    public AbstractNBSocketChannel(SelectorProvider provider) {
        super(provider);
    }

    @Override
    public abstract AbstractNBSocket socket(); 

	
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
}

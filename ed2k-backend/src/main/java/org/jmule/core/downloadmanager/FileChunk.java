/*
 *  JMule - Java file sharing client
 *  Copyright (C) 2007-2008 JMule team ( jmule@jmule.org / http://jmule.org )
 *
 *  Any parts of this program derived from other projects, or contributed
 *  by third-party developers are copyrighted by their respective authors.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

/*
 * Provenance note:
 * This file was extracted from the local JMule CVS donor tree by
 * tools/extract_jmule_backend.py and is now carried as WireShare-owned source.
 * The build must not depend on the donor jmule/ directory at compile time.
 */

package org.jmule.core.downloadmanager;

import java.nio.ByteBuffer;

/**
 * Created on 04-27-2008
 * @author binary256
 * @version $$Revision: 1.2 $$
 * Last changed by $$Author: javajox $$ on $$Date: 2008/08/02 14:21:10 $$
 */
public class FileChunk {
	private long chunkStart;
	private long chunkEnd;
	private ByteBuffer chunkData;
	
	public FileChunk(long chunkStart, long chunkEnd, ByteBuffer chunkData) {
		super();
		this.chunkStart = chunkStart;
		this.chunkEnd = chunkEnd;
		this.chunkData = chunkData;
	}
	
	public String toString() {
		return "[ "+chunkStart+" : "+chunkEnd+" ]";
	}
		
	public long getChunkStart() {
		return chunkStart;
	}
	
	public void setChunkStart(long chunkStart) {
		this.chunkStart = chunkStart;
	}
	
	public long getChunkEnd() {
		return chunkEnd;
	}
	
	public void setChunkEnd(long chunkEnd) {
		this.chunkEnd = chunkEnd;
	}
	
	public ByteBuffer getChunkData() {
		return chunkData;
	}
	
	public void setChunkData(ByteBuffer chunkData) {
		this.chunkData = chunkData;
	}
	
	public void clear() {
		chunkData.clear();
		chunkData.compact();
		chunkData.rewind();
		chunkData.limit(0);
	}	
}
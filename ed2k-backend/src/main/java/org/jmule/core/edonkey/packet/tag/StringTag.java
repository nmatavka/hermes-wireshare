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

package org.jmule.core.edonkey.packet.tag;

import java.nio.ByteBuffer;

import org.jmule.core.utils.Convert;
import org.jmule.core.utils.Misc;
import static org.jmule.core.edonkey.ED2KConstants.*;
/**
 * Created on Jul 15, 2009
 * @author binary256
 * @version $Revision: 1.3 $
 * Last changed by $Author: binary255 $ on $Date: 2010/06/15 16:47:06 $
 */
public class StringTag extends StandartTag {
	private String tagValue;
	private ByteBuffer byteBuffer;
	
	public StringTag(byte[] tagName, String tagValue) {
		super(TAGTYPE_STRING, tagName);
		this.tagValue = tagValue;
		updateByteBuffer();
	}
	
	private void updateByteBuffer() {
		byteBuffer = Misc.getByteBuffer(2 + tagValue.getBytes().length);
		byteBuffer.putShort(Convert.intToShort(tagValue.getBytes().length));
		byteBuffer.put(tagValue.getBytes());
	}
	
	ByteBuffer getValueAsByteBuffer() {
		byteBuffer.position(0);
		return byteBuffer;
	}

	int getValueLength() {
		return tagValue.getBytes().length + 2;
	}

	public Object getValue() {
		return tagValue;
	}


	public void setValue(Object object) {
		tagValue = (String)object;
		updateByteBuffer();
	}

}

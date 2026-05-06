/*
 * Provenance note:
 * This file was extracted from the local JMule CVS donor tree by
 * tools/extract_jmule_backend.py and is now carried as WireShare-owned source.
 * The build must not depend on the donor jmule/ directory at compile time.
 */

package org.jmule.core.bccrypto;

import java.io.InputStream;

abstract class LimitedInputStream
        extends InputStream
{
    protected final InputStream _in;

    LimitedInputStream(
        InputStream in)
    {
        this._in = in;
    }

    InputStream getUnderlyingStream()
    {
        return _in;
    }

    protected void setParentEofDetect(boolean on)
    {
       
    }
}

/*
 * Provenance note:
 * This file was extracted from the local JMule CVS donor tree by
 * tools/extract_jmule_backend.py and is now carried as WireShare-owned source.
 * The build must not depend on the donor jmule/ directory at compile time.
 */

package org.jmule.core.bccrypto;

import java.security.SecureRandom;

public class ParametersWithRandom
    implements CipherParameters
{
    private SecureRandom        random;
    private CipherParameters    parameters;

    public ParametersWithRandom(
        CipherParameters    parameters,
        SecureRandom        random)
    {
        this.random = random;
        this.parameters = parameters;
    }

    public ParametersWithRandom(
        CipherParameters    parameters)
    {
        this.random = null;
        this.parameters = parameters;
    }

    public SecureRandom getRandom()
    {
        if (random == null)
        {
            random = new SecureRandom();
        }
        return random;
    }

    public CipherParameters getParameters()
    {
        return parameters;
    }
}

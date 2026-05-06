/*
 * Provenance note:
 * This file was extracted from the local JMule CVS donor tree by
 * tools/extract_jmule_backend.py and is now carried as WireShare-owned source.
 * The build must not depend on the donor jmule/ directory at compile time.
 */

package org.jmule.core.bccrypto;

import java.math.BigInteger;

public class RSAKeyParameters
    extends AsymmetricKeyParameter
{
    private BigInteger      modulus;
    private BigInteger      exponent;

    public RSAKeyParameters(
        boolean     isPrivate,
        BigInteger  modulus,
        BigInteger  exponent)
    {
        super(isPrivate);

        this.modulus = modulus;
        this.exponent = exponent;
    }   

    public BigInteger getModulus()
    {
        return modulus;
    }

    public BigInteger getExponent()
    {
        return exponent;
    }
}

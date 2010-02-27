package org.picocontainer.gems;

import static org.picocontainer.Characteristics.*;

import java.util.Properties;

/**
 * A list of properties to allow switching on and off different characteristics at container 
 * construction time.
 * @author Michael Rimov
 */
public final class GemsCharacteristics {

	private static String _JMX = "jmx";
	
    private static final String _HOT_SWAP = "hotswap";
	
	
    private static final String _POOL = "pooled";
    
    /**
     * Turn off behavior for {@link org.picocontainer.gems.jmx.JMXExposing JMXExposing}
     */
    public static final Properties NO_JMX = immutable(_JMX, FALSE);

    /**
     * Turn on behavior for {@link org.picocontainer.gems.jmx.JMXExposing JMXExposing}
     */
    public static final Properties JMX = immutable(_JMX, TRUE);
    
    
    /**
     * Turn on hot-swapping behavior.
     */
    public static final Properties HOT_SWAP = immutable(_HOT_SWAP, TRUE);
    
    /**
     * Turn off hot swapping behavior.
     */
    public static final Properties NO_HOT_SWAP = immutable(_HOT_SWAP, FALSE);
    
    /**
     * Turn on pooling behavior.
     */
    public static final Properties POOL = immutable(_POOL, TRUE);
    
    /**
     * Turn off pooling behavior.
     */
    public static final Properties NO_POOL = immutable(_POOL, FALSE);

}

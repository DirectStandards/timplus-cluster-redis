package org.directtruststandards.timplus.cluster.cache;


import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.RedisClusteredCache;

/**
 * Redis specific implementation of a DelegatedClusteredCacheFactory interface.  
 * @author Greg Meyer
 * @since 1.0
 */
public class RedisDelegatedClusterCacheFactory implements DelegatedClusteredCacheFactory
{

	/**
	 * Empty constructor
	 */
	public RedisDelegatedClusterCacheFactory()
	{
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Cache<?, ?> createCache(final String name, final long maxSize, final long maxLifetime, final NodeID nodeId) 
	{
		return new RedisClusteredCache(name, maxSize, maxLifetime, nodeId);
	}
}

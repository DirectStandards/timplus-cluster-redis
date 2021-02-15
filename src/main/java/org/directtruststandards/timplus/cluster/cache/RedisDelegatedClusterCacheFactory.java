package org.directtruststandards.timplus.cluster.cache;


import java.io.Serializable;
import java.util.List;

import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.openfire.session.DomainPair;
import org.jivesoftware.openfire.spi.ClientRoute;
import org.jivesoftware.openfire.spi.RoutingTableImpl;
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
		Cache<?, ?> retVal = null;
		
		switch (name)
		{
			case RoutingTableImpl.ANONYMOUS_C2S_CACHE_NAME:
			case RoutingTableImpl.C2S_CACHE_NAME:
			{
				retVal =  new StringClientRouteCache<String, ClientRoute>(name, maxSize, maxLifetime, nodeId);
				break;
			}
			case RoutingTableImpl.S2S_CACHE_NAME:
			{
				retVal =  new DomainPairNodeIdRouteCache<DomainPair, NodeID>(name, maxSize, maxLifetime, nodeId);
				break;
			}
			case RoutingTableImpl.COMPONENT_CACHE_NAME:
			{
				retVal =  new StringNodeIdListRouteCache<String, List<NodeID>>(name, maxSize, maxLifetime, nodeId);
				break;
			}
			case RoutingTableImpl.C2S_SESSION_NAME:
			{
				retVal =  new StringStringListRouteCache<String, List<String>>(name, maxSize, maxLifetime, nodeId);
				break;
			}	
			default:
				retVal = new GenericRouteCache<Serializable, Serializable>(name, maxSize, maxLifetime, nodeId); 
		}
		
		return retVal;
	}
	
	public static class StringClientRouteCache<K extends String, V extends ClientRoute> extends RedisClusteredCache<K,V>
	{
		public StringClientRouteCache(String name, long maxSize, long maxLifetime, NodeID nodeId) {
			super(name, maxSize, maxLifetime, nodeId);
		}	
	}
	
	public static class DomainPairNodeIdRouteCache<K extends DomainPair, V extends NodeID> extends RedisClusteredCache<K,V>
	{
		public DomainPairNodeIdRouteCache(String name, long maxSize, long maxLifetime, NodeID nodeId) {
			super(name, maxSize, maxLifetime, nodeId);
		}	
	}	
	
	public static class StringNodeIdListRouteCache<K extends String, V extends List<NodeID>> extends RedisClusteredCache<K,V>
	{
		public StringNodeIdListRouteCache(String name, long maxSize, long maxLifetime, NodeID nodeId) {
			super(name, maxSize, maxLifetime, nodeId);
		}	
	}	
	
	public static class StringStringListRouteCache<K extends String, V extends List<String>> extends RedisClusteredCache<K,V>
	{
		public StringStringListRouteCache(String name, long maxSize, long maxLifetime, NodeID nodeId) {
			super(name, maxSize, maxLifetime, nodeId);
		}	
	}
	
	public static class GenericRouteCache<K extends Serializable, V extends Serializable> extends RedisClusteredCache<K,V>
	{
		public GenericRouteCache(String name, long maxSize, long maxLifetime, NodeID nodeId) {
			super(name, maxSize, maxLifetime, nodeId);
		}	
	}		
}

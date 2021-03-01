package org.directtruststandards.timplus.cluster.cache;


import java.io.Serializable;
import java.util.List;

import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.openfire.filetransfer.proxy.ClusterCrossProxyInfo;
import org.jivesoftware.openfire.filetransfer.proxy.ProxyConnectionManager;
import org.jivesoftware.openfire.muc.spi.LocalMUCRoom;
import org.jivesoftware.openfire.muc.spi.LocalMUCRoomManager;
import org.jivesoftware.openfire.roster.Roster;
import org.jivesoftware.openfire.session.ClientSessionInfo;
import org.jivesoftware.openfire.session.DomainPair;
import org.jivesoftware.openfire.spi.ClientRoute;
import org.jivesoftware.openfire.spi.RoutingTableImpl;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.ClusteredCacheFactory;
import org.jivesoftware.util.cache.DefaultExternalizableUtilStrategy;
import org.jivesoftware.util.cache.ExternalizableUtil;
import org.jivesoftware.util.cache.RedisClusteredCache;

/**
 * Redis specific implementation of a DelegatedClusteredCacheFactory interface.  
 * @author Greg Meyer
 * @since 1.0
 */
public class RedisDelegatedClusterCacheFactory implements DelegatedClusteredCacheFactory
{
	static
	{
		ExternalizableUtil.getInstance().setStrategy(new DefaultExternalizableUtilStrategy());
	}
	
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
	public Cache<?, ?> createCache(final String name, final long maxSize, final long maxLifetime, final NodeID nodeId, boolean nodePurgable) 
	{
		Cache<?, ?> retVal = null;
		
		switch (name)
		{
			case RoutingTableImpl.ANONYMOUS_C2S_CACHE_NAME:
			case RoutingTableImpl.C2S_CACHE_NAME:
			{
				retVal =  new StringClientRouteCache<String, ClientRoute>(name, maxSize, maxLifetime, nodeId, nodePurgable);
				break;
			}
			case RoutingTableImpl.S2S_CACHE_NAME:
			{
				retVal =  new DomainPairNodeIdRouteCache<DomainPair, NodeID>(name, maxSize, maxLifetime, nodeId, nodePurgable);
				break;
			}
			case RoutingTableImpl.COMPONENT_CACHE_NAME:
			{
				retVal =  new StringNodeIdListRouteCache<String, List<NodeID>>(name, maxSize, maxLifetime, nodeId, nodePurgable);
				break;
			}
			case RoutingTableImpl.C2S_SESSION_NAME:
			{
				retVal =  new StringStringListRouteCache<String, List<String>>(name, maxSize, maxLifetime, nodeId, nodePurgable);
				break;
			}
			case SessionManager.C2S_INFO_CACHE_NAME:
			{
				retVal =  new StringClientSessionInfoCache<String, ClientSessionInfo>(name, maxSize, maxLifetime, nodeId, nodePurgable);
				break;
			}	
			case ClusteredCacheFactory.ROSTER_CACHE_NAME:
			{
				retVal =  new StringRosterCache<String, Roster>(name, maxSize, maxLifetime, nodeId, nodePurgable);
				break;
			}	
			case ProxyConnectionManager.CLUSTER_CROSS_PROXY_MAP_CACHE_NAME:
			{
				retVal =  new StringClusterCrossProxyInfoCache<String, ClusterCrossProxyInfo>(name, maxSize, maxLifetime, nodeId, nodePurgable);
				break;
			}				
			default:
			
			{
				if (name.startsWith(LocalMUCRoomManager.LOCAL_ROOM_MANAGER_CACHE_BASE_NAME))
				{
					retVal =  new StringLocalMUCRoomCache<String, LocalMUCRoom>(name, maxSize, maxLifetime, nodeId, nodePurgable);
				}
				else
					retVal = new GenericRouteCache<Serializable, Serializable>(name, maxSize, maxLifetime, nodeId, nodePurgable); 
			}
		}
		
		return retVal;
	}
	
	public static class StringClientRouteCache<K extends String, V extends ClientRoute> extends RedisClusteredCache<K,V>
	{
		public StringClientRouteCache(String name, long maxSize, long maxLifetime, NodeID nodeId, boolean nodePurgable) {
			super(name, maxSize, maxLifetime, nodeId, nodePurgable);
		}	
	}
	
	public static class DomainPairNodeIdRouteCache<K extends DomainPair, V extends NodeID> extends RedisClusteredCache<K,V>
	{
		public DomainPairNodeIdRouteCache(String name, long maxSize, long maxLifetime, NodeID nodeId, boolean nodePurgable) {
			super(name, maxSize, maxLifetime, nodeId, nodePurgable);
		}	
	}	
	
	public static class StringNodeIdListRouteCache<K extends String, V extends List<NodeID>> extends RedisClusteredCache<K,V>
	{
		public StringNodeIdListRouteCache(String name, long maxSize, long maxLifetime, NodeID nodeId, boolean nodePurgable) {
			super(name, maxSize, maxLifetime, nodeId, nodePurgable);
		}	
	}	
	
	public static class StringStringListRouteCache<K extends String, V extends List<String>> extends RedisClusteredCache<K,V>
	{
		public StringStringListRouteCache(String name, long maxSize, long maxLifetime, NodeID nodeId, boolean nodePurgable) {
			super(name, maxSize, maxLifetime, nodeId, nodePurgable);
		}	
	}
	
	public static class StringClientSessionInfoCache<K extends String, V extends ClientSessionInfo> extends RedisClusteredCache<K,V>
	{
		public StringClientSessionInfoCache(String name, long maxSize, long maxLifetime, NodeID nodeId, boolean nodePurgable) {
			super(name, maxSize, maxLifetime, nodeId, nodePurgable);
		}			
	}
	
	public static class StringRosterCache<K extends String, V extends Roster> extends RedisClusteredCache<K,V>
	{
		public StringRosterCache(String name, long maxSize, long maxLifetime, NodeID nodeId, boolean nodePurgable) {
			super(name, maxSize, maxLifetime, nodeId, nodePurgable);
		}	
		
		@Override
		public boolean isSingletonCrossClusterCache()
		{
			return true;
		}
	}	
	
	public static class StringClusterCrossProxyInfoCache<K extends String, V extends ClusterCrossProxyInfo> extends RedisClusteredCache<K,V>
	{
		public StringClusterCrossProxyInfoCache(String name, long maxSize, long maxLifetime, NodeID nodeId, boolean nodePurgable) {
			super(name, maxSize, maxLifetime, nodeId, nodePurgable);
		}		
	}
	
	public static class StringLocalMUCRoomCache<K extends String, V extends LocalMUCRoom> extends RedisClusteredCache<K,V>
	{
		public StringLocalMUCRoomCache(String name, long maxSize, long maxLifetime, NodeID nodeId, boolean nodePurgable) {
			super(name, maxSize, maxLifetime, nodeId, nodePurgable);
		}		
		
		@Override
		public boolean isSingletonCrossClusterCache()
		{
			return true;
		}
	}
	
	public static class GenericRouteCache<K extends Serializable, V extends Serializable> extends RedisClusteredCache<K,V>
	{
		public GenericRouteCache(String name, long maxSize, long maxLifetime, NodeID nodeId, boolean nodePurgable) {
			super(name, maxSize, maxLifetime, nodeId, nodePurgable);
		}	
	}		
}

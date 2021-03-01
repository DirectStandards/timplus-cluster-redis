package org.directtruststandards.timplus.cluster.cache;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.DomainPairNodeIdRouteCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.GenericRouteCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringClientRouteCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringClientSessionInfoCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringLocalMUCRoomCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringNodeIdListRouteCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringStringListRouteCache;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.openfire.muc.spi.LocalMUCRoomManager;
import org.jivesoftware.openfire.spi.RoutingTableImpl;
import org.jivesoftware.util.cache.Cache;
import org.junit.jupiter.api.Test;

public class RedisDelegatedClusterCacheFactory_createCacheTest extends SpringBaseTest
{
	@Test
	public void testCreateCache_assertCacheCreated()
	{
		final RedisDelegatedClusterCacheFactory factory = new RedisDelegatedClusterCacheFactory();
			
		Cache<?, ?> cache = factory.createCache("Junit", 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}), true);
		
		assertNotNull(cache);
		assertTrue(cache instanceof GenericRouteCache);
		
		cache = factory.createCache(RoutingTableImpl.ANONYMOUS_C2S_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}), true);
		
		assertNotNull(cache);
		assertTrue(cache instanceof StringClientRouteCache);
		
		cache = factory.createCache(RoutingTableImpl.C2S_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}) , true);
		
		assertNotNull(cache);
		assertTrue(cache instanceof StringClientRouteCache);
		
		cache = factory.createCache(RoutingTableImpl.S2S_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}), true );
		
		assertNotNull(cache);
		assertTrue(cache instanceof DomainPairNodeIdRouteCache);
		
		cache = factory.createCache(RoutingTableImpl.COMPONENT_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}), true );
		
		assertNotNull(cache);
		assertTrue(cache instanceof StringNodeIdListRouteCache);
		
		cache = factory.createCache(RoutingTableImpl.C2S_SESSION_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}), true );
		
		assertNotNull(cache);
		assertTrue(cache instanceof StringStringListRouteCache);
		
		cache = factory.createCache(SessionManager.C2S_INFO_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}), true );
		
		assertNotNull(cache);
		assertTrue(cache instanceof StringClientSessionInfoCache);	
		
		cache = factory.createCache(LocalMUCRoomManager.LOCAL_ROOM_MANAGER_CACHE_BASE_NAME + "somedomain", 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}), false );
		
		assertNotNull(cache);
		assertTrue(cache instanceof StringLocalMUCRoomCache);
		assertFalse(cache.isNodeCachePurgeable());
	}
}

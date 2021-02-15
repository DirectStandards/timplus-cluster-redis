package org.directtruststandards.timplus.cluster.cache;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.DomainPairNodeIdRouteCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.GenericRouteCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringClientRouteCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringNodeIdListRouteCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringStringListRouteCache;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.openfire.spi.RoutingTableImpl;
import org.jivesoftware.util.cache.Cache;
import org.junit.jupiter.api.Test;

public class RedisDelegatedClusterCacheFactory_createCacheTest extends SpringBaseTest
{
	@Test
	public void testCreateCache_assertCacheCreated()
	{
		final RedisDelegatedClusterCacheFactory factory = new RedisDelegatedClusterCacheFactory();
			
		Cache<?, ?> cache = factory.createCache("Junit", 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}) );
		
		assertNotNull(cache);
		assertTrue(cache instanceof GenericRouteCache);
		
		cache = factory.createCache(RoutingTableImpl.ANONYMOUS_C2S_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}) );
		
		assertNotNull(cache);
		assertTrue(cache instanceof StringClientRouteCache);
		
		cache = factory.createCache(RoutingTableImpl.C2S_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}) );
		
		assertNotNull(cache);
		assertTrue(cache instanceof StringClientRouteCache);
		
		cache = factory.createCache(RoutingTableImpl.S2S_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}) );
		
		assertNotNull(cache);
		assertTrue(cache instanceof DomainPairNodeIdRouteCache);
		
		cache = factory.createCache(RoutingTableImpl.COMPONENT_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}) );
		
		assertNotNull(cache);
		assertTrue(cache instanceof StringNodeIdListRouteCache);
		
		cache = factory.createCache(RoutingTableImpl.C2S_SESSION_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}) );
		
		assertNotNull(cache);
		assertTrue(cache instanceof StringStringListRouteCache);
	}
}

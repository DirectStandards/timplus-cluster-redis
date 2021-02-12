package org.directtruststandards.timplus.cluster.cache;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jivesoftware.openfire.cluster.NodeID;
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
	}
	
	
}

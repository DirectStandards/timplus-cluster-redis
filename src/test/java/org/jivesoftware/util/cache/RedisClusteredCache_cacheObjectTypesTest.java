package org.jivesoftware.util.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.contains;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.DomainPairNodeIdRouteCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringNodeIdListRouteCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringStringListRouteCache;
import org.directtruststandards.timplus.cluster.cache.SpringBaseTest;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.openfire.session.DomainPair;
import org.junit.jupiter.api.Test;

public class RedisClusteredCache_cacheObjectTypesTest extends SpringBaseTest
{
	@SuppressWarnings("unchecked")
	@Test
	public void testCacheObject_genericType_assertCached()
	{
		final Cache<Serializable, Serializable> cache = 
				(Cache<Serializable, Serializable>)new RedisDelegatedClusterCacheFactory().createCache("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		cache.put("TestKey", "TestValue");
		
		assertEquals("TestValue", cache.get("TestKey"));
	}	
	
	@Test
	public void testCacheObject_stringListType_assertCached()
	{
		final Cache<String, ArrayList<String>> cache = new StringStringListRouteCache<>("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		final String str1 = "value1";
		final String str2 = "value2";
		
		ArrayList<String> strings = new ArrayList<>();
		strings.add(str1);
		strings.add(str2);		
		
		cache.put("TestKey", strings);
		
		final List<String> retStrings = (List<String>)cache.get("TestKey");
		
		assertThat(retStrings, contains(str1, str2));
	}		
	
	@Test
	public void testCacheObject_nodeIdList_assertCached()
	{
		final Cache<String, List<NodeID>> cache = new StringNodeIdListRouteCache<>("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		NodeID node1 = NodeID.getInstance(new byte[] {0,0,0,1});
		NodeID node2 = NodeID.getInstance(new byte[] {0,0,0,1});
		
		List<NodeID> nodes = new ArrayList<>();
		nodes.add(node1);
		nodes.add(node2);
		
		cache.put("TestKey", nodes);
		
		List<NodeID> retNodes = (List<NodeID>)cache.get("TestKey");
		
		assertThat(retNodes, contains(node1, node2));
	}	
	
	@Test
	public void testCacheObject_domainPairKey_nodeIdValue_assertCached()
	{
		final Cache<DomainPair, NodeID> cache = new DomainPairNodeIdRouteCache<>("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		NodeID node = NodeID.getInstance(new byte[] {0,0,0,1});
		
		final DomainPair key = new DomainPair("gm2552@cerner.com", "direct.securehealthemail.com");
		
		cache.put(key, node);
		
		assertEquals(node, cache.get(key));
	}		
	
	@Test
	public void testCacheObject_customAggregate_assertCached()
	{
		final CustomAggregate aggr = new  CustomAggregate();
		
		aggr.setValue("test");
		
		final CustomAggregateRedisClusteredCache<String, CustomAggregate> cache = 
				new CustomAggregateRedisClusteredCache<>("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		cache.put("TestKey", aggr);
		
		assertEquals(aggr.getValue(), cache.get("TestKey").getValue());
	}	
	
	@Test
	public void testCacheObject_customAggregateList_localAndRemoteNodeCaches_assertCached()
	{
		final CustomAggregate aggr1 = new  CustomAggregate();
		
		aggr1.setValue("value1");
		
		final CustomAggregate aggr2 = new  CustomAggregate();
		
		aggr2.setValue("value2");
		
		final CustomAggregateListRedisClusteredCache<String, List<CustomAggregate>> cacheLocal = 
				new CustomAggregateListRedisClusteredCache<>("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		final CustomAggregateListRedisClusteredCache<String, List<CustomAggregate>>cacheRemote = 
				new CustomAggregateListRedisClusteredCache<>("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,1}));
		
		cacheLocal.put("TestKey", Collections.singletonList(aggr1));
		
		cacheRemote.put("TestKey", Collections.singletonList(aggr2));
		
		List<CustomAggregate> cachedVals = cacheLocal.get("TestKey");
		
		assertEquals(2, cachedVals.size());
		
		cacheLocal.purgeClusteredNodeCaches(NodeID.getInstance(new byte[] {0,0,0,0}));
		
		cachedVals = cacheLocal.get("TestKey");
		
		assertEquals(1, cachedVals.size());
	}		
	
	protected static class CustomAggregate
	{
		private String value;
		
		public CustomAggregate()
		{
			
		}
		
		public String getValue() 
		{
			return value;
		}

		public void setValue(String value) 
		{
			this.value = value;
		}		
	}
	
	protected static class CustomAggregateRedisClusteredCache<K extends String, V extends CustomAggregate> extends RedisClusteredCache<K, V>
	{

		public CustomAggregateRedisClusteredCache(java.lang.String name, long maxSize, long maxLifetime,
				NodeID nodeId) {
			super(name, maxSize, maxLifetime, nodeId);
		}

	}
	
	protected static class CustomAggregateListRedisClusteredCache<K extends String, V extends List<CustomAggregate>> extends RedisClusteredCache<K, V>
	{

		public CustomAggregateListRedisClusteredCache(java.lang.String name, long maxSize, long maxLifetime,
				NodeID nodeId) {
			super(name, maxSize, maxLifetime, nodeId);
		}

	}
}

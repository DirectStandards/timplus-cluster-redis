package org.jivesoftware.util.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.contains;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.directtruststandards.timplus.cluster.cache.SpringBaseTest;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.openfire.session.DomainPair;
import org.junit.jupiter.api.Test;

public class RedisClusteredCache_cacheObjectTypesTest extends SpringBaseTest
{
	@Test
	public void testCacheObject_stringType_assertCached()
	{
		final RedisClusteredCache cache = new RedisClusteredCache("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		cache.put("TestKey", "TestValue");
		
		assertEquals("TestValue", cache.get("TestKey"));
	}	
	
	@Test
	public void testCacheObject_stringListType_assertCached()
	{
		final RedisClusteredCache cache = new RedisClusteredCache("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		final String str1 = "value1";
		final String str2 = "value2";
		
		List<String> strings = new ArrayList<>();
		strings.add(str1);
		strings.add(str2);		
		
		final List<String> retStrings = (List<String>)cache.put("TestKey", strings);
		
		assertThat(retStrings, contains(str1, str2));
	}		
	
	@Test
	public void testCacheObject_nodeIdList_assertCached()
	{
		final RedisClusteredCache cache = new RedisClusteredCache("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		NodeID node1 = NodeID.getInstance(new byte[] {0,0,0,1});
		NodeID node2 = NodeID.getInstance(new byte[] {0,0,0,1});
		
		List<NodeID> nodes = new ArrayList<>();
		nodes.add(node1);
		nodes.add(node2);
		
		cache.put("TestKey", nodes);
		
		List<NodeID> retNodes = (List<NodeID>)cache.get("TestKey");
		
		assertThat(nodes, contains(node1, node2));
	}	
	
	@Test
	public void testCacheObject_nodeId_assertCached()
	{
		final RedisClusteredCache cache = new RedisClusteredCache("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		NodeID node = NodeID.getInstance(new byte[] {0,0,0,1});
		
		cache.put("TestKey", node);
		
		NodeID retNode = (NodeID)cache.get("TestKey");
		
		assertEquals(retNode, cache.get("TestKey"));
	}	
	
	@Test
	public void testCacheObject_domainPairKey_nodeIdValue_assertCached()
	{
		final RedisClusteredCache cache = new RedisClusteredCache("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		NodeID node = NodeID.getInstance(new byte[] {0,0,0,1});
		
		final DomainPair key = new DomainPair("gm2552@cerner.com", "direct.securehealthemail.com");
		
		cache.put(key, node);
		
		assertEquals(node, cache.get(key));
	}		
}

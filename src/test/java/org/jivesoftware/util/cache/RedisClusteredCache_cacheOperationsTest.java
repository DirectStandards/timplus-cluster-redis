package org.jivesoftware.util.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.directtruststandards.timplus.cluster.cache.RedisCacheEntry;
import org.directtruststandards.timplus.cluster.cache.SpringBaseTest;
import org.jivesoftware.openfire.cluster.NodeID;
import org.junit.jupiter.api.Test;

public class RedisClusteredCache_cacheOperationsTest extends SpringBaseTest
{
	@Test
	public void testCacheObjects_localCache_cacheAndRetrieve() throws Exception
	{
		final RedisClusteredCache cache = new RedisClusteredCache("JUnitCache", 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		cache.put("TestKey", "TestValue");
		
		assertEquals("TestValue", cache.locallyCached.get("TestKey"));
		
		assertEquals("TestValue", cache.remotelyCached.findById("JUnitCacheTestKey").get().getValue());
		
		assertEquals("TestValue", cache.get("TestKey"));
	}
	
	@Test
	public void testCacheObjects_localCache_cacheAndRemove() throws Exception
	{
		final RedisClusteredCache cache = new RedisClusteredCache("JUnitCache", 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		cache.put("TestKey", "TestValue");
		
		assertEquals("TestValue", cache.locallyCached.get("TestKey"));
		
		assertEquals("TestValue", cache.get("TestKey"));
		
		cache.remove("TestKey");
		
		assertNull(cache.get("TestKey"));
		
		assertEquals(0, cache.size());
	}	
	
	@Test
	public void testCacheObjects_localCache_cacheAndContainsKey() throws Exception
	{
		final RedisClusteredCache cache = new RedisClusteredCache("JUnitCache", 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		cache.put("TestKey", "TestValue");
		
		assertTrue(cache.containsKey("TestKey"));
	}	
	
	@Test
	public void testCacheObjects_localCache_cacheAndContainsValue() throws Exception
	{
		final RedisClusteredCache cache = new RedisClusteredCache("JUnitCache", 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		cache.put("TestKey", "TestValue");
		
		assertTrue(cache.containsValue("TestValue"));
	}
		
	@Test
	public void testCacheObjects_remoteCache_retrieveRemoteObject() throws Exception
	{	
		final String entryValue =  UUID.randomUUID().toString();
		
		final String cacheName = UUID.randomUUID().toString();
		
		final String entryKey = UUID.randomUUID().toString();
		
		final NodeID remoteNode = NodeID.getInstance(UUID.randomUUID().toString().getBytes());
		
		final String remoteNodeCacheName = cacheName + remoteNode.toString();
		
		final RedisCacheEntry entry = new RedisCacheEntry(cacheName + entryKey , cacheName, remoteNodeCacheName, (Serializable)entryValue, 600000L);
		
		redisRepo.save(entry);
		
		final RedisClusteredCache cache = new RedisClusteredCache(cacheName, 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		assertEquals(entryValue, cache.get(entryKey));
		assertEquals(0, cache.locallyCached.size());
	}
	
	@Test
	public void testCacheObjects_remoteCache_cacheAndContainsKey() throws Exception
	{
		final String entryValue =  UUID.randomUUID().toString();
		
		final String cacheName = UUID.randomUUID().toString();
		
		final String entryKey = UUID.randomUUID().toString();
		
		final NodeID remoteNode = NodeID.getInstance(UUID.randomUUID().toString().getBytes());
		
		final String remoteNodeCacheName = cacheName + remoteNode.toString();
		
		final RedisCacheEntry entry = new RedisCacheEntry(cacheName + entryKey , cacheName, remoteNodeCacheName, (Serializable)entryValue, 600000L);
		
		redisRepo.save(entry);
		
		final RedisClusteredCache cache = new RedisClusteredCache(cacheName, 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		assertTrue(cache.containsKey(entryKey));
	}	
	
	@Test
	public void testCacheObjects_remoteCache_cacheAndContainsValue() throws Exception
	{
		final String entryValue =  UUID.randomUUID().toString();
		
		final String cacheName = UUID.randomUUID().toString();
		
		final String entryKey = UUID.randomUUID().toString();
		
		final NodeID remoteNode = NodeID.getInstance(UUID.randomUUID().toString().getBytes());
		
		final String remoteNodeCacheName = cacheName + remoteNode.toString();
		
		final RedisCacheEntry entry = new RedisCacheEntry(cacheName + entryKey , cacheName, remoteNodeCacheName, (Serializable)entryValue, 50000L);
		
		redisRepo.save(entry);
		
		final RedisClusteredCache cache = new RedisClusteredCache(cacheName, 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		assertTrue(cache.containsValue(entryValue));
	}		
	
	@Test
	public void testCacheObjects_retrieveAllValues() throws Exception
	{	
		final String entryValue =  UUID.randomUUID().toString();
		
		final String cacheName = UUID.randomUUID().toString();
		
		final String entryKey = UUID.randomUUID().toString();
		
		final NodeID remoteNode = NodeID.getInstance(UUID.randomUUID().toString().getBytes());
		
		final String remoteNodeCacheName = cacheName + remoteNode.toString();
		
		final RedisCacheEntry entry = new RedisCacheEntry(cacheName + entryKey , cacheName, remoteNodeCacheName, (Serializable)entryValue, 600000L);
		
		redisRepo.save(entry);
		
		final RedisClusteredCache cache = new RedisClusteredCache(cacheName, 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		final Collection<?> values = cache.values();
		
		assertEquals(1, values.size());
		
		assertThat(values, contains(entryValue));
	}
	
	@Test
	public void testCacheObjects_retrieveFullEntrySet() throws Exception
	{	
		final String entryValue =  UUID.randomUUID().toString();
		
		final String cacheName = UUID.randomUUID().toString();
		
		final String entryKey = UUID.randomUUID().toString();
		
		final NodeID remoteNode = NodeID.getInstance(UUID.randomUUID().toString().getBytes());
		
		final String remoteNodeCacheName = cacheName + remoteNode.toString();
		
		final RedisCacheEntry entry = new RedisCacheEntry(cacheName + entryKey , cacheName, remoteNodeCacheName, (Serializable)entryValue, 600000L);
		
		redisRepo.save(entry);
		
		final RedisClusteredCache cache = new RedisClusteredCache(cacheName, 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		final Set<Entry<Object, Object>> entrySet = cache.entrySet();
		
		assertEquals(1, entrySet.size());
		
		final Entry<?, ?> retEntry = entrySet.iterator().next();
		
		assertEquals(entryKey, retEntry.getKey());
		
		assertEquals(entryValue, retEntry.getValue());
	}
	
	@Test
	public void testCacheObjects_retrieveKeySet() throws Exception
	{	
		final String entryValue =  UUID.randomUUID().toString();
		
		final String cacheName = UUID.randomUUID().toString();
		
		final String entryKey = UUID.randomUUID().toString();
		
		final NodeID remoteNode = NodeID.getInstance(UUID.randomUUID().toString().getBytes());
		
		final String remoteNodeCacheName = cacheName + remoteNode.toString();
		
		final RedisCacheEntry entry = new RedisCacheEntry(cacheName + entryKey , cacheName, remoteNodeCacheName, (Serializable)entryValue, 600000L);
		
		redisRepo.save(entry);
		
		final RedisClusteredCache cache = new RedisClusteredCache(cacheName, 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		final Set<Entry<Object, Object>> entrySet = cache.entrySet();
		
		assertEquals(1, entrySet.size());
		
		final Collection<?> keys = cache.keySet();
		
		assertEquals(1, keys.size());
		
		assertThat(keys, contains(entryKey));
	}
	
	@Test
	public void testCacheObjects_retrieveSize() throws Exception
	{
		final RedisClusteredCache cache = new RedisClusteredCache("JUnitCache", 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		cache.put("TestKey", "TestValue");
		
		assertEquals("TestValue", cache.get("TestKey"));
		
		assertEquals(1, cache.size());
	}
	
	@Test
	public void testCacheObjects_isEmpty() throws Exception
	{
		final RedisClusteredCache cache = new RedisClusteredCache("JUnitCache", 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		assertTrue(cache.isEmpty());
		
		cache.put("TestKey", "TestValue");
		
		assertFalse(cache.isEmpty());
	}
	
	@Test
	public void testCacheObjects_purgeNodeCacheByNode() throws Exception
	{	
		final String entryValue =  UUID.randomUUID().toString();
		
		final String cacheName = UUID.randomUUID().toString();
		
		final String entryKey = UUID.randomUUID().toString();
		
		final NodeID remoteNode = NodeID.getInstance(UUID.randomUUID().toString().getBytes());
		
		final String remoteNodeCacheName = cacheName + remoteNode.toString();
		
		final RedisCacheEntry entry = new RedisCacheEntry(cacheName + entryKey , cacheName, remoteNodeCacheName, (Serializable)entryValue, 600000L);
		
		redisRepo.save(entry);
		
		final RedisClusteredCache cache = new RedisClusteredCache(cacheName, 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		cache.purgeClusteredNodeCaches(NodeID.getInstance(new byte[] {0,0,0,0}));
		
		assertEquals(1, cache.size());
		
		cache.purgeClusteredNodeCaches(remoteNode);
		
		assertEquals(0, cache.size());
	}	
	
	@Test
	public void testCacheObjects_clear() throws Exception
	{	
		final RedisClusteredCache cache = new RedisClusteredCache("JUnitCache", 50, 50000, NodeID.getInstance(new byte[] {0,0,0,0}));
		
		cache.put("TestKey", "TestValue");
		
		assertEquals("TestValue", cache.get("TestKey"));
		
		assertEquals(1, cache.locallyCached.size());
		assertEquals(1, cache.size());
		
		cache.clear();
		
		assertEquals(0, cache.locallyCached.size());
		assertEquals(0, cache.size());
	}		
}

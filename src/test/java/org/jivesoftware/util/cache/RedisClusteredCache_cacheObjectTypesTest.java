package org.jivesoftware.util.cache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.CrossClusterStringStringMapCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.DomainPairNodeIdRouteCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringClientSessionInfoCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringClusterCrossProxyInfoCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringNodeIdListRouteCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringRosterCache;
import org.directtruststandards.timplus.cluster.cache.RedisDelegatedClusterCacheFactory.StringStringListRouteCache;
import org.directtruststandards.timplus.cluster.cache.SpringBaseTest;
import org.jivesoftware.openfire.cluster.ClusterNode;
import org.jivesoftware.openfire.cluster.ClusterNodeStatus;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.openfire.filetransfer.proxy.ClusterCrossProxyInfo;
import org.jivesoftware.openfire.filetransfer.proxy.credentials.ProxyServerCredential;
import org.jivesoftware.openfire.muc.spi.LocalMUCRoom;
import org.jivesoftware.openfire.muc.spi.LocalMUCRoomManager;
import org.jivesoftware.openfire.muc.spi.RemoteMUCCache;
import org.jivesoftware.openfire.roster.Roster;
import org.jivesoftware.openfire.roster.RosterItem;
import org.jivesoftware.openfire.roster.RosterItem.AskType;
import org.jivesoftware.openfire.roster.RosterItem.RecvType;
import org.jivesoftware.openfire.roster.RosterItem.SubType;
import org.jivesoftware.openfire.session.ClientSessionInfo;
import org.jivesoftware.openfire.session.DomainPair;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.junit.jupiter.api.Test;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

public class RedisClusteredCache_cacheObjectTypesTest extends SpringBaseTest
{
	@SuppressWarnings("unchecked")
	@Test
	public void testCacheObject_genericType_assertCached()
	{
		final Cache<Serializable, Serializable> cache = 
				(Cache<Serializable, Serializable>)new RedisDelegatedClusterCacheFactory().createCache("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}), true);
		
		cache.put("TestKey", "TestValue");
		
		assertEquals("TestValue", cache.get("TestKey"));
	}	
	
	@Test
	public void testCacheObject_stringListType_assertCached()
	{
		final Cache<String, ArrayList<String>> cache = new StringStringListRouteCache<>("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}), true);
		
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
		final Cache<String, List<NodeID>> cache = new StringNodeIdListRouteCache<>("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}), true);
		
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
		final Cache<DomainPair, NodeID> cache = new DomainPairNodeIdRouteCache<>("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}), true);
		
		NodeID node = NodeID.getInstance(new byte[] {0,0,0,1});
		
		final DomainPair key = new DomainPair("gm2552@cerner.com", "direct.securehealthemail.com");
		
		cache.put(key, node);
		
		assertEquals(node, cache.get(key));
	}		
	
	@Test
	public void testCacheObject_stringClientInfoSession_assertCached()
	{
		final Cache<String, ClientSessionInfo> cache = new StringClientSessionInfoCache<>("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}), true);
		
		final JID fromJid = new JID("testFrom", "domain", "x9dkelw");
		
		final Presence pres = new Presence();
		pres.setTo(new JID("testTo", "domain", null));
		pres.setFrom(fromJid.asBareJID());
		pres.setType(Presence.Type.probe);
		
		
		final LocalClientSession clientSession = mock(LocalClientSession.class);
		when(clientSession.getPresence()).thenReturn(pres);
		when(clientSession.isOfflineFloodStopped()).thenReturn(true);
		when(clientSession.isMessageCarbonsEnabled()).thenReturn(false);
		when(clientSession.hasRequestedBlocklist()).thenReturn(false);
		
		final ClientSessionInfo sessionInfo = new ClientSessionInfo(clientSession);

		
		cache.put(fromJid.toString(), sessionInfo);
		
		final ClientSessionInfo retSessionInfo = cache.get(fromJid.toString());
		
		assertNotNull(retSessionInfo);
		
		assertEquals(pres.toXML(), retSessionInfo.getPresence().toXML());

	}	
	
	@Test
	public void testCacheObject_stringRoster_assertCached()
	{
		final Cache<String, Roster> cache = new StringRosterCache<>("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}), true);
		
		final List<String> groups = new LinkedList<>();
		groups.add("TestGroup");
		
		final RosterItem rosterItem = new RosterItem(12345, new JID("ah4626", "test.com", null),
				SubType.BOTH, AskType.SUBSCRIBE, RecvType.SUBSCRIBE, "nickname", groups);
		
		final ConcurrentMap<String, RosterItem> rosterItems = new ConcurrentHashMap<>();
		rosterItems.put("ah4626@test.com", rosterItem);
		
		
		final Roster roster = new Roster();
		roster.setUsername("gm2552");
		roster.setDomain("test.com");
		roster.setRosterItems(rosterItems);
		
		cache.put("gm2552@test.com", roster);
		
		final Roster readRoster = cache.get("gm2552@test.com");
		
		assertEquals("gm2552", readRoster.getUsername());
		final Collection<RosterItem> readRosterItems = readRoster.getRosterItems();
		assertEquals(1, readRosterItems.size());
		
		final RosterItem readRosterItem = readRosterItems.iterator().next();
		assertEquals(rosterItem.getID(), readRosterItem.getID());
		assertEquals(rosterItem.getJid(), readRosterItem.getJid());
		assertEquals(rosterItem.getAskStatus(), readRosterItem.getAskStatus());
		assertEquals(rosterItem.getSubStatus(), readRosterItem.getSubStatus());
		assertEquals(rosterItem.getRecvStatus(), readRosterItem.getRecvStatus());
		assertEquals(rosterItem.getNickname(), readRosterItem.getNickname());
		
		assertEquals(1, readRosterItem.getGroups().size());
		assertEquals("TestGroup", readRosterItem.getGroups().get(0));
	}
	
	@Test
	public void testCacheObject_stringClusterCrossProxyInfo_assertCached()
	{
		final Cache<String, ClusterCrossProxyInfo> cache = new StringClusterCrossProxyInfoCache<>("JUnitCache", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}), true);
		
		final ProxyServerCredential cred = new ProxyServerCredential();
		cred.setCreationDate(Calendar.getInstance().getTime());
		cred.setSubject("subject");
		cred.setSecret("secret");
		cred.setSecretHash(new byte[] {0,1,1,1});
		
		final ClusterNode node = new ClusterNode();
		node.setLastNodeHBDtTm(Instant.now());
		node.setNodeHost("test");
		node.setNodeId(NodeID.getInstance(new byte[] {0,0,0,0}));
		node.setNodeIP("127.0.0.1");
		node.setNodeJoinedDtTm(Instant.now());
		node.setNodeStatus(ClusterNodeStatus.NODE_JOINED);
		
		
		final ClusterCrossProxyInfo proxyInfo = new ClusterCrossProxyInfo();
		proxyInfo.setPort(7777);
		proxyInfo.setResponseDigest("12345");
		proxyInfo.setProxyServiceCredential(cred);
		proxyInfo.setReceiversClusterNode(node);
		
		
		cache.put(proxyInfo.getResponseDigest(), proxyInfo);
		
		final ClusterCrossProxyInfo retProxyInfo = cache.get(proxyInfo.getResponseDigest());
		
		/*
		 * Testing retrieval of relevant fields for proxy data transfers
		 */
		assertNotNull(retProxyInfo);
		
		assertEquals(proxyInfo.getPort(), retProxyInfo.getPort());
		assertEquals(proxyInfo.getResponseDigest(), retProxyInfo.getResponseDigest());
		
		final ProxyServerCredential retCred = retProxyInfo.getProxyServiceCredential();
		assertEquals(cred.getSubject(), retCred.getSubject());
		assertEquals(cred.getSecret(), retCred.getSecret());
		
		final ClusterNode retNode = retProxyInfo.getReceiversClusterNode();
		assertEquals(node.getNodeHost(), retNode.getNodeHost());
		assertEquals(node.getNodeIP(), retNode.getNodeIP());
	}		
	
	@Test
	public void testCacheObject_stringLocalMUCRoom_assertCached()
	{
		final String roomName = UUID.randomUUID().toString();
		
		final RedisDelegatedClusterCacheFactory factory = new RedisDelegatedClusterCacheFactory();
		final Cache<String, LocalMUCRoom> cache = 
				(Cache<String, LocalMUCRoom>)factory.createCache(LocalMUCRoomManager.LOCAL_ROOM_MANAGER_CACHE_BASE_NAME + "groupchat.testdomain.com", -1, 50000, NodeID.getInstance(new byte[] {0,0,0,0}), false);

		final LocalMUCRoom room = new LocalMUCRoom();
		
		cache.put(roomName, room);
		
		final LocalMUCRoom readRoom = cache.get(roomName);
	}
	
	@Test
	public void testCacheObject_crossClusterStringStringCache_assertCached()
	{
		
		final RedisDelegatedClusterCacheFactory factory = new RedisDelegatedClusterCacheFactory();
		final Cache<String, String> cache1 = 
				(Cache<String, String>)factory.createCache(RemoteMUCCache.MUC_NICK_JID_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}), false );

		cache1.put("me@you.com", "me@testyou.com");
		
		assertEquals("me@testyou.com", cache1.get("me@you.com"));
		

		/*
		 * Same cache different node... should act a one single cache stomping on the insert of the other node when using "put"
		 */
		final Cache<String, String> cache2 = 
				(Cache<String, String>)factory.createCache(RemoteMUCCache.MUC_NICK_JID_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,1}), false );
		
		cache2.put("me@you.com", "me2@testyou.com");
		
		assertEquals("me2@testyou.com", cache1.get("me@you.com"));
		
		cache2.remove("me@you.com");
		
		assertNull(cache1.get("me@you.com"));
	}	
	
	@Test
	public void testCacheObject_crossClusterStringStringMapCache_assertCached()
	{
		Map<String, String> map = new HashMap<>();
		
		map.put("testName", "testValue");
		
		final RedisDelegatedClusterCacheFactory factory = new RedisDelegatedClusterCacheFactory();
		final Cache<String, Map<String, String>> cache1 = 
				(Cache<String, Map<String,String>>)factory.createCache(RemoteMUCCache.MUC_OCCUPANT_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,0}), false );

		cache1.put("me@you.com", map);
		
		assertEquals(map, cache1.get("me@you.com"));
		

		/*
		 * Same cache different node... should act a one single cache stomping on the insert of the other node when using "put"
		 */
		final Cache<String, Map<String, String>> cache2 = 
				(Cache<String, Map<String, String>>)factory.createCache(RemoteMUCCache.MUC_OCCUPANT_CACHE_NAME, 0, 0, NodeID.getInstance(new byte[] {0,0,0,1}), false );
		
		map = new HashMap<>();
		
		cache2.put("me@you.com", map);
		
		assertTrue(cache1.get("me@you.com").isEmpty());
		
		cache2.remove("me@you.com");
		
		assertNull(cache1.get("me@you.com"));
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
			super(name, maxSize, maxLifetime, nodeId, true);
		}

	}
	
	protected static class CustomAggregateListRedisClusteredCache<K extends String, V extends List<CustomAggregate>> extends RedisClusteredCache<K, V>
	{

		public CustomAggregateListRedisClusteredCache(java.lang.String name, long maxSize, long maxLifetime,
				NodeID nodeId) {
			super(name, maxSize, maxLifetime, nodeId, true);
		}

	}
}

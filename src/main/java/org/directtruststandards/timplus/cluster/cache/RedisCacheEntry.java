package org.directtruststandards.timplus.cluster.cache;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Entry into the cache.  Caches are logically separated by cache names and further broken down by cluster node.  The fields in this class
 * are populated with the following values
 * <br>
 * Key: CacheName + NodeID + A unique key name in the cahce
 * <br>:
 * ClusteredCacheKey: CacheName + A unique key name in the cahce
 * <br>
 * CacheName: The name of the cache
 * <br>
 * NodeCacheName: CacheName + the node id of the cluster member.
 * <br>
 * Expiration: How long an entry should remain in the cache (in milliseconds).  A negative value indicates the entry never expires.
 * @author Greg Meyer
 * @since 1.0
 */
@RedisHash("timplusclustercache")
@Data
@AllArgsConstructor
public class RedisCacheEntry 
{
	/**
	 * The primary key in the cache.  This is made up of the cache name, the cluster node name, and the 
	 * entry key name.
	 */
	@Id
	private String key;
	
	/**
	 * This is an index that consists of the cache name and entry key name.  This
	 * effectively allows us to query a set of entries by the key entry name and retrieve
	 * all entries across the cluster.
	 */
	@Indexed
	private String clusteredCacheKey;		
	
	/**
	 * This is the name of the cache
	 */
	@Indexed
	private String cacheName;	
	
	/**
	 * This is the index of the cache name partitioned by cluster node.
	 * This is generally used to clear all entries in the cache associated with
	 * a given cluster node.  This is especially useful when a cluster node either gracefully
	 * exits the cluster or is evicted due to lack of heart beat.
	 */
	@Indexed
	private String nodeCacheName;
	
	/**
	 * The actual value of the cache entry.
	 */
	private String value;
	
	@TimeToLive(unit = MILLISECONDS)
	private Long expiration;	
}

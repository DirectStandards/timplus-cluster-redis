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
 * Key: CacheName + A unique key name in the cahce
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
	@Id
	private String key;
	
	@Indexed
	private String cacheName;	
	
	@Indexed
	private String nodeCacheName;
	
	private String value;
	
	@TimeToLive(unit = MILLISECONDS)
	private Long expiration;	
}

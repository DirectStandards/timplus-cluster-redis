package org.directtruststandards.timplus.cluster.cache;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

/**
 * Spring Data redis repository for accessing Redis entries.
 * @author Greg Meyer
 * @since 1.0
 *
 */
public interface RedisCacheRepository extends CrudRepository<RedisCacheEntry, String>, QueryByExampleExecutor<RedisCacheEntry>
{
	/**
	 * Retrieves entries by node cache name.
	 * @param name The node cache name: CacheName + Node ID
	 * @return All entries in the cache that were committed by a specific cluster node member.
	 */
	public Collection<RedisCacheEntry> findByNodeCacheName(String name);

	/**
	 * Retrieves entries by cache name.
	 * @param name The cache name
	 * @return All entries in the cache.
	 */
	public Collection<RedisCacheEntry> findByCacheName(String name);
	
	/**
	 * Deletes entries by node cache name.
	 * @param name The node cache name: CacheName + Node ID
	 */
	public void deleteByNodeCacheName(String name);
}

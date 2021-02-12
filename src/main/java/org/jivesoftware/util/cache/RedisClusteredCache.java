package org.jivesoftware.util.cache;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.directtruststandards.timplus.cluster.cache.CachingConfiguration;
import org.directtruststandards.timplus.cluster.cache.RedisCacheEntry;
import org.directtruststandards.timplus.cluster.cache.RedisCacheRepository;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.cluster.NodeID;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Example;

/**
 * A Redis back clustered cache.  Entries added by the local node cluster member are added
 * to a local cache and to Redis.  Lookups attempt to use the local cache first and fall back
 * to redis in case of a local cache miss.
 * @author Greg Meyer
 * @since 1.0
 *
 */
public class RedisClusteredCache implements Cache
{
    protected long maxCacheSize;

    protected long maxLifetime;

    protected String name;
	
    protected final NodeID nodeId;
    
    protected DefaultCache locallyCached;
    
    protected RedisCacheRepository remotelyCached;
    
    protected String nodeCacheName;
    
    public RedisClusteredCache(final String name, final long maxSize, final long maxLifetime, final NodeID nodeId)
    {
    	this.name = name;
    	this.maxCacheSize = maxSize;
    	this.maxLifetime = maxLifetime;
    	this.nodeId = nodeId;
    	
    	locallyCached = new DefaultCache<>(name, maxSize, maxLifetime);
    	
		final ApplicationContext ctx = CachingConfiguration.getApplicationContext();
		
		if (ctx == null)
			throw new IllegalStateException("Application context cannot be null");

		remotelyCached = ctx.getBean(RedisCacheRepository.class);
		
		nodeCacheName = name + nodeId.toString();
    }
    
	@Override
	public String getName() 
	{
		return name;
	}

	@Override
	public void setName(String name) 
	{
		this.name = name;
		
		this.nodeCacheName = name + nodeId.toString();
		
	}

	@Override
	public long getMaxCacheSize() 
	{

		return maxCacheSize;
	}

	@Override
	public void setMaxCacheSize(int maxSize) 
	{
		this.maxCacheSize = maxSize;
	}

	@Override
	public long getMaxLifetime() 
	{
		return maxLifetime;
	}

	@Override
	public void setMaxLifetime(long maxLifetime) 
	{
		this.maxLifetime = maxLifetime;
	}

	@Override
	public int getCacheSize() 
	{
		return (int)locallyCached.getCacheSize();
	}

	@Override
	public long getCacheHits() 
	{
		return locallyCached.getCacheHits();
	}

	@Override
	public long getCacheMisses() 
	{
		return locallyCached.getCacheMisses();
	}

	@Override
	public Collection<Object> values() 
	{
		final Collection<Object> retVal = new LinkedList<>();
		
		Collection<RedisCacheEntry> values = remotelyCached.findByCacheName(name);
		
		values.forEach(val -> retVal.add(val.getValue()));
		
		return Collections.unmodifiableCollection(retVal);
	}

	@Override
	public Set<Entry<Object, Object>> entrySet() 
	{
		final Set<Entry<Object, Object>> retVal = new HashSet<>();
		
		Collection<RedisCacheEntry> values = remotelyCached.findByCacheName(name);		
		
		values.forEach(val -> retVal.add(new AbstractMap.SimpleEntry(val.getKey().substring(name.length()), val.getValue())));
		
		return Collections.unmodifiableSet(retVal);
	}

	@Override
	public Set<Object> keySet() 
	{
		final Set<Object> retVal = new HashSet<>();
		
		Collection<RedisCacheEntry> values = remotelyCached.findByCacheName(name);
		
		values.forEach(val -> retVal.add(val.getKey().substring(name.length())));
		
		return Collections.unmodifiableSet(retVal);
	}

	@Override
	public int size() 
	{
		final RedisCacheEntry probe = new RedisCacheEntry((String)null, name, (String)null, (Serializable)null, maxLifetime);
		return (int)remotelyCached.count(Example.of(probe));
	}

	@Override
	public boolean isEmpty() 
	{
		final RedisCacheEntry probe = new RedisCacheEntry((String)null, name, (String)null, (Serializable)null, maxLifetime);
		
		return remotelyCached.count(Example.of(probe)) == 0;
	}

	@Override
	public boolean containsKey(Object key) 
	{
		return locallyCached.containsKey(key) || remotelyCached.existsById(name + key);
	}

	@Override
	public boolean containsValue(Object value) 
	{
		final RedisCacheEntry probe = new RedisCacheEntry((String)null, name, (String)null, (Serializable)value, maxLifetime);
		
		return locallyCached.containsValue(value) || (int)remotelyCached.count(Example.of(probe)) != 0;
	}

	@Override
	public Object get(Object key) 
	{
		Object retVal = locallyCached.get(key);
		if (retVal == null)
		{
			final Optional<RedisCacheEntry> entry = remotelyCached.findById(name + key);
			
			if (entry.isPresent())
				retVal = entry.get().getValue();
		}
		
		return retVal;
	}

	@Override
	public Object put(Object key, Object value) 
	{
		locallyCached.put((Serializable)key, (Serializable)value);

		final RedisCacheEntry entry = new RedisCacheEntry(name + key , name, nodeCacheName, (Serializable)value, maxLifetime);	
		
		
		remotelyCached.save(entry);
		
		return value;
	}

	@Override
	public Object remove(Object key) 
	{
		final Serializable retVal = locallyCached.remove(key);
		
		remotelyCached.deleteById(name + key);
		
		return retVal;
	}

	@Override
	public void clear() 
	{
		locallyCached.clear();
		
		remotelyCached.deleteAll(remotelyCached.findByNodeCacheName(nodeCacheName));
	}

	@Override
	public void purgeClusteredNodeCaches(NodeID node) 
	{
		if (XMPPServer.getInstance() != null && node.equals(XMPPServer.getInstance().getNodeID()))
			locallyCached.clear();
		
		remotelyCached.deleteAll(remotelyCached.findByNodeCacheName(name + node.toString()));
		
	}

	@Override
	public void putAll(Map m) 
	{
		// TODO Auto-generated method stub
		if (m != null && m.size() > 0)
		{
			locallyCached.putAll(m);
		
			m.forEach((key, value) -> 
			{
				final RedisCacheEntry entry = new RedisCacheEntry(name + key , name, nodeCacheName, (Serializable)value, maxLifetime);
				
				remotelyCached.save(entry);				
			});
		}
	}

}

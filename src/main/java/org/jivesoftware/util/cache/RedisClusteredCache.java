package org.jivesoftware.util.cache;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
import org.jivesoftware.openfire.cluster.NodeID;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A Redis back clustered cache.  Entries added by the local node cluster member are added
 * to a local cache and to Redis.  Lookups attempt to use the local cache first and fall back
 * to redis in case of a local cache miss.
 * @author Greg Meyer
 * @since 1.0
 *
 */
public abstract class RedisClusteredCache<K,V> implements Cache<K,V>
{
    protected long maxCacheSize;

    protected long maxLifetime;

    protected String name;
	
    protected final NodeID nodeId;
    
    protected RedisCacheRepository remotelyCached;
    
    protected String nodeCacheName;
    
    protected ObjectMapper objectMapper;
    
    protected Class<K> keyType; 
    
    protected Type valueType; 
    
    public RedisClusteredCache(final String name, final long maxSize, final long maxLifetime, final NodeID nodeId)
    {
    	this.name = name;
    	this.maxCacheSize = maxSize;
    	this.maxLifetime = maxLifetime;
    	this.nodeId = nodeId;
    	
		final ApplicationContext ctx = CachingConfiguration.getApplicationContext();
		
		if (ctx == null)
			throw new IllegalStateException("Application context cannot be null");

		remotelyCached = ctx.getBean(RedisCacheRepository.class);
		
		objectMapper = ctx.getBean(ObjectMapper.class);
		
		nodeCacheName = name + nodeId.toString();

		// Needed to get full type information for performing deserialization of generics
        final Type superClass = getClass().getGenericSuperclass();
        
        valueType =  ((ParameterizedType) superClass).getActualTypeArguments()[1];
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
		return -1;
	}

	@Override
	public long getCacheHits() 
	{
		return 0;
	}

	@Override
	public long getCacheMisses() 
	{
		return 0;
	}

	@Override
	public Collection<V> values() 
	{
		final Collection<V> retVal = new LinkedList<>();
		
		Collection<RedisCacheEntry> values = remotelyCached.findByCacheName(name);
		
		values.forEach(val -> retVal.add(deserializedRedisCacheEntryValue(val.getValue())));
		
		return Collections.unmodifiableCollection(retVal);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Set<Entry<K, V>> entrySet() 
	{
		final Set<Entry<K, V>> retVal = new HashSet<>();
		
		Collection<RedisCacheEntry> values = remotelyCached.findByCacheName(name);		
		
		values.forEach(val -> retVal.add(new AbstractMap.SimpleEntry(val.getKey().substring(name.length()), 
				deserializedRedisCacheEntryValue(val.getValue()))));
		
		return Collections.unmodifiableSet(retVal);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<K> keySet() 
	{
		final Set<K> retVal = new HashSet<>();
		
		Collection<RedisCacheEntry> values = remotelyCached.findByCacheName(name);
		
		values.forEach(val -> retVal.add((K)val.getKey().substring(name.length())));
		
		return Collections.unmodifiableSet(retVal);
	}

	@Override
	public int size() 
	{
		final RedisCacheEntry probe = new RedisCacheEntry((String)null, name, (String)null, (String)null, maxLifetime);
		return (int)remotelyCached.count(Example.of(probe));
	}

	@Override
	public boolean isEmpty() 
	{
		final RedisCacheEntry probe = new RedisCacheEntry((String)null, name, (String)null, (String)null, maxLifetime);
		
		return remotelyCached.count(Example.of(probe)) == 0;
	}

	@Override
	public boolean containsKey(Object key) 
	{
		return remotelyCached.existsById(name + key);
	}

	@Override
	public boolean containsValue(Object value) 
	{
		RedisCacheEntry probe = null;
		try
		{
			final String mappedValue = objectMapper.writeValueAsString(value);
		
			probe = new RedisCacheEntry((String)null, name, (String)null, mappedValue, maxLifetime);
		}
		catch (Exception e)
		{
			probe = new RedisCacheEntry((String)null, name, (String)null, null, maxLifetime);
		}
		
		return (int)remotelyCached.count(Example.of(probe)) != 0;
	}

	@Override
	public V get(Object key) 
	{
		V retVal = null;

		final Optional<RedisCacheEntry> entry = remotelyCached.findById(name + key);
		
		if (entry.isPresent())
			retVal = deserializedRedisCacheEntryValue(entry.get().getValue());
		
		return retVal;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V put(Object key, Object value) 
	{
		
		remotelyCached.save(createSafeRedisCacheEntry(key, value));
		
		return (V)value;
	}

	@Override
	public V remove(Object key) 
	{
		final Optional<RedisCacheEntry> retVal = remotelyCached.findById(name + key);
		
		if (retVal.isPresent())
		{
			remotelyCached.deleteById(name + key);
			return deserializedRedisCacheEntryValue(retVal.get().getValue());
		}
		else
			return null;
	}

	@Override
	public void clear() 
	{
		
		remotelyCached.deleteAll(remotelyCached.findByNodeCacheName(nodeCacheName));
	}

	@Override
	public void purgeClusteredNodeCaches(NodeID node) 
	{
		
		remotelyCached.deleteAll(remotelyCached.findByNodeCacheName(name + node.toString()));
		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void putAll(Map m) 
	{
		// TODO Auto-generated method stub
		if (m != null && m.size() > 0)
		{
			m.forEach((key, value) -> 
			{				
				remotelyCached.save(createSafeRedisCacheEntry(key, value));				
			});
		}
	}

	protected RedisCacheEntry createSafeRedisCacheEntry(Object key, Object value)
	{
		try
		{
			final String mappedValue = objectMapper.writeValueAsString(value);
		
			return new RedisCacheEntry(name + key , name, nodeCacheName, mappedValue, maxLifetime);	
		}
		catch (Exception e)
		{
			return new RedisCacheEntry(name + key , name, nodeCacheName, null, maxLifetime);	
		}
	}
	
	protected V deserializedRedisCacheEntryValue(String serialized)
	{
		try
		{
			return objectMapper.readValue(serialized, getDeserilizedValueType());
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public <T> TypeReference<T> getDeserilizedValueType()
	{
        return forType(valueType);
	}
	
	protected <T> TypeReference<T> forType(final Type type) 
	{
		return new TypeReference<T>()
		{
		    public Type getType() { return type; }
		};
	}
}

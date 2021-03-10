package org.directtruststandards.timplus.cluster.cache;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Spring configuration for Redis repositories and Redis Template
 * Redis connection information can set using spring configuration (properties files, 
 * spring cloud config, etc).
 * @author Greg Meyer
 * @since 1.0
 */
@EnableRedisRepositories
@Configuration
public class CachingConfiguration implements ApplicationContextAware
{
	protected static ApplicationContext ctx;
	
	@Bean
	public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) 
	{
	   RedisTemplate<byte[], byte[]> template = new RedisTemplate<byte[], byte[]>();
	   
	   template.setConnectionFactory(redisConnectionFactory);
	   
	   return template;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException 
	{
		ctx = applicationContext;
	}
	
	public static ApplicationContext getApplicationContext()
	{
		return ctx;
	}
}

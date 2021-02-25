package org.directtruststandards.timplus.cluster.cache;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;

import redis.embedded.RedisServer;

@SpringBootApplication
@Configuration
public class TestApplication 
{
	public static void main(String[] args) 
	{
		new SpringApplicationBuilder(TestApplication.class).web(WebApplicationType.NONE).run(args);
	}
	
    private RedisServer redisServer;

    
    public TestApplication(RedisProperties redisProperties) 
    {
        this.redisServer = new RedisServer(redisProperties.getPort());
    }

    @PostConstruct
    public void postConstruct() 
    {
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() 
    {
        redisServer.stop();
    }	
}

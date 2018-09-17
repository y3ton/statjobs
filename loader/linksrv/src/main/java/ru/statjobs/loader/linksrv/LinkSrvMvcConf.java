package ru.statjobs.loader.linksrv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.statjobs.loader.linksrv.dao.RedisDao;
import ru.statjobs.loader.utils.JsonUtils;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

@Configuration
@EnableWebMvc
@ComponentScan("ru.statjobs.loader.linksrv")
public class LinkSrvMvcConf {


    @Autowired
    ServletContext context;

    RedisDao redisDao;

    @Bean
    @Scope("singleton")
    RedisDao getRedisDao() {
        redisDao = new RedisDao(
                (String) context.getAttribute(App.REDIS_HOST),
                (Integer) context.getAttribute(App.REDIS_PORT));
        return redisDao;
    }

    @Bean
    @Scope("singleton")
    public JsonUtils getJsonUtils() {
        return new JsonUtils();
    }

    @PreDestroy
    public void destroy() {
        redisDao.close();
    }

}

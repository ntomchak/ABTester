package manners.cowardly.abpromoter.database.redis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.bukkit.configuration.ConfigurationSection;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageTemplate.DeliverableMessage.MessageLinkTokenInfo;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Redis {
    private JedisPool pool;
    private String server;

    public Redis(String serverName, ConfigurationSection redisSection) {
        this.server = serverName;
        load(redisSection);
    }

    private void load(ConfigurationSection section) {
        String password = section.getString("password");
        String host = section.getString("host");
        String user = section.getString("user");
        int port = section.getInt("port", 6379);

        GenericObjectPoolConfig<Jedis> config = new GenericObjectPoolConfig<Jedis>();
        config.setMaxTotal(12);
        config.setMaxIdle(12);
        config.setMinIdle(12);
        config.setTestOnBorrow(true);

        if (password == null || password.length() == 0) {
            pool = new JedisPool(config, host, port);
        } else {
            pool = new JedisPool(config, host, port, user, password);
        }
    }

    private void recordLink(Jedis r, String token, int id, String destinationUrl, String linkSource) {
        Map<String, String> tokenInfo = new HashMap<String, String>();
        tokenInfo.put("server", server);
        tokenInfo.put("id", Integer.toString(id));
        tokenInfo.put("url", destinationUrl);
        tokenInfo.put("linkSource", linkSource);
        r.hset(token, tokenInfo);
        r.expire(token, 5000000L);
    }

    // from async
    public void recordMenuLinks(int open, Collection<MessageLinkTokenInfo> tokens) {
        if (!tokens.isEmpty()) {
            try (Jedis r = pool.getResource()) {
                for (MessageLinkTokenInfo token : tokens)
                    recordLink(r, token.getToken(), open, token.getUrl(), "open");
            } catch (Exception e) {
                e.printStackTrace();
                ABPromoter.getInstance().getLogger().severe("Could not record menu chat external links to redis.");
            }
        }
    }

    // from async
    public void recordDeliveryLinks(int delivery, Collection<MessageLinkTokenInfo> tokens) {
        if (!tokens.isEmpty()) {
            try (Jedis r = pool.getResource()) {
                for (MessageLinkTokenInfo token : tokens)
                    recordLink(r, token.getToken(), delivery, token.getUrl(), "delivery");
            } catch (Exception e) {
                e.printStackTrace();
                ABPromoter.getInstance().getLogger().severe("Could not record announcer external links to redis.");
            }
        }
    }
}

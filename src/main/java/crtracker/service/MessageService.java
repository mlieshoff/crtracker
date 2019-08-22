package crtracker.service;

import net.dv8tion.jda.core.MessageBuilder;
import org.mili.utils.Lambda;
import org.mili.utils.sql.service.ServiceException;

import java.util.concurrent.ConcurrentHashMap;
import crtracker.Config;
import crtracker.bot.DiscordApi;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageService extends NonDbTransactionalService {

    private final ConcurrentHashMap<String, DiscordApi> cache = new ConcurrentHashMap<>();

    public void sendAlert(final Config config, final String message) {
        try {
            doInService(new Lambda<Void>() {
                public Void exec(Object... params) throws Exception {
                    if (Config.TEST) {
                        log.info("BOT: sendAlert: {}", message);
                    } else {
                        getBot(config).sendAlert(new MessageBuilder().appendCodeBlock(message, "bash").build());
                    }
                    return null;
                }
            });
        } catch (ServiceException e) {
            log.warn("Could not send alert to Discord: %s", message, e);
            releaseBot();
        }
    }

    private DiscordApi getBot(Config config) throws Exception {
        DiscordApi discordApi = cache.get("KEY");
        if (discordApi == null) {
            cache.putIfAbsent("KEY", config.createBot());
        }
        return cache.get("KEY");
    }

    public void sendWelcome(final Config config, final String message) {
        try {
            doInService(new Lambda<Void>() {
                public Void exec(Object... params) throws Exception {
                    if (Config.TEST) {
                        log.info("BOT: sendWelcome: {}", message);
                    } else {
                        getBot(config).sendWelcome(new MessageBuilder().append(message).build());
                    }
                    return null;
                }
            });
        } catch (ServiceException e) {
            log.warn("Could not send welcome to Discord: %s", message, e);
            releaseBot();
        }
    }

    public void sendLiga(final Config config, final String message) {
        try {
            doInService(new Lambda<Void>() {
                public Void exec(Object... params) throws Exception {
                    if (Config.TEST) {
                        log.info("BOT: sendLiga: {}", message);
                    } else {
                        getBot(config).sendLiga(new MessageBuilder().append(message).build());
                    }
                    return null;
                }
            });
        } catch (ServiceException e) {
            log.warn("Could not send liga to Discord: %s", message, e);
            releaseBot();
        }
    }

    private void releaseBot() {
        cache.clear();
    }

}

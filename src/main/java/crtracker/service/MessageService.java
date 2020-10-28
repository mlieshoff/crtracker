package crtracker.service;

import static java.lang.String.format;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.mili.utils.Lambda;
import org.mili.utils.sql.service.ServiceException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import crtracker.Config;
import crtracker.bot.DiscordApi;
import jcrapi2.model.PlayerBattleLog;
import jcrapi2.model.PlayerBattleLogOpponent;
import jcrapi2.model.PlayerBattleLogTeam;
import jcrapi2.response.GetClanResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageService extends NonDbTransactionalService {

  private static final String EMPTY = "\u200b";
  private static final String VERSUS_TEXT = "versus";
  private static final String VERSUS_PATTERN = "%s:%s";
  private static final String URL_BATTLE_PATTERN = "https://statsroyale.com/de/profile/%s/battles";
  private static final String AUTHOR = "RoyalCardForces";
  private static final String URL_CLAN = "https://statsroyale.com/de/clan/RP88QQG";
  private static final String URL_CLAN_ICON = "https://cdn.statsroyale.com/images/clanwars/16000077_bronze3.png";
  private static final String
      THUMBNAIL =
      "https://cdn.royaleapi.com/static/img/branding/royaleapi-logo.png?t=d36aee7acba48c1f894a438c6823cd01a7d2f4e9";

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
    }
  }

  public void sendLiga(final Config config, final GetClanResponse getClanResponse,
                       final PlayerBattleLog playerBattleLog) {
    try {
      doInService(new Lambda<Void>() {
        public Void exec(Object... params) throws Exception {
          MessageEmbed messageEmbed = createBattleMessageEmbed(getClanResponse, playerBattleLog);
          if (Config.TEST) {
            log.info("BOT: sendLiga: {}", messageEmbed);
          } else {
            getBot(config).sendLiga(messageEmbed);
          }
          return null;
        }
      });
    } catch (ServiceException e) {
      log.warn("Could not send liga to Discord: %s", playerBattleLog.getBattleTime(), e);
    }
  }

  public void sendLiveTicker(final Config config, final GetClanResponse getClanResponse,
                             final PlayerBattleLog playerBattleLog) {
    try {
      doInService(new Lambda<Void>() {
        public Void exec(Object... params) throws Exception {
          MessageEmbed messageEmbed = createBattleMessageEmbed(getClanResponse, playerBattleLog);
          log.info("BOT: sendLiveTicker: {}", messageEmbed.toData());
          if (Config.TEST) {
            log.info("BOT: sendLiveTicker: {}", messageEmbed);
          } else {
            getBot(config).sendLiveTicker(messageEmbed);
          }
          return null;
        }
      });
    } catch (ServiceException e) {
      log.warn("Could not send live ticker to Discord: %s", playerBattleLog.getBattleTime(), e);
    }
  }

  private MessageEmbed createBattleMessageEmbed(GetClanResponse getClanResponse, PlayerBattleLog playerBattleLog) {
    List<PlayerBattleLogTeam> team = playerBattleLog.getTeam();
    String playerTag = team.get(0).getTag().substring(1);
    List<PlayerBattleLogOpponent> opponentTeam = playerBattleLog.getOpponent();

    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(translateGameMode(playerBattleLog), format(URL_BATTLE_PATTERN, playerTag));
    embedBuilder.setAuthor(AUTHOR, URL_CLAN, URL_CLAN_ICON);
    embedBuilder.setThumbnail(THUMBNAIL);

    if (team.size() > 1) {
      embedBuilder
          .addField("2 vs 2", transformPlayer(team.get(0)) + " and " + transformPlayer(team.get(1)),
              false);
      embedBuilder
          .addField("Versus", transformPlayer(opponentTeam.get(0)) + " and " + transformPlayer(opponentTeam.get(1)),
              false);
    } else {
      embedBuilder.addField("1 vs 1",
          transformPlayer(team.get(0)) + " versus " + transformPlayer(opponentTeam.get(0)), false);
    }

    int playerCrowns = playerBattleLog.getTeam().get(0).getCrowns();
    int opponentCrowns = playerBattleLog.getOpponent().get(0).getCrowns();
    String msg = "Unentschieden";
    if (playerCrowns > opponentCrowns) {
      msg = "Sieg";
    } else if (playerCrowns < opponentCrowns) {
      msg = "Niederlage";
    }
    embedBuilder.addField(playerBattleLog.getArena().getName(),
        format(VERSUS_PATTERN, playerCrowns, opponentCrowns) + " " + msg, false);
    return embedBuilder.build();
  }

  private String transformPlayer(PlayerBattleLogTeam playerBattleLogTeam) {
    return "[" + playerBattleLogTeam.getName() + "](https://royaleapi.com/player/" + playerBattleLogTeam.getTag()
        .substring(1) + ")";
  }

  private String transformPlayer(PlayerBattleLogOpponent playerBattleLogOpponent) {
    return "[" + playerBattleLogOpponent.getName() + "](https://royaleapi.com/player/" + playerBattleLogOpponent
        .getTag().substring(1) + ")";
  }

  private String translateGameMode(PlayerBattleLog playerBattleLog) {
    String type = playerBattleLog.getType().toLowerCase();
    if ("challenge".equals(type)) {
      return playerBattleLog.getChallengeTitle();
    }
    return playerBattleLog.getGameMode().getName() + " (" + playerBattleLog.getType() + ")";
  }

}

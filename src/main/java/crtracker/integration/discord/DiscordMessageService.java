package crtracker.integration.discord;

import static java.lang.String.format;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import crtracker.service.ConfigurationService;
import jcrapi2.model.PlayerBattleLog;
import jcrapi2.model.PlayerBattleLogOpponent;
import jcrapi2.model.PlayerBattleLogTeam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DiscordMessageService {

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

  @Autowired
  private ConfigurationService configurationService;

  public void sendAlert(String message) throws Exception {
    getBot(configurationService).sendMessage(getChannelId("discord.channel.alerts"),
        new MessageBuilder().appendCodeBlock(message, "bash").build());
  }

  private long getChannelId(String channelName) {
    return Long.valueOf(configurationService.getConfig().getProperty(channelName));
  }

  private DiscordApi getBot(ConfigurationService configurationService) throws Exception {
    DiscordApi discordApi = cache.get("KEY");
    if (discordApi == null) {
      cache.putIfAbsent("KEY", new DiscordApi((String) configurationService.getCredentials().get("discord.token")));
    }
    return cache.get("KEY");
  }

  public void sendWelcome(String message) throws Exception {
    getBot(configurationService)
        .sendMessage(getChannelId("discord.channel.welcome"), new MessageBuilder().append(message).build());
  }

  public void sendLiga(PlayerBattleLog playerBattleLog) throws Exception {
    getBot(configurationService)
        .sendMessageEmbedd(getChannelId("discord.channel.liga"), createBattleMessageEmbed(playerBattleLog));
  }

  public void sendLiveTicker(PlayerBattleLog playerBattleLog) throws Exception {
    getBot(configurationService)
        .sendMessageEmbedd(getChannelId("discord.channel.liveticker"), createBattleMessageEmbed(playerBattleLog));
  }

  private MessageEmbed createBattleMessageEmbed(PlayerBattleLog playerBattleLog) {
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

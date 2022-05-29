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
import jcrapi2.api.intern.players.battlelog.LogEntry;
import jcrapi2.api.intern.players.battlelog.Opponent;
import jcrapi2.api.intern.players.battlelog.Team;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DiscordMessageService {

  private static final String ONE_VS_ONE_VERSUS_TEXT = "1 vs 1";
  private static final String TWO_VS_TWO_VERSUS_TEXT = "2 vs 2";
  private static final String VERSUS_TEXT = "Versus";
  private static final String VERSUS_PATTERN = "%s:%s %s";
  private static final String AND_TEXT_PATTERN = "%s and %s";
  private static final String VERSUS_TEXT_PATTERN = "%s versus %s";
  private static final String GAMEMODE_PATTERN = "%s (%s)";
  private static final String DRAW_TEXT = "Unentschieden";
  private static final String WON_TEXT = "Sieg";
  private static final String LOST_TEXT = "Niederlage";
  private static final String URL_BATTLE_PATTERN = "https://statsroyale.com/de/profile/%s/battles";
  private static final String AUTHOR = "RoyalCardForces";
  private static final String URL_CLAN = "https://statsroyale.com/de/clan/RP88QQG";
  private static final String URL_CLAN_ICON = "https://cdn.statsroyale.com/images/clanwars/16000077_bronze3.png";
  private static final String
      THUMBNAIL =
      "https://cdn.royaleapi.com/static/img/branding/royaleapi-logo.png?t=d36aee7acba48c1f894a438c6823cd01a7d2f4e9";
  private static final String LINK_PATTERN = "[%s](https://royaleapi.com/player/%s)";

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

  public void sendLiga(LogEntry logEntry) throws Exception {
    getBot(configurationService)
        .sendMessageEmbedd(getChannelId("discord.channel.liga"), createBattleMessageEmbed(logEntry));
  }

  public void sendLiveTicker(LogEntry logEntry) throws Exception {
    getBot(configurationService)
        .sendMessageEmbedd(getChannelId("discord.channel.liveticker"), createBattleMessageEmbed(logEntry));
  }

  private MessageEmbed createBattleMessageEmbed(LogEntry logEntry) {
    List<Team> team = logEntry.getTeam();
    String playerTag = team.get(0).getTag().substring(1);
    List<Opponent> opponentTeam = logEntry.getOpponent();

    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(translateGameMode(logEntry), format(URL_BATTLE_PATTERN, playerTag));
    embedBuilder.setAuthor(AUTHOR, URL_CLAN, URL_CLAN_ICON);
    embedBuilder.setThumbnail(THUMBNAIL);

    if (team.size() > 1) {
      embedBuilder
          .addField(
              TWO_VS_TWO_VERSUS_TEXT,
              format(AND_TEXT_PATTERN, transformPlayer(team.get(0)), transformPlayer(team.get(1))),
              false
          );
      embedBuilder
          .addField(
              VERSUS_TEXT,
              format(AND_TEXT_PATTERN, transformPlayer(opponentTeam.get(0)), transformPlayer(opponentTeam.get(1))),
              false
          );
    } else {
      embedBuilder.addField(
          ONE_VS_ONE_VERSUS_TEXT,
          format(VERSUS_TEXT_PATTERN, transformPlayer(team.get(0)), transformPlayer(opponentTeam.get(0))),
          false
      );
    }

    int playerCrowns = logEntry.getTeam().get(0).getCrowns();
    int opponentCrowns = logEntry.getOpponent().get(0).getCrowns();
    String msg = DRAW_TEXT;
    if (playerCrowns > opponentCrowns) {
      msg = WON_TEXT;
    } else if (playerCrowns < opponentCrowns) {
      msg = LOST_TEXT;
    }
    embedBuilder.addField(
        logEntry.getArena().getName(),
        format(VERSUS_PATTERN, playerCrowns, opponentCrowns, msg),
        false
    );
    return embedBuilder.build();
  }

  private String transformPlayer(Team team) {
    return format(LINK_PATTERN, team.getName(), team.getTag().substring(1));
  }

  private String transformPlayer(Opponent opponent) {
    return format(LINK_PATTERN, opponent.getName(), opponent.getTag().substring(1));
  }

  private String translateGameMode(LogEntry logEntry) {
    String type = logEntry.getType().toLowerCase();
    /*
    if ("challenge".equals(type)) {
      return logEntry.getChallengeTitle();
    }
    */
    return format(GAMEMODE_PATTERN, logEntry.getGameMode().getName(), logEntry.getType());
  }

}

package crtracker.integration;

import org.mili.utils.Lambda;
import org.mili.utils.sql.service.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import crtracker.integration.discord.DiscordMessageService;
import crtracker.service.BaseService;
import crtracker.service.ConfigurationService;
import jcrapi2.api.intern.players.battlelog.LogEntry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MessageService extends BaseService {

  @Autowired
  private DiscordMessageService discordMessageService;

  public void sendAlert(String message) {
    try {
      doInService((Lambda<Void>) params -> {
        if (ConfigurationService.TEST) {
          log.info("sendAlert: {}", message);
        } else {
          discordMessageService.sendAlert(message);
        }
        return null;
      });
    } catch (ServiceException e) {
      log.warn("Could not send alert: {}}", message, e);
    }
  }

  public void sendWelcome(String message) {
    try {
      doInService((Lambda<Void>) params -> {
        if (ConfigurationService.TEST) {
          log.info("sendWelcome: {}", message);
        } else {
          discordMessageService.sendWelcome(message);
        }
        return null;
      });
    } catch (ServiceException e) {
      log.warn("Could not send welcome: {}", message, e);
    }
  }

  public void sendLiga(LogEntry logEntry) {
    try {
      doInService((Lambda<Void>) params -> {
        if (ConfigurationService.TEST) {
          log.info("sendLiga: {}", logEntry.getBattleTime());
        } else {
          discordMessageService.sendLiga(logEntry);
        }
        return null;
      });
    } catch (ServiceException e) {
      log.warn("Could not send liga: {}", logEntry.getBattleTime(), e);
    }
  }

  public void sendLiveTicker(LogEntry logEntry) {
    try {
      doInService((Lambda<Void>) params -> {
        if (ConfigurationService.TEST) {
          log.info("sendLiveTicker: {}", logEntry.getBattleTime());
        } else {
          discordMessageService.sendLiveTicker(logEntry);
        }
        return null;
      });
    } catch (ServiceException e) {
      log.warn("Could not send live ticker: {}", logEntry.getBattleTime(), e);
    }
  }

}

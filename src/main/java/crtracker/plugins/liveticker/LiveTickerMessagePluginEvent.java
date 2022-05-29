package crtracker.plugins.liveticker;

import crtracker.integration.MessageService;
import crtracker.plugins.messaging.MessagePluginEvent;
import jcrapi2.api.intern.players.battlelog.LogEntry;
import lombok.Getter;

@Getter
public class LiveTickerMessagePluginEvent extends MessagePluginEvent<LogEntry> {

  public LiveTickerMessagePluginEvent(LogEntry message) {
    super(message);
  }

  @Override
  public void send(MessageService messageService) {
    messageService.sendLiveTicker(message);
  }

}

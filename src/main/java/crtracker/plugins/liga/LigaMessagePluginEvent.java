package crtracker.plugins.liga;

import crtracker.integration.MessageService;
import crtracker.plugins.messaging.MessagePluginEvent;
import jcrapi2.api.intern.players.battlelog.LogEntry;
import lombok.Getter;

@Getter
public class LigaMessagePluginEvent extends MessagePluginEvent<LogEntry> {

  public LigaMessagePluginEvent(LogEntry message) {
    super(message);
  }

  @Override
  public void send(MessageService messageService) {
    messageService.sendLiga(message);
  }

}

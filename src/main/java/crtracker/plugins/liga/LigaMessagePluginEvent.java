package crtracker.plugins.liga;

import crtracker.integration.MessageService;
import crtracker.plugins.messaging.MessagePluginEvent;
import jcrapi2.model.PlayerBattleLog;
import lombok.Getter;

@Getter
public class LigaMessagePluginEvent extends MessagePluginEvent<PlayerBattleLog> {

  public LigaMessagePluginEvent(PlayerBattleLog message) {
    super(message);
  }

  @Override
  public void send(MessageService messageService) {
    messageService.sendLiga(message);
  }

}

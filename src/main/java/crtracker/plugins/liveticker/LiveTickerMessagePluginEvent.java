package crtracker.plugins.liveticker;

import crtracker.integration.MessageService;
import crtracker.plugins.messaging.MessagePluginEvent;
import jcrapi2.model.PlayerBattleLog;
import lombok.Getter;

@Getter
public class LiveTickerMessagePluginEvent extends MessagePluginEvent<PlayerBattleLog> {

  public LiveTickerMessagePluginEvent(PlayerBattleLog message) {
    super(message);
  }

  @Override
  public void send(MessageService messageService) {
    messageService.sendLiveTicker(message);
  }

}

package crtracker.plugins.messaging;

import crtracker.integration.MessageService;
import lombok.Getter;

@Getter
public class AlertPluginEvent extends MessagePluginEvent<String> {

  public AlertPluginEvent(String message) {
    super(message);
  }

  @Override
  public void send(MessageService messageService) {
    messageService.sendAlert(message);
  }

}

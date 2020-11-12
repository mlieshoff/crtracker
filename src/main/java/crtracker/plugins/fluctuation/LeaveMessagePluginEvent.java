package crtracker.plugins.fluctuation;

import static org.apache.commons.lang3.StringUtils.join;

import java.util.Collection;
import crtracker.integration.MessageService;
import crtracker.plugins.messaging.MessagePluginEvent;
import lombok.Getter;

@Getter
public class LeaveMessagePluginEvent extends MessagePluginEvent<Collection<String>> {

  public LeaveMessagePluginEvent(Collection<String> message) {
    super(message);
  }

  @Override
  public void send(MessageService messageService) {
    messageService.sendAlert("Folgende Member haben den Clan verlassen:\n" + join(message, "\n"));
  }

}

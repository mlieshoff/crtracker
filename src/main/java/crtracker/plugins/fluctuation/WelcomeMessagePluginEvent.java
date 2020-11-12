package crtracker.plugins.fluctuation;

import static org.apache.commons.lang3.StringUtils.join;

import java.util.Collection;
import crtracker.integration.MessageService;
import crtracker.plugins.messaging.MessagePluginEvent;
import lombok.Getter;

@Getter
public class WelcomeMessagePluginEvent extends MessagePluginEvent<Collection<String>> {

  public WelcomeMessagePluginEvent(Collection<String> message) {
    super(message);
  }

  @Override
  public void send(MessageService messageService) {
    messageService.sendWelcome(
        "Willkommen im Clan! Bitte begrüßt die folgenden Neuankömmlinge im Clan-Chat:\n" + join(message, "\n"));
  }

}

package crtracker.plugins.messaging;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import crtracker.integration.MessageService;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugin.PluginEvent;

@Service
public class MessagePlugin extends AbstractPlugin<MessagePluginEvent<?>> {

  @Autowired
  private MessageService messageService;

  @Override
  public void onPluginEvent(Session session, MessagePluginEvent<?> messagePluginEvent) {
    messagePluginEvent.send(messageService);
  }

  @Override
  public boolean canHandlePluginEvent(PluginEvent pluginEvent) {
    return pluginEvent instanceof MessagePluginEvent;
  }

}

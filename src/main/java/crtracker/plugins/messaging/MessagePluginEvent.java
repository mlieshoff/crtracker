package crtracker.plugins.messaging;

import crtracker.integration.MessageService;
import crtracker.plugin.PluginEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class MessagePluginEvent<T> implements PluginEvent {

  protected final T message;

  public abstract void send(MessageService messageService);

}

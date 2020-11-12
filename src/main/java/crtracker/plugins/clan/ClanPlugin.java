package crtracker.plugins.clan;

import org.hibernate.Session;
import org.springframework.stereotype.Service;

import crtracker.plugin.AbstractPlugin;
import crtracker.plugin.PluginEvent;
import crtracker.plugins.fluctuation.FluctuationPluginEvent;
import jcrapi2.response.GetClanResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClanPlugin extends AbstractPlugin<ClanPluginEvent> {

  @Override
  public void onPluginEvent(Session session, ClanPluginEvent clanPluginEvent) {
    GetClanResponse getClanResponse = clanPluginEvent.getGetClanResponse();
    pluginManager.fire(new FluctuationPluginEvent(getClanResponse.getMemberList()));
  }

  @Override
  public boolean canHandlePluginEvent(PluginEvent pluginEvent) {
    return pluginEvent instanceof ClanPluginEvent;
  }

}

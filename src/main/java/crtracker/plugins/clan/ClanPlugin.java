package crtracker.plugins.clan;

import org.hibernate.Session;
import org.springframework.stereotype.Service;

import crtracker.plugin.AbstractPlugin;
import crtracker.plugin.PluginEvent;
import crtracker.plugins.fluctuation.FluctuationPluginEvent;
import jcrapi2.api.intern.clans.info.ClanResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClanPlugin extends AbstractPlugin<ClanPluginEvent> {

  @Override
  public void onPluginEvent(Session session, ClanPluginEvent clanPluginEvent) {
    ClanResponse clanResponse = clanPluginEvent.getClanResponse();
    pluginManager.fire(new FluctuationPluginEvent(clanResponse.getMemberList()));
  }

  @Override
  public boolean canHandlePluginEvent(PluginEvent pluginEvent) {
    return pluginEvent instanceof ClanPluginEvent;
  }

}

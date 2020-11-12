package crtracker.plugins.member;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import crtracker.persistency.Role;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugin.PluginEvent;
import jcrapi2.model.ClanMember;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClanMemberPlugin extends AbstractPlugin<ClanMemberPluginEvent> {

  @Autowired
  private MeasureDao measureDao;

  @Override
  public void onPluginEvent(Session session, ClanMemberPluginEvent clanMemberPluginEvent) {
    ClanMember clanMember = clanMemberPluginEvent.getClanMember();
    measureDao.updateNumberMeasure(session, clanMember.getTag(), CrTrackerTypes.MEMBER_DONATIONS.getCode(),
        clanMember.getDonations());
    measureDao.updateNumberMeasure(session, clanMember.getTag(), CrTrackerTypes.MEMBER_ROLE.getCode(),
        Role.forName(clanMember.getRole()).getCode());
    measureDao.updateStringMeasure(session, clanMember.getTag(), CrTrackerTypes.MEMBER_NICK.getCode(),
        clanMember.getName());
  }

  @Override
  public boolean canHandlePluginEvent(PluginEvent pluginEvent) {
    return pluginEvent instanceof ClanMemberPluginEvent;
  }

}

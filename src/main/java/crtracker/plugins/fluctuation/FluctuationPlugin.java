package crtracker.plugins.fluctuation;

import static java.util.Arrays.asList;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.persistency.model.StringMeasure;
import crtracker.persistency.model.TextMeasure;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugin.PluginEvent;
import jcrapi2.api.intern.clans.info.Member;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FluctuationPlugin extends AbstractPlugin<FluctuationPluginEvent> {

  @Autowired
  private MeasureDao measureDao;

  @Override
  public void onPluginEvent(Session session, FluctuationPluginEvent fluctuationPluginEvent) {
    List<Member> clanMembers = fluctuationPluginEvent.getMembers();
    String clanTag = configurationService.getClanTag();
    TextMeasure
        oldMembers =
        measureDao.updateTextMeasure(session, clanTag, CrTrackerTypes.CLAN_MEMBERS.getCode(),
            getClanMemberTags(clanMembers));
    if (oldMembers != null) {
      Set<String> old = new TreeSet<>(asList(oldMembers.getValue().split(",")));
      Set<String> current = new TreeSet<>(asList(getClanMemberTags(clanMembers).split(",")));
      if (!current.equals(old)) {
        Collection<String> newMembers = CollectionUtils.subtract(current, old);
        Collection<String> leftMembers = CollectionUtils.subtract(old, current);
        if (!newMembers.isEmpty()) {
          pluginManager.fire(new WelcomeMessagePluginEvent(resolveMemberTags(session, newMembers)));
        }
        if (!leftMembers.isEmpty()) {
          pluginManager.fire(new LeaveMessagePluginEvent(resolveMemberTags(session, leftMembers)));
        }
      }
    }
  }

  private Collection<String> resolveMemberTags(Session session, Collection<String> memberTags) {
    Collection<String> names = new TreeSet<>();
    memberTags.forEach(memberTag -> {
      log.info("try to resolve tag: {}", memberTag);
      StringMeasure name = measureDao.getCurrentStringMeasure(session, CrTrackerTypes.MEMBER_NICK, memberTag);
      if (name != null) {
        names.add(name.getValue());
      } else {
        names.add(memberTag + " (Springer)");
        log.warn("something weird while resolving name for tag: " + memberTag);
      }
    });
    return names;
  }

  private static String getClanMemberTags(List<Member> members) {
    Set<String> clanMemberTags = members.stream().map(Member::getTag).collect(Collectors.toCollection(TreeSet::new));
    return StringUtils.join(clanMemberTags, ",");
  }

  @Override
  public boolean canHandlePluginEvent(PluginEvent pluginEvent) {
    return pluginEvent instanceof FluctuationPluginEvent;
  }

}

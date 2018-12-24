package crtracker.checks;

import static java.util.Arrays.asList;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mili.utils.sql.service.ServiceException;
import org.mili.utils.sql.service.ServiceFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import crtracker.Config;
import crtracker.job.AbstractJob;
import crtracker.persistency.Role;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.persistency.model.StringMeasure;
import crtracker.persistency.model.TextMeasure;
import crtracker.service.MessageService;
import jcrapi2.Api;
import jcrapi2.model.ClanMember;
import jcrapi2.request.GetClanRequest;
import jcrapi2.response.GetClanResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataImporter extends AbstractJob {

    private final MessageService messageService = ServiceFactory.getService(MessageService.class);

    private final Config config;

    private final Api api;

    private final String clanTag;

    private final MeasureDao measureDao = new MeasureDao();

    public DataImporter(Config config) throws Exception {
        this.config = config;
        clanTag = config.getConfig().getProperty("crtracker.clan.tag");
        api = config.createApi();
    }

    @Override public long getTimeout() {
        return 60000;
    }

    @Override
    protected void runIntern() throws Exception {
        Session session = config.createSession();
        Transaction transaction = session.beginTransaction();
        try {
            doImport(session);
            transaction.commit();
        } catch (Exception e) {
            log.error("Error while importing data", e);
            if (transaction != null) {
                transaction.rollback();
            }
            messageService.sendAlert(config, "Error while importing data: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private void doImport(Session session) {
        importClan(session);
    }

    private void importClan(Session session) {
        GetClanResponse getClanResponse = api.getClan(GetClanRequest.builder(clanTag).build());
        importClan(session, getClanResponse);
    }

    private void importClan(Session session, GetClanResponse getClanResponse) {
        TextMeasure oldMembers = measureDao.updateTextMeasure(
                session,
                getClanResponse.getTag(),
                CrTrackerTypes.CLAN_MEMBERS.getCode(),
                getClanMemberTags(getClanResponse)
        );
        importMembers(session, getClanResponse.getMemberList());
        if (oldMembers != null) {
            Set<String> old = new TreeSet<>();
            old.addAll(asList(oldMembers.getValue().split(",")));

            Set<String> current = new TreeSet<>();
            current.addAll(asList(getClanMemberTags(getClanResponse).split(",")));

            if (!current.equals(old)) {
                Collection<String> newMembers = CollectionUtils.subtract(current, old);
                Collection<String> leftMembers = CollectionUtils.subtract(old, current);
                if (newMembers.size() > 0) {
                    messageService.sendWelcome(config,
                            "Willkommen im Clan! Bitte begrüßt die folgenden Neuankömmlinge im Clan-Chat:\n" + StringUtils.join(resolveMemberTags(session, newMembers), "\n"));
                }
                if (leftMembers.size() > 0) {
                    messageService.sendAlert(config, "Folgende Member haben den Clan verlassen:\n" + StringUtils
                            .join(resolveMemberTags(session, leftMembers), "\n"));
                }
            }
        }
    }

    private Collection<String> resolveMemberTags(Session session, Collection<String> memberTags) {
        Collection<String> names = new TreeSet<>();
        for (String memberTag : memberTags) {
            log.info("try to resolve tag: {}", memberTag);
            StringMeasure name = measureDao.getCurrentStringMeasure(session, CrTrackerTypes.MEMBER_NICK, memberTag);
            if (name != null) {
                names.add(name.getValue());
            } else {
                names.add(memberTag + " (Springer)");
                log.warn("something weird while resolving name for tag: " + memberTag);
            }
        }
        return names;
    }

    private void importMembers(Session session, List<ClanMember> clanMembers) {
        for (ClanMember clanMember : clanMembers) {
            measureDao.updateNumberMeasure(
                    session,
                    clanMember.getTag(),
                    CrTrackerTypes.MEMBER_DONATIONS.getCode(),
                    // TODO: bug in wrapper
                    Integer.valueOf(clanMember.getDonations())
            );
            measureDao.updateNumberMeasure(
                    session,
                    clanMember.getTag(),
                    CrTrackerTypes.MEMBER_ROLE.getCode(),
                    Role.forName(clanMember.getRole()).getCode()
            );
            measureDao.updateStringMeasure(
                    session,
                    clanMember.getTag(),
                    CrTrackerTypes.MEMBER_NICK.getCode(),
                    clanMember.getName()
            );
        }
    }

    private static String getClanMemberTags(GetClanResponse getClanResponse) {
        Set<String> clanMemberTags = new TreeSet<>();
        for (ClanMember clanMember : getClanResponse.getMemberList()) {
            clanMemberTags.add(clanMember.getTag());
        }
        return StringUtils.join(clanMemberTags, ",");
    }

}

package crtracker.checks;

import static java.util.Arrays.asList;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mili.utils.sql.service.ServiceFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import crtracker.Config;
import crtracker.api.ApiWrapper;
import crtracker.api.ClanData;
import crtracker.api.ClanDataMember;
import crtracker.api.PlayerBattleLogData;
import crtracker.job.AbstractJob;
import crtracker.persistency.Role;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.persistency.model.NumberMeasure;
import crtracker.persistency.model.StringMeasure;
import crtracker.persistency.model.TextMeasure;
import crtracker.service.MessageService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataImporter extends AbstractJob {

    private final static SimpleDateFormat BATTLE_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

    private final MessageService messageService = ServiceFactory.getService(MessageService.class);

    private final Config config;

    private final ApiWrapper apiWrapper;

    private final String clanTag;

    private final MeasureDao measureDao = new MeasureDao();

    public DataImporter(Config config) throws Exception {
        this.config = config;
        clanTag = config.getConfig().getProperty("crtracker.clan.tag");
        apiWrapper = config.createApiWrapper();
    }

    @Override
    public long getTimeout() {
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
        ClanData clanData = apiWrapper.getClanData(clanTag);
        importClan(session, clanData);
    }

    private void importClan(Session session, ClanData clanData) {
        TextMeasure oldMembers = measureDao
                .updateTextMeasure(session, clanData.getTag(), CrTrackerTypes.CLAN_MEMBERS.getCode(),
                        getClanMemberTags(clanData));
        importMembers(session, clanData.getClanDataMembers());
        if (oldMembers != null) {
            Set<String> old = new TreeSet<>();
            old.addAll(asList(oldMembers.getValue().split(",")));

            Set<String> current = new TreeSet<>();
            current.addAll(asList(getClanMemberTags(clanData).split(",")));

            if (!current.equals(old)) {
                Collection<String> newMembers = CollectionUtils.subtract(current, old);
                Collection<String> leftMembers = CollectionUtils.subtract(old, current);
                if (newMembers.size() > 0) {
                    messageService.sendWelcome(config,
                            "Willkommen im Clan! Bitte begrüßt die folgenden Neuankömmlinge im Clan-Chat:\n"
                                    + StringUtils.join(resolveMemberTags(session, newMembers), "\n"));
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

    private void importMembers(Session session, List<ClanDataMember> clanDataMembers) {
        for (ClanDataMember clanDataMember : clanDataMembers) {
            measureDao.updateNumberMeasure(session, clanDataMember.getTag(), CrTrackerTypes.MEMBER_DONATIONS.getCode(),
                    clanDataMember.getDonations());
            measureDao.updateNumberMeasure(session, clanDataMember.getTag(), CrTrackerTypes.MEMBER_ROLE.getCode(),
                    Role.forName(clanDataMember.getRole()).getCode());
            measureDao.updateStringMeasure(session, clanDataMember.getTag(), CrTrackerTypes.MEMBER_NICK.getCode(),
                    clanDataMember.getName());
            importBattles(session, clanDataMember.getTag());
        }
    }

    private static String getClanMemberTags(ClanData clanData) {
        Set<String> clanMemberTags = new TreeSet<>();
        for (ClanDataMember clanDataMember : clanData.getClanDataMembers()) {
            clanMemberTags.add(clanDataMember.getTag());
        }
        return StringUtils.join(clanMemberTags, ",");
    }

    private void importBattles(Session session, String playerTag) {
        NumberMeasure lastBattleIdMeasure = measureDao
                .getCurrentNumberMeasure(session, CrTrackerTypes.MEMBER_LAST_TEST_1V1, playerTag);
        long lastBattleTimeMillis = lastBattleIdMeasure != null ? lastBattleIdMeasure.getValue() : 0;
        PlayerBattleLogData playerBattleLogData = apiWrapper.getBattleLogFor(playerTag);
        for (PlayerBattleLogData.PlayerBattleLogDataEntry playerBattleLogDataEntry : playerBattleLogData.getEntries()) {
            if ("clanmate".equalsIgnoreCase(playerBattleLogDataEntry.getType())) {
                try {
                    Date battleTime = BATTLE_TIME_FORMAT.parse(playerBattleLogDataEntry.getId());
                    long battleTimeMillis = battleTime.getTime();
                    if (battleTimeMillis > lastBattleTimeMillis) {
                        lastBattleTimeMillis = battleTimeMillis;
                        int player1Crowns = playerBattleLogDataEntry.getPlayer1Crowns();
                        int player2Crowns = playerBattleLogDataEntry.getPlayer2Crowns();
                        NumberMeasure ratingMeasure = measureDao
                                .getCurrentNumberMeasure(session, CrTrackerTypes.INTERN_TOURNAMENT, playerTag);
                        long rating = ratingMeasure != null ? ratingMeasure.getValue() : 0;
                        if (player1Crowns > player2Crowns) {
                            rating += player1Crowns;
                        } else if (player1Crowns < player2Crowns) {
                            rating += player2Crowns;
                        } else {
                            rating += player1Crowns;
                        }
                        measureDao.updateNumberMeasure(session, playerTag, CrTrackerTypes.INTERN_TOURNAMENT.getCode(),
                                rating);
                        measureDao
                                .updateNumberMeasure(session, playerTag, CrTrackerTypes.MEMBER_LAST_TEST_1V1.getCode(),
                                        lastBattleTimeMillis);
                        messageService.sendLiga(config,
                                String.format("%s VS %s: %s:%s", playerBattleLogDataEntry.getPlayer1Name(),
                                        playerBattleLogDataEntry.getPlayer2Name(), player1Crowns, player2Crowns));
                    }
                } catch (ParseException e) {
                    log.warn("error while parsing battle time", e);
                }
            }
        }
    }

}

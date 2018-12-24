/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package crtracker.challenge;

import org.hibernate.Session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Lieshoff
 */
public class ChallengeDao {

    public Map<Long, ChallengeDefinition> getActiveChallengeDefinitions(Session session) {
        List<ChallengeDefinition> list = session.createQuery("from " + ChallengeDefinition.class.getName() + " t where t.active=:active")
                .setBoolean("active", true)
                .list();
        Map<Long, ChallengeDefinition> map = new HashMap<>();
        for (ChallengeDefinition challengeDefinition : list) {
            map.put(challengeDefinition.getId(), challengeDefinition);
        }
        return map;
    }

    public List<RunningChallenge> getRunningChallenges(Session session) {
        return session.createQuery("from " + RunningChallenge.class.getName() + " t where t.challengeStatus=:challengeStatus")
                .setByte("challengeStatus", ChallengeStatus.RUNNING.getCode())
                .list();
    }

}

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

package crtracker.persistency.model;

/**
 * @author Michael Lieshoff
 */
public enum CrTrackerTypes implements MeasureType {

  ID(-1),
  CLAN_MEMBERS(1),
  CLAN_DONATIONS(2),
  MEMBER_DONATIONS(3),
  MEMBER_CROWNS(4),
  MEMBER_NICK(5),
  MEMBER_BANNED_NICK(6),
  MEMBER_ROLE(7),
  CHALLENGE(8),
  INTERN_TOURNAMENT(9),
  MEMBER_LAST_TIME_LIGA_BATTLE(10),
  MEMBER_LAST_TIME_BATTLE(11),
  MEMBER_RIVER_WARS_SHIP_ATTACKS(12),
  MEMBER_RIVER_WARS_SHIP_REPAIRPOINTS(13),
  MEMBER_RIVER_WARS_FAME(14),
  MEMBER_LAST_10_RIVER_WARS_FAME(15),
  ;

  private final int code;

  CrTrackerTypes(int code) {
    this.code = code;
  }

  @Override
  public int getCode() {
    return code;
  }

}

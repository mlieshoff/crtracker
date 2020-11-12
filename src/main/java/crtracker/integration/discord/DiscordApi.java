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

package crtracker.integration.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.security.auth.login.LoginException;

public class DiscordApi {

  private final JDA jda;

  public DiscordApi(String token) throws LoginException, InterruptedException {
    jda = JDABuilder.createLight(token)
        .setAutoReconnect(true)
        .build();
    jda.awaitReady();
  }

  public void sendMessage(long channelId, Message message) {
    jda.getTextChannelById(channelId).sendMessage(message).submit();
  }

  public void sendMessageEmbedd(long channelId, MessageEmbed messageEmbed) {
    jda.getTextChannelById(channelId).sendMessage(messageEmbed).submit();
  }

}

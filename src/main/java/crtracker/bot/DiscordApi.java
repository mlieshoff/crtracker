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

package crtracker.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

import java.util.Properties;
import javax.security.auth.login.LoginException;

/**
 * @author Michael Lieshoff
 */
public class DiscordApi {

  private JDA jda;

  private Properties config;

  public DiscordApi(Properties config, Properties credentials)
      throws LoginException, InterruptedException, RateLimitedException {
    this.config = config;
    jda = JDABuilder.createLight(credentials.getProperty("discord.token"))
        .setAutoReconnect(true)
        .build();
    jda.awaitReady();
  }

  public void sendAlert(Message message) {
    jda.getTextChannelById(Long.valueOf(config.getProperty("discord.channel.alerts"))).sendMessage(message)
        .submit();
  }

  public void sendWelcome(Message message) {
    jda.getTextChannelById(Long.valueOf(config.getProperty("discord.channel.welcome"))).sendMessage(message)
        .submit();
  }

  public void sendLiga(MessageEmbed messageEmbed) {
    jda.getTextChannelById(Long.valueOf(config.getProperty("discord.channel.liga"))).sendMessage(messageEmbed).submit();
  }

  public void sendLiveTicker(MessageEmbed messageEmbed) {
    jda.getTextChannelById(Long.valueOf(config.getProperty("discord.channel.liveticker"))).sendMessage(messageEmbed)
        .submit();
  }

}

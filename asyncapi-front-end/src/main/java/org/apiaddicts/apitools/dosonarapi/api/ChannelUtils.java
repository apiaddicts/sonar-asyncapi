/*
 * doSonarAPI: SonarQube AsyncAPI Plugin
 * Copyright (C) 2024-2024 Apiaddicts
 * contacta AT apiaddicts DOT org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.apiaddicts.apitools.dosonarapi.api;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ChannelUtils {
  public static final Pattern SPINAL_CASE_PATTERN = Pattern.compile("[a-z0-9-]+");

  public static boolean isVariable(String fragment) {
    return fragment.startsWith("{") && fragment.endsWith("}");
  }

  private ChannelUtils() {
    // Hidden utility type constructor
  }

  public static boolean checkChannel(String channel, Predicate<String> segmentChecker) {
    String[] fragments = channel.split("/");
    for (String fragment : fragments) {
      if (fragment.isEmpty() || isVariable(fragment)) {
        continue;
      }
      if (!segmentChecker.test(fragment)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isSpinalCase(String fragment) {
    return SPINAL_CASE_PATTERN.matcher(fragment).matches();
  }

  /**
   * A resource channel is a channel that does not end with a variable.
   * Examples:
   * <ul>
   * <li>{@code some/parrots/{parrotId}} is not resource</li>
   * <li>{@code some/parrots/{parrotId}} is not a resource</li>
   * <li>{@code some/parrots/{parrotId}/head-color} is a resource</li>
   * </ul>
   * @param channel the channel to examine
   * @return true if the channel is a resource channel
   */
  public static boolean isResourceChannel(String channel) {
    String[] fragments = channel.split("/");
    if (fragments.length == 0) {
      return true;
    }
    return !isVariable(fragments[fragments.length - 1]);
  }

  public static String terminalSegment(String channel) {
    channel = trimTrailingSlash(channel);
    String[] split = channel.split("/");
    return split[split.length - 1];
  }

  public static String trimTrailingSlash(String channel) {
    if (channel.endsWith("/")) {
      return channel.substring(0, channel.length() - 1);
    } else {
      return channel;
    }
  }
}

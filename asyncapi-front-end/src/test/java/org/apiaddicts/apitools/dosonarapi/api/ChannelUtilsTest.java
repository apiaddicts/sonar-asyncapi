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

import org.apiaddicts.apitools.dosonarapi.api.ChannelUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.apiaddicts.apitools.dosonarapi.api.ChannelUtils.checkChannel;
import static org.apiaddicts.apitools.dosonarapi.api.ChannelUtils.isResourceChannel;
import static org.apiaddicts.apitools.dosonarapi.api.ChannelUtils.terminalSegment;
import static org.apiaddicts.apitools.dosonarapi.api.ChannelUtils.trimTrailingSlash;

public class ChannelUtilsTest {
  @Test
  public void detects_nonspinal() {
    assertTrue(checkChannel("some/naive/path", ChannelUtils::isSpinalCase));
    assertTrue(checkChannel("some/naive/path-with-dashes", ChannelUtils::isSpinalCase));
    assertTrue(checkChannel("multi-spinal/path-example", ChannelUtils::isSpinalCase));
    assertTrue(checkChannel("multi-spinal/path-example/with-0123", ChannelUtils::isSpinalCase));

    assertFalse(checkChannel("Some/naive/Path", ChannelUtils::isSpinalCase));
    assertFalse(checkChannel("some_other/path", ChannelUtils::isSpinalCase));
    assertFalse(checkChannel("yetAnother/path", ChannelUtils::isSpinalCase));
    assertFalse(checkChannel("some/1234/weirdPath", ChannelUtils::isSpinalCase));
  }

  @Test
  public void ignores_path_variables() {
    assertTrue(checkChannel("pets/{someVariable}", ChannelUtils::isSpinalCase));
  }

  @Test
  public void detects_resoure_paths() {
    assertTrue(isResourceChannel("some/parrots"));
    assertTrue(isResourceChannel("/"));
    assertTrue(isResourceChannel("some/parrots/"));
    assertFalse(isResourceChannel("some/parrots/{parrotId}"));
    assertFalse(isResourceChannel("some/parrots/{parrotId}/"));
    assertTrue(isResourceChannel("some/parrots/{parrotId}/feather"));
    assertTrue(isResourceChannel("some/parrots/{parrotId}/feather/"));
  }

  @Test
  public void trims_slashes() {
    assertThat(trimTrailingSlash("/")).isEqualTo("");
    assertThat(trimTrailingSlash("something")).isEqualTo("something");
    assertThat(trimTrailingSlash("something/")).isEqualTo("something");
  }

  @Test
  public void extracts_terminal_segments() {
    assertThat(terminalSegment("/")).isEqualTo("");
    assertThat(terminalSegment("something")).isEqualTo("something");
    assertThat(terminalSegment("something/")).isEqualTo("something");
    assertThat(terminalSegment("something/else")).isEqualTo("else");
  }
}

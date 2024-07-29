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
package org.apiaddicts.apitools.dosonarapi.checks;

import org.junit.Test;
import org.apiaddicts.apitools.dosonarapi.AsyncApiCheckVerifier;

import static org.junit.Assert.assertEquals;
import static org.apiaddicts.apitools.dosonarapi.checks.ChannelAmbiguityCheck.ConflictMode.AMBIGUOUS;
import static org.apiaddicts.apitools.dosonarapi.checks.ChannelAmbiguityCheck.ConflictMode.CLEAR;
import static org.apiaddicts.apitools.dosonarapi.checks.ChannelAmbiguityCheck.split;

public class ChannelAmbiguityCheckTest {

  private static ChannelAmbiguityCheck.ConflictMode hasConflicts(String tested, String reference) {
    String[] testedParts = split(tested);
    String[] referenceParts = split(reference);
    return new ChannelAmbiguityCheck.ConflictChecker().check(testedParts, referenceParts);
  }

  @Test
  public void verify_channel_ambiguity() {
    AsyncApiCheckVerifier.verify("src/test/resources/checks/v4/channel-ambiguity.yaml", new ChannelAmbiguityCheck());
  }

  @Test
  public void channels_of_different_length_dont_conflict() {
    assertEquals(CLEAR, hasConflicts("pets", "animals/{animalId}/name"));
    assertEquals(CLEAR, hasConflicts("pets/{petId}", "pets/1234"));
  }

  @Test
  public void different_channels_of_same_length_dont_conflict() {
    assertEquals(CLEAR, hasConflicts("pets", "animals/{animalId}"));
  }

  @Test
  public void channels_with_parameter_leaves_are_ambiguous() {
    assertEquals(AMBIGUOUS, hasConflicts("pets/{petId}", "pets/{petId}"));
    assertEquals(AMBIGUOUS, hasConflicts("animals/{animalId}/name", "animals/{animalId}/name"));
  }

/* 
  @Test
  public void channels_with_mixed_parameters_are_ambiguous() {
    assertEquals(AMBIGUOUS, hasConflicts("pets/{petId}", "{pets}/1234"));
    assertEquals(AMBIGUOUS, hasConflicts("animals/{animalId}/name", "{animals}/{animalId}/details"));
  }

  @Test
  public void channels_with_shared_parameters_are_not_ambiguous() {
    assertEquals(CLEAR, hasConflicts("pets/{petId}/details", "pets/{petId}/info"));
  }

  @Test
  public void parameter_is_masked_by_defined_segment() {
    assertEquals(AMBIGUOUS, hasConflicts("pets/bella", "pets/{petId}"));
    assertEquals(AMBIGUOUS, hasConflicts("animals/{animalId}/name", "animals/bella/name"));
  }*/
}

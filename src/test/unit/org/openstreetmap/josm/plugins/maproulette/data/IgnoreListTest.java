package org.openstreetmap.josm.plugins.maproulette.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.testutils.annotations.BasicPreferences;

@BasicPreferences
class IgnoreListTest {
    @Test
    void testIgnores() {
        // Set up the "basic" list
        for (int i = 0; i < 10; i++) {
            IgnoreList.ignoreTask(i);
            IgnoreList.ignoreChallenge(i);
        }
        for (int i = 20; i < 30; i++) {
            IgnoreList.ignoreTask(i);
            IgnoreList.ignoreChallenge(i);
        }
        IgnoreList.ignoreTask(15);
        IgnoreList.ignoreChallenge(15);
        assertAll(() -> assertTrue(IgnoreList.isTaskIgnored(0)),
                () -> assertTrue(IgnoreList.isChallengeIgnored(0)),
                () -> assertTrue(IgnoreList.isTaskIgnored(9)),
                () -> assertTrue(IgnoreList.isChallengeIgnored(9)),
                () -> assertTrue(IgnoreList.isTaskIgnored(15)),
                () -> assertTrue(IgnoreList.isChallengeIgnored(15)),
                () -> assertTrue(IgnoreList.isTaskIgnored(20)),
                () -> assertTrue(IgnoreList.isChallengeIgnored(20)),
                () -> assertTrue(IgnoreList.isTaskIgnored(29)),
                () -> assertTrue(IgnoreList.isChallengeIgnored(29)));
    }
}
// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.parsers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.openstreetmap.josm.plugins.maproulette.util.RecordAssertion.assertRecordsEqual;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.plugins.maproulette.api.ChallengeAPI;
import org.openstreetmap.josm.plugins.maproulette.api.enums.Priority;
import org.openstreetmap.josm.plugins.maproulette.api.model.Challenge;
import org.openstreetmap.josm.plugins.maproulette.api.model.ChallengeCreation;
import org.openstreetmap.josm.plugins.maproulette.api.model.ChallengeExtra;
import org.openstreetmap.josm.plugins.maproulette.api.model.ChallengeGeneral;
import org.openstreetmap.josm.plugins.maproulette.api.model.ChallengePriority;
import org.openstreetmap.josm.plugins.maproulette.api.model.Point;
import org.openstreetmap.josm.plugins.maproulette.util.MapRouletteConfig;

/**
 * Test class for challenge parsing
 */
@MapRouletteConfig
class ChallengeParserTest {
    @Test
    void testChallenge15318() {
        final var challenge = assertDoesNotThrow(() -> ChallengeAPI.challenge(15318));
        final var expected = new Challenge(15318, "Add direction to Stop - USA Los Angeles Timezone",
                Instant.parse("2020-11-28T18:22:24.399Z"), Instant.parse("2023-01-24T14:21:34.897Z"),
                "This challenge will show every [highway=stop](https://wiki.openstreetmap.org/wiki/Tag:highway%3Dstop) without [direction=*](https://wiki.openstreetmap.org/wiki/Key:direction). Your goal is to add tag \"direction\" with value: \"forward\", \"backward\" or \"both\" for every stop. Read article [highway=stop](https://wiki.openstreetmap.org/wiki/Tag:highway%3Dstop) to know how to map.\n\n#### Overpass query\n[All my queries](https://wiki.openstreetmap.org/wiki/User:Binnette/OverpassQueries)\n\n#### About Binnette\n[Twitch](https://www.twitch.tv/binnettetv) - [Twitter](https://twitter.com/BinnetteBin) - [Wiki](https://wiki.openstreetmap.org/wiki/User:Binnette)",
                false, "",
                new ChallengeGeneral(918586, 39866,
                        "Add tag direction with value: \"forward\", \"backward\" or \"both\" for every stop. Read article [highway=stop](https://wiki.openstreetmap.org/wiki/Tag:highway%3Dstop) to know how to map.\n\nEx: direction=forward / direction=backward / direction=both\n\nNote:\n- for this usecase iD editor is great and shows directions.\n- with iD to change direction, click on the node and press v (reverse shortcut).",
                        2,
                        "Add direction=forward/backward/both to [highway=stop](https://wiki.openstreetmap.org/wiki/Tag:highway%3Dstop).",
                        true, false, 0, 1674515454, "Add tag direction to highway=stop - USA LA Timezone #maproulette",
                        "", false, new long[0], false),
                new ChallengeCreation(
                        "area[name=\"America/Los_Angeles Timezone\"]->.a;\nnode[\"highway\"=\"stop\"][!\"direction\"][!\"stop\"](area.a);\nway(bn)[\"highway\"][\"oneway\"!=\"yes\"];\nnode(w)[\"highway\"=\"stop\"][!\"direction\"];\nout skel;",
                        "", "node"),
                new ChallengePriority(Priority.LOW, "{}", "{}", "{}"),
                new ChallengeExtra(19, 3, 19, -1, "", "", false, "", "", null, null, false, false, "[]", "", false,
                        null),
                3, "Request timeout to overpass-api.de/178.63.48.217:80 after 120000 ms",
                Instant.parse("2022-07-23T07:13:38.609Z"), Instant.parse("2022-07-23T07:13:38.609Z"),
                new Point(38.3241717215975, -120.521573627918),
                "{\"type\":\"Polygon\",\"coordinates\":[[[-124.586248289365,32.5435948864956],[-124.586248289365,48.9940324531592],[-114.103130987674,48.9940324531592],[-114.103130987674,32.5435948864956],[-124.586248289365,32.5435948864956]]]}",
                4, 29734);
        assertRecordsEqual(expected, challenge);
    }
}

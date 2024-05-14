// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.openstreetmap.josm.plugins.maproulette.api.model.Challenge;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api_caching.ChallengeCache;
import org.openstreetmap.josm.plugins.maproulette.markdown.SelectParser;
import org.openstreetmap.josm.plugins.maproulette.util.ExceptionDialogUtil;
import org.openstreetmap.josm.tools.Utils;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * A helper class for gui rendering
 */
public final class MRGuiHelper {
    /**
     * Replace mustache variables (see <a href="https://learn.maproulette.org/documentation/mustache-tag-replacement/">
     *     MapRoulette mustache tag replacement
     * </a>)
     */
    private static final Pattern MUSTACHE_PATTERN = Pattern.compile("\\{{2}(.*?)}{2}");
    /**
     * Keys that should not be found in OSM but are often present in task metadata
     */
    private static final Set<String> DISCARDABLE_KEYS = Set.of(
            // Common MR tags used for identifiers
            "id", "@id", "osmid", "osm_id", "osmIdentifier",
            // Atlas checks
            "flag:check", "flag:generator", "identifier", "itemType", "last_edit_changeset", "last_edit_time",
            "last_edit_user_id", "last_edit_version");

    private MRGuiHelper() {
        // Hide constructor
    }

    /**
     * Get the instruction text for a task
     *
     * @param currentTask The task to get instructiohns for
     * @return The instruction text
     */
    @Nonnull
    public static String getInstructionText(@Nullable Task currentTask) {
        if (currentTask == null) {
            return "";
        }
        final String instruction;
        if (Utils.isStripEmpty(currentTask.instruction())) {
            Challenge challenge;
            try {
                challenge = ChallengeCache.challenge(currentTask.parentId());
            } catch (IOException ioException) {
                ExceptionDialogUtil.explainException(ioException);
                challenge = null;
            }
            if (challenge == null) {
                instruction = tr("Could not fetch instruction from parent challenge");
            } else if (!Utils.isStripEmpty(challenge.general().instruction())) {
                instruction = challenge.general().instruction();
            } else if (!Utils.isStripEmpty(challenge.description())) {
                instruction = challenge.description();
            } else {
                instruction = challenge.name();
            }
        } else {
            instruction = currentTask.instruction();
        }
        final var matcher = MUSTACHE_PATTERN.matcher(instruction);
        final var builder = new StringBuilder(instruction.length());
        while (matcher.find()) {
            final var tag = matcher.group(1);
            final var replacement = currentTask.geometries().allPrimitives().stream().map(prim -> prim.get(tag))
                    .filter(Objects::nonNull).collect(Collectors.joining(System.lineSeparator()));
            matcher.appendReplacement(builder, Utils.isStripEmpty(replacement) ? matcher.group(0) : replacement);
        }
        matcher.appendTail(builder);

        // Instructions can be markdown, so we want to convert it to html
        final var selectParser = Collections.singletonList(new SelectParser());
        final var node = Parser.builder().extensions(selectParser).build().parse(builder.toString());
        return "<html>" + HtmlRenderer.builder().extensions(selectParser).build().render(node) + "</html>";
    }

    /**
     * Filter keys (to remove keys that are from challenge objects)
     *
     * @param keys The keys to filter
     * @return The filtered stream
     */
    public static Stream<String> filterKeys(Stream<String> keys) {
        return keys.filter(DISCARDABLE_KEYS::contains);
    }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link SelectParser}
 */
class SelectParserTest {
    private HtmlRenderer renderer;
    private Parser parser;

    @BeforeEach
    void setup() {
        renderer = HtmlRenderer.builder().extensions(Collections.singletonList(new SelectParser())).build();
        parser = Parser.builder().extensions(Collections.singletonList(new SelectParser())).build();
    }

    static List<Arguments> testSelect() {
        final var renderSelect = "\n<select>\n<option selected=\"\" value=\" \"> </option>\n<option value=\"One\">One</option>\n<option value=\"Two\">Two</option>\n<option value=\"Three\">Three</option>\n</select>\n";
        final var markdownSelect = "[select \" \" values=\"One,Two,Three\"]";
        return Arrays.asList(Arguments.of("<p>" + renderSelect + "</p>\n", markdownSelect),
                Arguments.of("<p>Matching: " + renderSelect + "</p>\n", "Matching: " + markdownSelect),
                Arguments.of("<p>" + renderSelect + " values</p>\n", markdownSelect + " values"),
                Arguments.of("<p>Matching: " + renderSelect + " values</p>\n",
                        "Matching: " + markdownSelect + " values"),
                Arguments.of(
                        "<p>\n<select name=\"Test Name\">\n<option selected=\"\" value=\" \"> </option>\n<option value=\"One\">One</option>\n<option value=\"Two\">Two</option>\n<option value=\"Three\">Three</option>\n</select>\n</p>\n",
                        "[select \" \" name=\"Test Name\" values=\"One,Two,Three\"]"));
    }

    @ParameterizedTest
    @MethodSource
    void testSelect(String expectedRender, String input) {
        final var node = parser.parse(input);
        final var render = renderer.render(node);
        assertEquals(expectedRender, render);
    }
}
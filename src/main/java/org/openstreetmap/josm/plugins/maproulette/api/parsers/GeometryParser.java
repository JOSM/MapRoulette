// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.parsers;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.DataSource;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.io.GeoJSONReader;
import org.openstreetmap.josm.io.IllegalDataException;

/**
 * Parse geometry data
 */
final class GeometryParser {
    /**
     * Hide the constructor
     */
    private GeometryParser() {
        // Hide the constructor
    }

    /**
     * Parse an input string
     *
     * @param input The string to parse
     * @return The parsed output
     * @throws IllegalDataException If something is wrong with the input data
     */
    static DataSet parse(String input) throws IllegalDataException {
        try (var reader = Json.createReader(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)))) {
            final var value = reader.readValue();
            final var ds = new DataSet();
            if (value instanceof JsonObject object) {
                parseObject(ds, object);
            } else if (value instanceof JsonArray array) {
                parseArray(ds, array);
            }
            final var dsBox = new BBox();
            ds.allPrimitives().stream().map(IPrimitive::getBBox).forEach(dsBox::add);
            final var bounds = new Bounds(dsBox.getBottomRight());
            bounds.extend(dsBox.getTopLeft());
            ds.addDataSource(new DataSource(bounds, null));
            return ds;
        }
    }

    /**
     * Parse an array of objects
     *
     * @param ds    The dataset to add the objects to
     * @param array The array to parse
     * @throws IllegalDataException If something could not be processed
     */
    private static void parseArray(DataSet ds, JsonArray array) throws IllegalDataException {
        for (var value : array) {
            if (value instanceof JsonArray a) {
                parseArray(ds, a);
            } else if (value instanceof JsonObject obj) {
                parseObject(ds, obj);
            }
        }
    }

    /**
     * Parse a json object
     *
     * @param ds     The dataset to merge the data into
     * @param object The object to parse
     * @throws IllegalDataException If the object is not parseable
     */
    private static void parseObject(DataSet ds, JsonObject object) throws IllegalDataException {
        if (object.containsKey("type")) {
            ds.mergeFrom(GeoJSONReader.parseDataSet(
                    new ByteArrayInputStream(object.toString().getBytes(StandardCharsets.UTF_8)),
                    NullProgressMonitor.INSTANCE));
        } else if (object.containsKey("features")) {
            final var value = object.get("features");
            if (value instanceof JsonObject obj) {
                parseObject(ds, obj);
            } else if (value instanceof JsonArray array) {
                parseArray(ds, array);
            } else {
                throw new IllegalDataException(object.toString());
            }
        } else {
            throw new IllegalDataException(object.toString());
        }
    }

}

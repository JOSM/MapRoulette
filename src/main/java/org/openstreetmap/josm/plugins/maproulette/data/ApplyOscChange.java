// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.data;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Apply an OSC change (stored as a {@link DataSet})
 */
public class ApplyOscChange extends Command {
    /**
     * The dataset to merge from
     */
    private final DataSet toMergeFrom;

    /**
     * Creates a new command in the context of a specific data set, without data layer
     *
     * @param data        the data set. Must not be null.
     * @param toMergeFrom The dataset to merge from
     * @throws IllegalArgumentException if data is null
     * @since 11240
     */
    public ApplyOscChange(DataSet data, DataSet toMergeFrom) {
        super(data);
        if (toMergeFrom == null) {
            throw new IllegalArgumentException("toMergeFrom must not be null");
        }
        this.toMergeFrom = toMergeFrom;
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted,
            Collection<OsmPrimitive> added) {
        final var ds = getAffectedDataSet();
        new MergeDataSetsCommand(ds, this.toMergeFrom, true, null).fillModifiedData(modified, deleted, added);
    }

    @Override
    public String getDescriptionText() {
        return tr("MapRoulette OSC change");
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + this.toMergeFrom.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && ((ApplyOscChange) obj).toMergeFrom.equals(this.toMergeFrom);
    }
}

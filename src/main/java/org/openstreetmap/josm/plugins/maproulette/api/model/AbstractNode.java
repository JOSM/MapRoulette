// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.INode;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.IRelation;
import org.openstreetmap.josm.data.osm.IWay;
import org.openstreetmap.josm.data.osm.OsmData;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.User;
import org.openstreetmap.josm.data.osm.visitor.PrimitiveVisitor;
import org.openstreetmap.josm.gui.mappaint.StyleCache;

/**
 * An abstract node implementation for use with a painter
 */
public interface AbstractNode extends INode {
    @Override
    default boolean isModified() {
        return false;
    }

    @Override
    default void setModified(boolean modified) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isVisible() {
        return true;
    }

    @Override
    default void setVisible(boolean visible) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isDeleted() {
        return false;
    }

    @Override
    default void setDeleted(boolean deleted) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isIncomplete() {
        return true;
    }

    @Override
    default boolean isUndeleted() {
        return false;
    }

    @Override
    default boolean isUsable() {
        return true;
    }

    @Override
    default boolean isNewOrUndeleted() {
        return false;
    }

    @Override
    default boolean isReferredByWays(int n) {
        return n == 0;
    }

    @Override
    default long getOsmId() {
        throw new UnsupportedOperationException();
    }

    @Override
    default int getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void setOsmId(long id, int version) {
        throw new UnsupportedOperationException();
    }

    @Override
    default User getUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void setUser(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Instant getInstant() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void setInstant(Instant timestamp) {
        throw new UnsupportedOperationException();
    }

    @Override
    default int getRawTimestamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void setRawTimestamp(int timestamp) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isTimestampEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    default int getChangesetId() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void setChangesetId(int changesetId) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void accept(PrimitiveVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    default void visitReferrers(PrimitiveVisitor visitor) {
        for (IPrimitive ref : this.getReferrers()) {
            if (ref instanceof INode n) {
                visitor.visit(n);
            } else if (ref instanceof IWay<?> w) {
                visitor.visit(w);
            } else if (ref instanceof IRelation<?> r) {
                visitor.visit(r);
            }
        }
    }

    @Override
    default boolean isHighlighted() {
        return false;
    }

    @Override
    default void setHighlighted(boolean highlighted) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isTagged() {
        return !this.getInterestingTags().isEmpty();
    }

    @Override
    default Map<String, String> getInterestingTags() {
        return Collections.emptyMap();
    }

    @Override
    default boolean isAnnotated() {
        return true;
    }

    @Override
    default boolean hasDirectionKeys() {
        return false;
    }

    @Override
    default boolean reversedDirection() {
        return false;
    }

    @Override
    default BBox getBBox() {
        return new BBox(this);
    }

    @Override
    default List<? extends IPrimitive> getReferrers(boolean allowWithoutDataset) {
        return Collections.emptyList();
    }

    @Override
    default OsmData<?, ?, ?, ?> getDataSet() {
        throw new UnsupportedOperationException();
    }

    @Override
    default long getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    default LatLon getCoor() {
        return null;
    }

    @Override
    default void setCoor(LatLon coor) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void setEastNorth(EastNorth eastNorth) {
        throw new UnsupportedOperationException();
    }

    @Override
    default long getUniqueId() {
        throw new UnsupportedOperationException();
    }

    @Override
    default OsmPrimitiveType getType() {
        return null;
    }

    @Override
    default boolean isNew() {
        return false;
    }

    @Override
    default StyleCache getCachedStyle() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void setCachedStyle(StyleCache mappaintStyle) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isCachedStyleUpToDate() {
        return false;
    }

    @Override
    default void declareCachedStyleUpToDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    default Map<String, String> getKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void setKeys(Map<String, String> keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    default String get(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void remove(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean hasKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    default Collection<String> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    default int getNumKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void removeAll() {
        throw new UnsupportedOperationException();
    }
}

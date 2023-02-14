// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.data;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.DataSource;
import org.openstreetmap.josm.data.conflict.Conflict;
import org.openstreetmap.josm.data.conflict.ConflictCollection;
import org.openstreetmap.josm.data.osm.DataIntegrityProblemException;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.tools.JosmRuntimeException;

/**
 * This is intended to replace {@link org.openstreetmap.josm.data.osm.DataSetMerger}.
 * It takes primitives from one dataset and puts them in a different dataset.
 * TODO move to JOSM core
 */
public class MergeDataSetsCommand extends Command {
    /**
     * {@code true} if we want to merge DataSet bounds
     */
    private final boolean mergeBounds;
    /**
     * The progress monitor to show the user that <i>something</i> is happening
     */
    private final ProgressMonitor progressMonitor;

    /**
     * the collection of conflicts created during merging
     */
    private final ConflictCollection conflicts = new ConflictCollection();

    /**
     * the target dataset for merging
     */
    private final DataSet targetDataSet;
    /**
     * the source dataset where primitives are merged from
     */
    private final DataSet sourceDataSet;

    /**
     * A map of all primitives that got replaced with other primitives.
     * Key is the PrimitiveId in their dataset, the value is the PrimitiveId in my dataset
     */
    private final Map<PrimitiveId, PrimitiveId> mergedMap = new HashMap<>();
    /**
     * a set of primitive ids for which we have to fix references (to nodes and
     * to relation members) after the first phase of merging
     */
    private final Set<PrimitiveId> objectsWithChildrenToMerge = new HashSet<>();
    private final Set<OsmPrimitive> objectsToDelete = new HashSet<>();

    /**
     * The objects that are added to {@link #targetDataSet}
     */
    private Collection<OsmPrimitive> added = new HashSet<>();
    /**
     * The objects that are modified in {@link #targetDataSet}
     */
    private Collection<OsmPrimitive> modified = new HashSet<>();
    /**
     * The objects that are deleted in {@link #targetDataSet}
     */
    private Collection<OsmPrimitive> deleted = new HashSet<>();

    /**
     * Create a new merge command
     *
     * @param sourceDataSet   The dataset from which data is coming (this will be returned by {@link #getAffectedDataSet()})
     * @param targetDataSet   The dataset to which data is going
     * @param mergeBounds     {@code true} if the dataset bounds should be merged
     * @param progressMonitor The progress monitor to use for potentially long-running operations
     */
    public MergeDataSetsCommand(@Nonnull DataSet targetDataSet, @Nonnull DataSet sourceDataSet, boolean mergeBounds,
            @Nullable ProgressMonitor progressMonitor) {
        super(targetDataSet);
        Objects.requireNonNull(targetDataSet);
        Objects.requireNonNull(sourceDataSet);
        this.targetDataSet = targetDataSet;
        this.sourceDataSet = sourceDataSet;
        this.mergeBounds = mergeBounds;
        this.progressMonitor = progressMonitor;
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified, Collection<OsmPrimitive> deleted,
            Collection<OsmPrimitive> added) {
        if (sourceDataSet == null)
            return;
        this.modified = modified;
        this.deleted = deleted;
        this.added = added;
        if (progressMonitor != null) {
            progressMonitor.beginTask(tr("Merging data..."), sourceDataSet.allPrimitives().size());
        }
        targetDataSet.update(() -> {
            List<? extends OsmPrimitive> candidates = null;
            for (Node node : sourceDataSet.getNodes()) {
                // lazy initialisation to improve performance, see #19898
                if (candidates == null) {
                    candidates = new ArrayList<>(targetDataSet.getNodes());
                }
                mergePrimitive(node, candidates);
                if (progressMonitor != null) {
                    progressMonitor.worked(1);
                }
            }
            candidates = null;
            for (Way way : sourceDataSet.getWays()) {
                // lazy initialisation to improve performance
                if (candidates == null) {
                    candidates = new ArrayList<>(targetDataSet.getWays());
                }
                mergePrimitive(way, candidates);
                if (progressMonitor != null) {
                    progressMonitor.worked(1);
                }
            }
            candidates = null;
            for (Relation relation : sourceDataSet.getRelations()) {
                // lazy initialisation to improve performance
                if (candidates == null) {
                    candidates = new ArrayList<>(targetDataSet.getRelations());
                }
                mergePrimitive(relation, candidates);
                if (progressMonitor != null) {
                    progressMonitor.worked(1);
                }
            }
            candidates = null;
            fixReferences();

            Area a = targetDataSet.getDataSourceArea();

            // copy the merged layer's data source info.
            // only add source rectangles if they are not contained in the layer already.
            if (mergeBounds) {
                for (DataSource src : sourceDataSet.getDataSources()) {
                    if (a == null || !a.contains(src.bounds.asRect())) {
                        targetDataSet.addDataSource(src);
                        // TODO remove dataSource code when in core during undo
                    }
                }
            }

            // copy the merged layer's API version
            if (targetDataSet.getVersion() == null) {
                targetDataSet.setVersion(sourceDataSet.getVersion());
            }

            // copy the merged layer's policies and locked status
            if (sourceDataSet.getUploadPolicy() != null && (targetDataSet.getUploadPolicy() == null
                    || sourceDataSet.getUploadPolicy().compareTo(targetDataSet.getUploadPolicy()) > 0)) {
                targetDataSet.setUploadPolicy(sourceDataSet.getUploadPolicy());
            }
            if (sourceDataSet.getDownloadPolicy() != null && (targetDataSet.getDownloadPolicy() == null
                    || sourceDataSet.getDownloadPolicy().compareTo(targetDataSet.getDownloadPolicy()) > 0)) {
                targetDataSet.setDownloadPolicy(sourceDataSet.getDownloadPolicy());
            }
            if (sourceDataSet.isLocked() && !targetDataSet.isLocked()) {
                targetDataSet.lock();
            }
        });
        if (progressMonitor != null) {
            progressMonitor.finishTask();
        }
    }

    /**
     * Merges a primitive onto primitives dataset.
     * <p>
     * If other.id != 0 it tries to merge it with an corresponding primitive from
     * my dataset with the same id. If this is not possible a conflict is remembered
     * in {@link #conflicts}.
     * <p>
     * If other.id == 0 (new primitive) it tries to find a primitive in my dataset with id == 0 which
     * is semantically equal. If it finds one it merges its technical attributes onto
     * my primitive.
     *
     * @param source     the primitive to merge
     * @param candidates a set of possible candidates for a new primitive
     */
    protected void mergePrimitive(OsmPrimitive source, Collection<? extends OsmPrimitive> candidates) {
        if (!source.isNew()) {
            // try to merge onto a matching primitive with the same defined id
            //
            if (mergeById(source))
                return;
        } else {
            // ignore deleted primitives from source
            if (source.isDeleted())
                return;

            // try to merge onto a primitive  which has no id assigned
            // yet but which is equal in its semantic attributes
            //
            for (OsmPrimitive target : candidates) {
                if (!target.isNew() || target.isDeleted()) {
                    continue;
                }
                if (target.hasEqualSemanticAttributes(source)) {
                    mergedMap.put(source.getPrimitiveId(), target.getPrimitiveId());
                    // copy the technical attributes from other version
                    target.setVisible(source.isVisible());
                    target.setUser(source.getUser());
                    target.setRawTimestamp(source.getRawTimestamp());
                    target.setModified(source.isModified());
                    objectsWithChildrenToMerge.add(source.getPrimitiveId());
                    return;
                }
            }
        }

        // If we get here we didn't find a suitable primitive in
        // the target dataset. Create a clone and add it to the target dataset.
        //
        OsmPrimitive target;
        switch (source.getType()) {
        case NODE:
            target = source.isNew() ? new Node() : new Node(source.getId());
            break;
        case WAY:
            target = source.isNew() ? new Way() : new Way(source.getId());
            break;
        case RELATION:
            target = source.isNew() ? new Relation() : new Relation(source.getId());
            break;
        default:
            throw new AssertionError();
        }
        target.mergeFrom(source);
        targetDataSet.addPrimitive(target);
        this.added.add(target);
        mergedMap.put(source.getPrimitiveId(), target.getPrimitiveId());
        objectsWithChildrenToMerge.add(source.getPrimitiveId());
    }

    protected OsmPrimitive getMergeTarget(OsmPrimitive mergeSource) {
        PrimitiveId targetId = mergedMap.get(mergeSource.getPrimitiveId());
        if (targetId == null)
            return null;
        return targetDataSet.getPrimitiveById(targetId);
    }

    protected void addConflict(Conflict<?> c) {
        c.setMergedMap(mergedMap);
        conflicts.add(c);
    }

    protected void addConflict(OsmPrimitive my, OsmPrimitive their) {
        addConflict(new Conflict<>(my, their));
    }

    protected void fixIncomplete(Way other) {
        Way myWay = (Way) getMergeTarget(other);
        if (myWay == null)
            throw new JosmRuntimeException(tr("Missing merge target for way with id {0}", other.getUniqueId()));
    }

    /**
     * Postprocess the dataset and fix all merged references to point to the actual
     * data.
     */
    public void fixReferences() {
        for (Way w : sourceDataSet.getWays()) {
            if (!conflicts.hasConflictForTheir(w) && objectsWithChildrenToMerge.contains(w.getPrimitiveId())) {
                mergeNodeList(w);
                fixIncomplete(w);
            }
        }
        for (Relation r : sourceDataSet.getRelations()) {
            if (!conflicts.hasConflictForTheir(r) && objectsWithChildrenToMerge.contains(r.getPrimitiveId())) {
                mergeRelationMembers(r);
            }
        }

        deleteMarkedObjects();
    }

    /**
     * Deleted objects in objectsToDelete set and create conflicts for objects that cannot
     * be deleted because they're referenced in the target dataset.
     */
    protected void deleteMarkedObjects() {
        boolean flag;
        do {
            flag = false;
            for (Iterator<OsmPrimitive> it = objectsToDelete.iterator(); it.hasNext();) {
                OsmPrimitive target = it.next();
                OsmPrimitive source = sourceDataSet.getPrimitiveById(target.getPrimitiveId());
                if (source == null)
                    throw new JosmRuntimeException(tr(
                            "Object of type {0} with id {1} was marked to be deleted, but it''s missing in the source dataset",
                            target.getType(), target.getUniqueId()));

                List<OsmPrimitive> referrers = target.getReferrers();
                if (referrers.isEmpty()) {
                    IPrimitive.resetPrimitiveChildren(target);
                    target.mergeFrom(source);
                    target.setDeleted(true);
                    this.deleted.add(target);
                    it.remove();
                    flag = true;
                } else {
                    for (OsmPrimitive referrer : referrers) {
                        // If one of object referrers isn't going to be deleted,
                        // add a conflict and don't delete the object
                        if (!objectsToDelete.contains(referrer)) {
                            addConflict(target, source);
                            it.remove();
                            flag = true;
                            break;
                        }
                    }
                }

            }
        } while (flag);

        if (!objectsToDelete.isEmpty()) {
            // There are some more objects rest in the objectsToDelete set
            // This can be because of cross-referenced relations.
            for (OsmPrimitive osm : objectsToDelete) {
                IPrimitive.resetPrimitiveChildren(osm);
            }
            for (OsmPrimitive osm : objectsToDelete) {
                osm.setDeleted(true);
                osm.mergeFrom(sourceDataSet.getPrimitiveById(osm.getPrimitiveId()));
                this.deleted.add(osm);
            }
        }
    }

    /**
     * Merges the node list of a source way onto its target way.
     *
     * @param source the source way
     * @throws IllegalStateException if no target way can be found for the source way
     * @throws IllegalStateException if there isn't a target node for one of the nodes in the source way
     */
    private void mergeNodeList(Way source) {
        Way target = (Way) getMergeTarget(source);
        if (target == null)
            throw new IllegalStateException(tr("Missing merge target for way with id {0}", source.getUniqueId()));

        List<Node> newNodes = new ArrayList<>(source.getNodesCount());
        for (Node sourceNode : source.getNodes()) {
            Node targetNode = (Node) getMergeTarget(sourceNode);
            if (targetNode != null) {
                newNodes.add(targetNode);
                if (targetNode.isDeleted() && !conflicts.hasConflictForMy(targetNode)) {
                    addConflict(new Conflict<>(targetNode, sourceNode, true));
                    targetNode.setDeleted(false);
                    this.modified.add(targetNode);
                }
            } else
                throw new IllegalStateException(
                        tr("Missing merge target for node with id {0}", sourceNode.getUniqueId()));
        }
        target.setNodes(newNodes);
        this.modified.add(target);
    }

    /**
     * Merges the relation members of a source relation onto the corresponding target relation.
     *
     * @param source the source relation
     * @throws IllegalStateException if there is no corresponding target relation
     * @throws IllegalStateException if there isn't a corresponding target object for one of the relation
     *                               members in source
     */
    private void mergeRelationMembers(Relation source) {
        Relation target = (Relation) getMergeTarget(source);
        if (target == null)
            throw new IllegalStateException(tr("Missing merge target for relation with id {0}", source.getUniqueId()));
        List<RelationMember> newMembers = new LinkedList<>();
        for (RelationMember sourceMember : source.getMembers()) {
            OsmPrimitive targetMember = getMergeTarget(sourceMember.getMember());
            if (targetMember == null)
                throw new IllegalStateException(tr("Missing merge target of type {0} with id {1}",
                        sourceMember.getType(), sourceMember.getUniqueId()));
            newMembers.add(new RelationMember(sourceMember.getRole(), targetMember));
            if (targetMember.isDeleted() && !conflicts.hasConflictForMy(targetMember)) {
                addConflict(new Conflict<>(targetMember, sourceMember.getMember(), true));
                targetMember.setDeleted(false);
            }
            this.modified.add(targetMember);
        }
        target.setMembers(newMembers);
        this.modified.add(target);
    }

    /**
     * Tries to merge a primitive <code>source</code> into an existing primitive with the same id.
     *
     * @param source the source primitive which is to be merged into a target primitive
     * @return true, if this method was able to merge <code>source</code> into a target object; false, otherwise
     */
    private boolean mergeById(OsmPrimitive source) {
        OsmPrimitive target = targetDataSet.getPrimitiveById(source.getId(), source.getType());
        // merge other into an existing primitive with the same id, if possible
        //
        if (target == null)
            return false;
        // found a corresponding target, remember it
        mergedMap.put(source.getPrimitiveId(), target.getPrimitiveId());

        if (target.getVersion() > source.getVersion())
            // target.version > source.version => keep target version
            return true;

        boolean mergeFromSource = false;
        boolean haveSameVersion = target.getVersion() == source.getVersion();

        if (haveSameVersion && !target.isModified() && !source.isModified()
                && target.isVisible() != source.isVisible()) {
            // Same version, but different "visible" attribute and neither of them are modified.
            // It indicates a serious problem in datasets.
            // For example, datasets can be fetched from different OSM servers or badly hand-modified.
            // We shouldn't merge that datasets.
            throw new DataIntegrityProblemException(
                    tr("Conflict in ''visible'' attribute for object of type {0} with id {1}", target.getType(),
                            target.getId()));
        }

        if (!target.isModified() && source.isDeleted()) {
            // target not modified and source is deleted
            // So mark it to be deleted. See #20091
            //
            objectsToDelete.add(target);
        } else if (source.isIncomplete()) {
            // source is incomplete. Nothing to do.
            //
        } else if (target.isIncomplete()) {
            // target is incomplete, source completes it
            // => merge source into target
            //
            mergeFromSource = true;
        } else if (target.isDeleted() && source.isDeleted() && !haveSameVersion) {
            // both deleted. Source is newer. Take source. See #19783
            mergeFromSource = true;
        } else if (target.isDeleted() && !source.isDeleted() && haveSameVersion) {
            // same version, but target is deleted. Assume target takes precedence
            // otherwise too many conflicts when refreshing from the server
            // but, if source is modified, there is a conflict
            if (source.isModified()) {
                addConflict(new Conflict<>(target, source, true));
            }
            // or, if source has a referrer that is not in the target dataset there is a conflict
            // If target dataset refers to the deleted primitive, conflict will be added in fixReferences method
            for (OsmPrimitive referrer : source.getReferrers()) {
                if (targetDataSet.getPrimitiveById(referrer.getPrimitiveId()) == null) {
                    addConflict(new Conflict<>(target, source, true));
                    target.setDeleted(false);
                    break;
                }
            }
        } else if (!target.isModified() && source.isModified()) {
            // target not modified. We can assume that source is the most recent version.
            // clone it into target.
            mergeFromSource = true;
        } else if (!target.isModified() && !source.isModified()) {
            // both not modified. Merge nevertheless, even if versions are the same
            // This helps when updating "empty" relations, see #4295
            mergeFromSource = true;
        } else if (target.isModified() && !source.isModified() && haveSameVersion) {
            // target is same as source but target is modified
            // => keep target and reset modified flag if target and source are semantically equal
            if (target.hasEqualSemanticAttributes(source)) {
                target.setModified(false);
            }
        } else if (source.isDeleted() != target.isDeleted()) {
            // target is modified and deleted state differs.
            // this has to be resolved manually.
            //
            addConflict(target, source);
        } else if (!target.hasEqualSemanticAttributes(source)) {
            // target is modified and is not semantically equal with source. Can't automatically
            // resolve the differences
            // =>  create a conflict
            addConflict(target, source);
        } else {
            // clone from other. mergeFrom will mainly copy
            // technical attributes like timestamp or user information. Semantic
            // attributes should already be equal if we get here.
            //
            mergeFromSource = true;
        }
        if (mergeFromSource) {
            target.mergeFrom(source);
            objectsWithChildrenToMerge.add(source.getPrimitiveId());
        }
        return true;
    }

    @Override
    public String getDescriptionText() {
        return tr("Move data between datasets");
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.sourceDataSet, this.targetDataSet, this.mergeBounds);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            final MergeDataSetsCommand other = (MergeDataSetsCommand) obj;
            return this.sourceDataSet == other.sourceDataSet && this.targetDataSet == other.targetDataSet
                    && this.mergeBounds == other.mergeBounds;
        }
        return false;
    }
}

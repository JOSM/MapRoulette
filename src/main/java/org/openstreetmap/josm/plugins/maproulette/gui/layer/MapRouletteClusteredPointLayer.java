// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui.layer;

import static org.openstreetmap.josm.gui.layer.OsmDataLayer.PROPERTY_HIDE_LABELS_WHILE_DRAGGING;
import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.INode;
import org.openstreetmap.josm.data.osm.IWay;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.QuadBuckets;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.osm.visitor.paint.StyledMapRenderer;
import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.draw.SymbolShape;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.mappaint.mapcss.MapCSSStyleSource;
import org.openstreetmap.josm.gui.mappaint.styleelement.MapImage;
import org.openstreetmap.josm.gui.mappaint.styleelement.Symbol;
import org.openstreetmap.josm.plugins.maproulette.api.MRColors;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.Identifier;
import org.openstreetmap.josm.plugins.maproulette.api.model.Locatable;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api.model.TaskClusteredPoint;
import org.openstreetmap.josm.plugins.maproulette.api_caching.TaskCache;
import org.openstreetmap.josm.plugins.maproulette.gui.ModifiedObjects;
import org.openstreetmap.josm.plugins.maproulette.gui.TaskListPanel;
import org.openstreetmap.josm.plugins.maproulette.io.upload.LateUploadHook;
import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ListenerList;

/**
 * A layer for showing task locations
 */
public class MapRouletteClusteredPointLayer extends Layer implements MouseListener {
    /**
     * The style source, mostly used for preferences
     */
    private static final MapCSSStyleSource STYLE = new MapCSSStyleSource(
            "setting::maproulette{type:color;label:tr(\"MapRoulette task geometry\");default:maproulette#6495ed;}"
                    + "way::maproulette{casing-width:3;casing-color:setting(\"maproulette\");opacity:0.2;}"
                    + "node:tagged::maproulette{symbol-shape:circle;symbol-size:15;symbol-stroke-color:setting(\"maproulette\");"
                    + "symbol-fill-color:setting(\"maproulette\");symbol-fill-opacity:0.5;symbol-stroke-opacity:0.5;}");

    /**
     * The color for locked MR tasks
     */
    private static final NamedColorProperty LOCKED_TASK_COLOR = new NamedColorProperty(
            NamedColorProperty.COLOR_CATEGORY_MAPPAINT, STYLE.url, marktr("MapRoulette task geometry"),
            ColorHelper.html2color("#6495ed"));

    /**
     * The cached image for tasks
     */
    private static final MapImage MR_IMAGE = new MapImage("dialogs/user_no_image", STYLE);

    /**
     * The point bucket
     */
    private final QuadBuckets<TaskClusteredPoint> pointBucket = new QuadBuckets<>();
    /**
     * The id mapping
     */
    private final Set<TaskClusteredPoint> pointMap = new TreeSet<>(Comparator.comparingLong(Identifier::id));
    /**
     * The selected points
     */
    private final Collection<TaskClusteredPoint> selected = new HashSet<>();
    /**
     * The listener for selection updates
     */
    private final ListenerList<Consumer<Collection<TaskClusteredPoint>>> selectionListeners = ListenerList.create();
    /**
     * The listeners for notifying of updated data
     */
    private final ListenerList<Consumer<Map<Long, TaskClusteredPoint>>> updatedDataListeners = ListenerList.create();
    private final Consumer<Map<Long, Task>> taskUpdated;
    /**
     * The bounds of the points
     */
    private final Bounds bounds;

    /**
     * Create a new layer
     *
     * @param bounds The bounds of the layer
     * @param points The points for the layer
     */
    public MapRouletteClusteredPointLayer(Bounds bounds, Collection<TaskClusteredPoint> points) {
        super(tr("MapRoulette Task Layer"));
        this.pointBucket.addAll(points);
        this.bounds = bounds;
        this.taskUpdated = tasks -> {
            final var taskIds = tasks.keySet().stream().mapToLong(Long::longValue).sorted().toArray();
            final Predicate<Locatable> filter = point -> Arrays.binarySearch(taskIds, point.id()) >= 0;
            this.pointBucket.removeIf(filter);
            this.pointMap.removeIf(filter);
            this.refreshTasks(tasks);
            this.updatedDataListeners.fireEvent(listener -> listener.accept(Collections.emptyMap()));
        };
        LateUploadHook.addUploadListener(this.taskUpdated);
        this.pointMap.addAll(points);
    }

    /**
     * Refresh tasks
     *
     * @param tcMap The map of task id to point
     */
    public synchronized void refreshTasks(Map<Long, ? extends TaskClusteredPoint> tcMap) {
        final var current = this.pointMap.stream().collect(Collectors.toMap(Identifier::id, c -> c));
        for (var entry : tcMap.entrySet()) {
            if (current.containsKey(entry.getKey())) {
                final var toRemove = current.get(entry.getKey());
                this.pointBucket.remove(toRemove);
                this.pointMap.remove(toRemove);
            }
            this.pointMap.add(entry.getValue());
            this.pointBucket.add(entry.getValue());
        }
        final Map<Long, TaskClusteredPoint> updateCollection = this.pointBucket.stream()
                .collect(Collectors.toMap(Identifier::id, c -> c));
        this.updatedDataListeners.fireEvent(consumer -> consumer.accept(updateCollection));
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("dialogs", "user_no_image", ImageProvider.ImageSizes.LAYER);
    }

    @Override
    public String getToolTipText() {
        return tr("MapRoulette Task Layer: ");
    }

    @Override
    public void mergeFrom(Layer from) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        v.visit(this.bounds);
    }

    @Override
    public Object getInfoComponent() {
        return null;
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[0];
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bounds) {
        final var box = bounds.toBBox();
        final var painter = new StyledMapRenderer(g, mv, false);
        final var mrColor = LOCKED_TASK_COLOR.get();
        final var mrColorOpacity = new Color(mrColor.getRed(), mrColor.getGreen(), mrColor.getBlue(), 128);

        final var listSelected = MainApplication.getMap().getToggleDialog(TaskListPanel.class).getSelected();

        painter.enableSlowOperations(mv.getMapMover() == null || !mv.getMapMover().movementInProgress()
                || !PROPERTY_HIDE_LABELS_WHILE_DRAGGING.get());
        painter.getSettings(false);

        final var fixedColor = ColorHelper.alphaMultiply(MRColors.statusColor(TaskStatus.FIXED), 0.5f);
        final var skippedColor = ColorHelper.alphaMultiply(MRColors.statusColor(TaskStatus.SKIPPED), 0.5f);
        final var falsePositiveColor = ColorHelper.alphaMultiply(MRColors.statusColor(TaskStatus.FALSE_POSITIVE), 0.5f);
        final var alreadyFixedColor = ColorHelper.alphaMultiply(MRColors.statusColor(TaskStatus.ALREADY_FIXED), 0.5f);
        final var tooHardColor = ColorHelper.alphaMultiply(MRColors.statusColor(TaskStatus.TOO_HARD), 0.5f);
        final var statusSymbol = new Symbol(SymbolShape.SQUARE, 25, null, fixedColor, fixedColor);
        final var disabledStatusSymbol = new Symbol(SymbolShape.SQUARE, 13, null, fixedColor, fixedColor);
        for (var point : this.pointBucket.search(box)) {
            if (ModifiedObjects.getLockedTask(point.id()) == null && !TaskCache.isHidden(point)) {
                final boolean isSelected = this.selected.contains(point) || listSelected.contains(point);
                final var symbolColor = switch (point.status()) {
                case FIXED -> fixedColor;
                case FALSE_POSITIVE -> falsePositiveColor;
                case ALREADY_FIXED -> alreadyFixedColor;
                case SKIPPED -> skippedColor;
                case TOO_HARD -> tooHardColor;
                default -> null;
                };
                final var disabled = switch (point.status()) {
                case FIXED, FALSE_POSITIVE -> true;
                default -> false;
                };
                if (!disabled) {
                    if (symbolColor != null) {
                        painter.drawNodeSymbol(point.location(), statusSymbol, symbolColor, symbolColor);
                    }
                    painter.drawNodeIcon(point.location(), MR_IMAGE, false, isSelected, false, 0);
                } else {
                    // Yes, we want to switch the draw order
                    painter.drawNodeIcon(point.location(), MR_IMAGE, true, isSelected, false, 0);
                    if (symbolColor != null) {
                        painter.drawNodeSymbol(point.location(), disabledStatusSymbol, symbolColor, symbolColor);
                    }
                }
            }
        }

        final var symbol = new Symbol(SymbolShape.CIRCLE, 10, null, mrColorOpacity, mrColorOpacity);
        final var stroke = new BasicStroke(8f);
        for (var task : ModifiedObjects.getLockedTasks()) {
            for (INode n : task.geometries().searchNodes(box)) {
                if (n.isTagged() || !n.isReferredByWays(1)) {
                    painter.drawNodeSymbol(n, symbol, mrColorOpacity, mrColorOpacity);
                }
            }
            for (IWay<?> w : task.geometries().searchWays(box)) {
                if (w.isTagged()) {
                    if (w.isClosed()) {
                        painter.drawArea(w, mrColorOpacity, null, 12.5f, 0.5f, false);
                    } else {
                        painter.drawWay(w, mrColorOpacity, stroke, stroke, null, 0, false, false, false, false);
                    }
                }
            }
            for (Relation r : task.geometries().searchRelations(box)) {
                if (r.isTagged() && r.isMultipolygon()) {
                    painter.drawArea(r, mrColorOpacity, null, null, null, false);
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        final var mapView = MainApplication.getMap().mapView;
        final var clickLocation = mapView.getLatLon(e.getPoint().x, e.getPoint().y);
        final var bbox = new BBox(clickLocation);
        final ArrayList<TaskClusteredPoint> add = e.isShiftDown() ? new ArrayList<>(this.selected) : new ArrayList<>();
        add.addAll(this.pointBucket.search(bbox));
        add.removeIf(TaskCache::isHidden);
        if (!add.isEmpty() && !this.selected.containsAll(add) && !add.containsAll(this.selected)) {
            this.setSelected(add);
            return;
        }
        bbox.addLatLon(clickLocation, 0.000_000_8 * mapView.getDist100Pixel());
        add.addAll(this.pointBucket.search(bbox));
        add.removeIf(TaskCache::isHidden);
        if (add.isEmpty()) {
            final var tNode = new Node(clickLocation);
            for (var task : ModifiedObjects.getLockedTasks()) {
                if (!TaskCache.isHidden(task)) {
                    final var minDistance = task.geometries().searchPrimitives(bbox).stream()
                            .mapToDouble(prim -> Geometry.getDistance(tNode, prim)).min().orElse(Double.NaN);
                    if (!Double.isNaN(minDistance) && minDistance < mapView.getDist100Pixel() / 10) {
                        this.pointBucket.stream().filter(p -> p.id() == task.id()).findFirst().ifPresent(add::add);
                    }
                }
            }
        }
        this.setSelected(add);
        this.invalidate();
    }

    /**
     * Set points as selected
     *
     * @param points The points to set as selected
     */
    private void setSelected(Collection<TaskClusteredPoint> points) {
        final var lastSelected = new HashSet<>(this.selected);
        this.selected.clear();
        for (var p : points) {
            if (this.pointBucket.contains(p)) {
                this.selected.add(p);
            }
        }
        // Only fire listeners if the selection actually changed
        if (!this.selected.equals(lastSelected)) {
            this.selectionListeners.fireEvent(listener -> listener.accept(points));
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Do nothing
    }

    @Override
    public synchronized void destroy() {
        super.destroy();
        this.selectionListeners.fireEvent(listener -> listener.accept(Collections.emptyList()));
        MainApplication.getMap().mapView.removeMouseListener(this);
        LateUploadHook.removeUploadListener(this.taskUpdated);
    }

    /**
     * Add a listener for data updates
     *
     * @param updateConsumer The consumer for data updates
     */
    public void addListener(Consumer<Map<Long, TaskClusteredPoint>> updateConsumer) {
        this.updatedDataListeners.addListener(updateConsumer);
    }

    /**
     * Remove a listener for data updates
     *
     * @param updateConsumer The consumer to remove for data updates
     */
    public void removeListener(Consumer<Map<Long, TaskClusteredPoint>> updateConsumer) {
        this.updatedDataListeners.removeListener(updateConsumer);
    }

    /**
     * Add a selection listener
     *
     * @param listener The listener for updated selection events
     */
    public void addSelectionListener(Consumer<Collection<TaskClusteredPoint>> listener) {
        this.selectionListeners.addListener(listener);
        listener.accept(Collections.unmodifiableCollection(this.selected));
    }

    /**
     * Get the tasks from this layer
     *
     * @return The tasks for this layer
     */
    public Collection<TaskClusteredPoint> getTasks() {
        return Collections.unmodifiableCollection(this.pointBucket);
    }

    /**
     * Remove a selection listener
     *
     * @param listener The listener for updated selection events
     */
    public void removeSelectionListener(Consumer<Collection<TaskClusteredPoint>> listener) {
        this.selectionListeners.removeListener(listener);
    }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui.task.current;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.Option;

import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmDataManager;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.VerticallyScrollablePanel;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.data.TaskPrimitives;
import org.openstreetmap.josm.plugins.maproulette.gui.MRGuiHelper;
import org.openstreetmap.josm.plugins.maproulette.gui.ModifiedObjects;
import org.openstreetmap.josm.plugins.maproulette.gui.ModifiedTask;
import org.openstreetmap.josm.plugins.maproulette.gui.TagChangeTable;
import org.openstreetmap.josm.plugins.maproulette.gui.preferences.MapRoulettePreferences;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.Utils;

/**
 * A panel for currently locked tasks
 */
public final class CurrentTaskPanel extends ToggleDialog {
    /**
     * The serial UID for this panel
     */
    @Serial
    private static final long serialVersionUID = 6628674700523633284L;
    /**
     * The model for the task
     */
    private final VerticallyScrollablePanel panel = new VerticallyScrollablePanel(new GridBagLayout());
    /**
     * The label for the task id
     */
    private final JLabel idLabel = new JLabel();
    /**
     * The panel for instructions
     */
    private final HtmlPanel instructionPane = new HtmlPanel();
    /**
     * The panel for cooperative work
     */
    private final JPanel cooperativeWork = new JPanel();
    /**
     * The actions for the task
     */
    private final InnerAction[] actions;
    /**
     * The current task
     */
    private Task task;

    /**
     * Create a new task panel
     */
    public CurrentTaskPanel() {
        super(tr("Current MapRoulette Task"), "user_no_image", tr("This is the current MapRoulette Task"),
                Shortcut.registerShortcut("maproulette:task", tr("MapRoulette: Current task"), KeyEvent.CHAR_UNDEFINED,
                        Shortcut.NONE),
                200, false, MapRoulettePreferences.class, false);

        final Supplier<Task> supplier = () -> this.task;
        final Supplier<HTMLDocument> docSupplier = () -> (HTMLDocument) this.instructionPane.getEditorPane()
                .getDocument();
        final var newActions = new InnerAction[] {
                new TaskStatusAction(TaskStatus.FALSE_POSITIVE, supplier, docSupplier),
                new TaskStatusAction(TaskStatus.TOO_HARD, supplier, docSupplier),
                new TaskStatusAction(TaskStatus.FIXED, supplier, docSupplier),
                new TaskStatusAction(TaskStatus.ALREADY_FIXED, supplier, docSupplier),
                new TaskStatusAction(TaskStatus.SKIPPED, supplier, docSupplier), new SelectOsmPrimitives(supplier) };

        final var sideButtons = new ArrayList<SideButton>();
        sideButtons.add(new SideButton(newActions[0])); // False positive
        sideButtons.add(new SideButton(newActions[2])); // Fixed
        sideButtons.add(new SideButton(newActions[4])); // Skipped
        sideButtons.add(new SideButton(newActions[5])); // Additional actions
        sideButtons.get(0).createArrow(l -> showPopupMenu(sideButtons.get(0), newActions[0], newActions[1]));
        sideButtons.get(1).createArrow(l -> showPopupMenu(sideButtons.get(1), newActions[2], newActions[3]));
        this.actions = newActions;
        this.instructionPane.enableClickableHyperlinks();
        final var gbc = GBC.eol();
        this.panel.add(this.idLabel, gbc);
        this.panel.add(new JLabel(tr("Instructions: ")), gbc);
        gbc.fill(GridBagConstraints.BOTH);
        this.panel.add(this.instructionPane, gbc);
        this.panel.add(this.cooperativeWork, gbc);
        super.createLayout(this.panel.getVerticalScrollPane(), false, sideButtons);
    }

    private static void showPopupMenu(Component parent, Object... menuItems) {
        final var menu = new JPopupMenu();
        final var box = parent.getBounds();
        for (var item : menuItems) {
            if (item instanceof Action action) {
                menu.add(action);
            } else {
                throw new IllegalArgumentException("We don't currently support " + item.getClass());
            }
        }
        menu.show(parent, 0, box.y + box.height);
    }

    /**
     * Update the internal model
     *
     * @param task The task to show
     */
    public void refreshModel(final Task task) {
        if (this.task != null) {
            final var selectedResponses = getSelections(
                    (HTMLDocument) this.instructionPane.getEditorPane().getDocument());
            if (!selectedResponses.isEmpty()) {
                final var modifiedTask = ModifiedObjects.getModifiedTask(this.task.id());
                final ModifiedTask newModifiedTask;
                if (modifiedTask == null) {
                    newModifiedTask = new ModifiedTask(this.task, this.task.status(), null, null, null,
                            selectedResponses);
                } else {
                    newModifiedTask = new ModifiedTask(modifiedTask.task(), modifiedTask.status(),
                            modifiedTask.comment(), modifiedTask.tags(), modifiedTask.reviewRequested(),
                            selectedResponses);
                    ModifiedObjects.removeModifiedTask(modifiedTask);
                }
                ModifiedObjects.addModifiedTask(newModifiedTask);
            }
        }
        this.task = task;
        if ((task == null && this.isVisible()
                && Config.getPref().getBoolean("maproulette.current_task_panel.autohide", false))
                || (task != null && !this.isVisible())) {
            this.toggleAction.actionPerformed(null);
        }
        this.refreshPanel();
    }

    /**
     * Get the actions for the panel
     * @return The actions
     */
    InnerAction[] actions() {
        return actions;
    }

    private void refreshPanel() {
        final var currentTask = this.task;
        this.panel.setBackground(UIManager.getColor("Panel.background"));
        for (var action : actions) {
            action.updateEnabledState();
        }
        if (currentTask == null) {
            this.idLabel.setText(null);
            this.instructionPane.setText(tr("Please select a locked task"));
            this.cooperativeWork.setVisible(false);
            return;
        }
        this.idLabel.setText(tr("ID: {0}", currentTask.id()));

        this.instructionPane.setText(MRGuiHelper.getInstructionText(currentTask));
        updateSelections((HTMLDocument) this.instructionPane.getEditorPane().getDocument(), this.task);
        if (currentTask.isCooperativeWorkOsmChange()) {
            final var cooperativePanel = this.cooperativeWork;
            cooperativePanel.removeAll();
            cooperativePanel.setVisible(true);
            cooperativePanel.setLayout(new GridBagLayout());
            final var taskCooperativeWork = Objects.requireNonNull(currentTask.cooperativeWorkAsOsmChange());
            if (taskCooperativeWork.creates() != null && taskCooperativeWork.creates().length > 0) {
                cooperativePanel.add(new JLabel(tr("Creates")), GBC.eol());
                throw new IllegalArgumentException("Haven't figured out what to do with creates");
            }
            if (taskCooperativeWork.updates() != null && taskCooperativeWork.updates().length > 0
                    && OsmDataManager.getInstance().getEditDataSet() != null) {
                cooperativePanel.add(new JLabel(tr("Tag Updates")), GBC.eol());
                final var table = new TagChangeTable();
                cooperativePanel.add(table.getTableHeader(), GBC.eol().fill(GridBagConstraints.HORIZONTAL));
                cooperativePanel.add(table, GBC.eol().fill(GridBagConstraints.HORIZONTAL));
                for (var updates : taskCooperativeWork.updates()) {
                    var row = 0;
                    final var current = OsmDataManager.getInstance().getEditDataSet().getPrimitiveById(updates.osmId(),
                            updates.osmType());
                    for (var change : updates.tags().updates().entrySet()) {
                        final var old = current.get(change.getKey());
                        table.setValueAt(change.getKey(), row, 0);
                        table.setValueAt(old, row, 1);
                        table.setValueAt(change.getValue(), row++, 2);
                    }
                    for (var change : updates.tags().deletes()) {
                        table.setValueAt(change, row, 0);
                        table.setValueAt(current.get(change), row++, 1);
                    }
                }
            } else {
                this.cooperativeWork.setVisible(false);
            }
        }
    }

    /**
     * Update the selections in a doc from the stored selections
     * @param doc The doc to update
     * @param task The task to get the selections for
     */
    private static void updateSelections(HTMLDocument doc, Task task) {
        final var modifiedTask = ModifiedObjects.getModifiedTask(task.id());
        if (modifiedTask != null && modifiedTask.completionResponses() != null) {
            final var selectIterator = doc.getIterator(HTML.Tag.SELECT);
            final var selectListener = new SelectComboBoxListener(doc, task);
            while (selectIterator.isValid()) {
                final var attribs = selectIterator.getAttributes();
                if (attribs.getAttribute(HTML.Attribute.NAME) != null) {
                    final var name = (String) attribs.getAttribute(HTML.Attribute.NAME);
                    if (modifiedTask.completionResponses().get(name) != null && attribs
                            .getAttribute(StyleConstants.ModelAttribute)instanceof DefaultComboBoxModel<?> listModel) {
                        listModel.addListDataListener(selectListener);
                        final var expectedOption = modifiedTask.completionResponses().get(name);
                        for (var i = 0; i < listModel.getSize(); i++) {
                            final var currentOption = (Option) listModel.getElementAt(i);
                            if (Objects.equals(currentOption.getValue(), expectedOption.getValue())) {
                                listModel.setSelectedItem(currentOption);
                                break;
                            }
                        }
                    }
                }
                selectIterator.next();
            }
        }
    }

    /**
     * Get the selections from the comboboxes of a document
     * @param doc The document to parse
     * @return The selected options
     */
    public static Map<String, Option> getSelections(HTMLDocument doc) {
        final var selectionMap = new TreeMap<String, Option>();
        final var selectIterator = doc.getIterator(HTML.Tag.SELECT);
        while (selectIterator.isValid()) {
            final var attribs = selectIterator.getAttributes();
            if (attribs.getAttribute(HTML.Attribute.NAME) != null) {
                final var name = (String) attribs.getAttribute(HTML.Attribute.NAME);
                if (attribs.getAttribute(StyleConstants.ModelAttribute)instanceof DefaultComboBoxModel<?> listModel) {
                    final var option = (Option) listModel.getSelectedItem();
                    if (!Utils.isStripEmpty(option.getValue())) {
                        selectionMap.put(name, option);
                    }
                }
            }
            selectIterator.next();
        }
        return Collections.unmodifiableMap(selectionMap);
    }

    /**
     * A listener for select combo boxes from a {@link HTMLDocument}
     * @param doc The document to use to update a task from
     * @param task The originating task
     */
    private record SelectComboBoxListener(HTMLDocument doc, Task task) implements ListDataListener {

    @Override
    public void intervalAdded(ListDataEvent e) {
        updateModifiedTask();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        updateModifiedTask();
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        updateModifiedTask();
    }

    private void updateModifiedTask() {
        final var originalModifiedTask = ModifiedObjects.getModifiedTask(this.task.id());
        if (originalModifiedTask != null) {
            final var modifiedTask = new ModifiedTask(originalModifiedTask.task(), originalModifiedTask.status(),
                    originalModifiedTask.comment(), originalModifiedTask.tags(), originalModifiedTask.reviewRequested(),
                    getSelections(doc));
            ModifiedObjects.removeModifiedTask(originalModifiedTask);
            ModifiedObjects.addModifiedTask(modifiedTask);
        }
    }
}

/**
 * An action purely for making it easier to call {@link JosmAction#updateEnabledState()}.
 */
abstract static class InnerAction extends JosmAction {
    /**
     * Serial UID for this action
     */
    @Serial
    private static final long serialVersionUID = -6428448152026023173L;

    /**
     * Constructs a new {@code JosmAction} and installs layer changed and selection changed adapters.
     * <br>
     * Use this super constructor to setup your action.
     *
     * @param name              the action's text as displayed on the menu (if it is added to a menu)
     * @param iconName          the filename of the icon to use
     * @param tooltip           a longer description of the action that will be displayed in the tooltip. Please note
     *                          that html is not supported for menu actions on some platforms.
     * @param shortcut          a ready-created shortcut object or null if you don't want a shortcut. But you always
     *                          do want a shortcut, remember you can always register it with group=none, so you
     *                          won't be assigned a shortcut unless the user configures one. If you pass null here,
     *                          the user CANNOT configure a shortcut for your action.
     * @param registerInToolbar register this action for the toolbar preferences?
     */
    protected InnerAction(String name, String iconName, String tooltip, Shortcut shortcut, boolean registerInToolbar) {
        super(name, iconName, tooltip, shortcut, registerInToolbar, true);
    }

    @Override
    public void updateEnabledState() {
        super.updateEnabledState();
    }
}

/**
 * Select the task primitives
 */
private static class SelectOsmPrimitives extends InnerAction {
    /**
     * The serial UID for this action
     */
    @Serial
    private static final long serialVersionUID = -5705885041487335379L;
    private final Supplier<Task> taskSuppler;

    SelectOsmPrimitives(Supplier<Task> taskSupplier) {
        super(tr("Select Primitives"), "dialogs/select", tr("Select the OSM primitives for this task"),
                Shortcut.registerShortcut("maproulette:select_task_primitives",
                        tr("MapRoulette: Select task primitives"), KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
                false);
        Objects.requireNonNull(taskSupplier);
        this.taskSuppler = taskSupplier;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final var task = this.taskSuppler.get();
        if (task != null) {
            final var primitives = TaskPrimitives.getPrimitiveIds(task);
            if (!primitives.isEmpty()) {
                OsmDataManager.getInstance().getEditDataSet().setSelected(primitives);
                AutoScaleAction.autoScale(AutoScaleAction.AutoScaleMode.SELECTION);
            }
        }
    }

    @Override
    public void updateEnabledState() {
        if (this.taskSuppler != null) { // This check is only needed for the constructor. Watch JEP draft 8300786.
            final var task = this.taskSuppler.get();
            this.setEnabled(!Optional.ofNullable(task).map(TaskPrimitives::getPrimitiveIds)
                    .orElse(Collections.emptyList()).isEmpty());
        }
    }
}}

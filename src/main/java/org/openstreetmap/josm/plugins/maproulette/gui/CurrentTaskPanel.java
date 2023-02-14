// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.OsmDataManager;
import org.openstreetmap.josm.gui.ConditionalOptionPaneUtil;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.VerticallyScrollablePanel;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api_caching.TaskCache;
import org.openstreetmap.josm.plugins.maproulette.data.ApplyCooperativeChange;
import org.openstreetmap.josm.plugins.maproulette.data.ApplyOscChange;
import org.openstreetmap.josm.plugins.maproulette.data.TaskPrimitives;
import org.openstreetmap.josm.plugins.maproulette.gui.preferences.MapRoulettePreferences;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Shortcut;

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
        final var newActions = new InnerAction[] { new TaskStatusAction(TaskStatus.FALSE_POSITIVE, supplier),
                new TaskStatusAction(TaskStatus.TOO_HARD, supplier), new TaskStatusAction(TaskStatus.FIXED, supplier),
                new TaskStatusAction(TaskStatus.ALREADY_FIXED, supplier),
                new TaskStatusAction(TaskStatus.SKIPPED, supplier), new SelectOsmPrimitives(supplier) };

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
        gbc.fill(GBC.BOTH);
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
        this.task = task;
        if ((task == null && this.isVisible()
                && Config.getPref().getBoolean("maproulette.current_task_panel.autohide", false))
                || (task != null && !this.isVisible())) {
            this.toggleAction.actionPerformed(null);
        }
        this.refreshPanel();
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
                cooperativePanel.add(table.getTableHeader(), GBC.eol().fill(GBC.HORIZONTAL));
                cooperativePanel.add(table, GBC.eol().fill(GBC.HORIZONTAL));
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
     * An action purely for making it easier to call {@link JosmAction#updateEnabledState()}.
     */
    private abstract static class InnerAction extends JosmAction {
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
        protected InnerAction(String name, String iconName, String tooltip, Shortcut shortcut,
                boolean registerInToolbar) {
            super(name, iconName, tooltip, shortcut, registerInToolbar, true);
        }

        @Override
        public void updateEnabledState() {
            super.updateEnabledState();
        }
    }

    /**
     * Update the status for a task
     */
    private static class TaskStatusAction extends InnerAction {
        /**
         * The serial UID for this action
         */
        @Serial
        private static final long serialVersionUID = -7143294590412539737L;
        /**
         * The status for this task
         */
        private final TaskStatus status;
        /**
         * The supplier that will give us the current task
         */
        private final Supplier<Task> currentTaskProvider;

        /**
         * Create a new task status action
         *
         * @param status              The status to use
         * @param currentTaskProvider The current task provider
         */
        TaskStatusAction(TaskStatus status, Supplier<Task> currentTaskProvider) {
            super(status.description(), getIconName(status), status.description(),
                    Shortcut.registerShortcut("maproulette:" + status.name().toLowerCase(Locale.ENGLISH),
                            tr("MapRoulette: Mark Task as {0}", status.description()), KeyEvent.CHAR_UNDEFINED,
                            Shortcut.NONE),
                    false);
            this.status = status;
            this.currentTaskProvider = currentTaskProvider;
        }

        /**
         * Get the icon name for a status type
         *
         * @param status The status type
         * @return The icon name
         */
        private static String getIconName(TaskStatus status) {
            return switch (status) {
            case FIXED, ALREADY_FIXED -> "dialogs/validator";
            case TOO_HARD, FALSE_POSITIVE -> "cancel";
            case SKIPPED -> "svpRight";
            default -> "presets/misc/no_icon";
            };
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final var selected = new ArrayList<>(
                    MainApplication.getMap().getToggleDialog(TaskListPanel.class).getSelected());
            final var task = this.currentTaskProvider.get();
            if (task != null) {
                selected.removeIf(p -> p.id() == task.id());
            }
            handleTask(task);
            if (!selected.isEmpty()) {
                final var markAll = ConditionalOptionPaneUtil.showConfirmationDialog("TaskStatusAction.bulkUpdate",
                        MainApplication.getMainFrame(),
                        tr("<html>Mark all selected tasks?<br>This will apply all tag fixes and geometry fixes!</html>"),
                        tr("Mark all selected tasks?"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
                        JOptionPane.YES_OPTION);
                if (markAll) {
                    for (var toMark : selected) {
                        if (toMark instanceof Task t) {
                            handleTask(t);
                        } else {
                            handleTask(TaskCache.get(toMark.id()));
                        }
                    }
                }
            }

            // Use the first enabled action (not this, in otherwords)
            SideButton button = null;
            if (e.getSource()instanceof JMenuItem menuItem && menuItem.getParent()instanceof JPopupMenu menu
                    && menu.getInvoker()instanceof SideButton sideButton) {
                if (sideButton.getAction() == this) {
                    final var newAction = Stream.of(menu.getSubElements()).filter(JMenuItem.class::isInstance)
                            .map(item -> ((JMenuItem) item).getAction()).filter(Action::isEnabled)
                            .filter(action -> action != this).findFirst().orElse(this);
                    sideButton.setAction(newAction);
                }
                button = sideButton;
                sideButton.setAction(this);
            } else if (e.getSource()instanceof SideButton sideButton) {
                button = sideButton;
            }
            if (button != null && button.getParent().getParent().getParent()instanceof CurrentTaskPanel panel) {
                for (var action : panel.actions) {
                    action.updateEnabledState();
                }
            }
        }

        private void handleTask(Task task) {
            // TODO put extended dialog here, ask for comment/tags/null -- don't forget to use the bulk operation methods
            if (task != null) {
                ModifiedObjects.addModifiedTask(new ModifiedTask(task, this.status, null, null, null));
            }
            if (task != null && task.isCooperativeWorkOsmChange() && this.status == TaskStatus.FIXED) {
                final var command = new ApplyCooperativeChange(
                        Objects.requireNonNull(task.cooperativeWorkAsOsmChange()))
                                .generateCommand(OsmDataManager.getInstance().getEditDataSet());
                if (command != null) {
                    command.executeCommand();
                    if (!command.getParticipatingPrimitives().isEmpty()) {
                        UndoRedoHandler.getInstance().add(command, false);
                    }
                }
            } else if (task != null && task.isCooperativeWorkOsc() && this.status == TaskStatus.FIXED) {
                UndoRedoHandler.getInstance().add(new ApplyOscChange(OsmDataManager.getInstance().getEditDataSet(),
                        Objects.requireNonNull(task.cooperativeWorkAsOsc()).a));
            }
            Optional.ofNullable(MainApplication.getMap().getToggleDialog(CurrentTaskPanel.class))
                    .ifPresent(p -> p.refreshModel(task));
        }

        @Override
        public void updateEnabledState() {
            if (this.currentTaskProvider != null) {
                final var task = this.currentTaskProvider.get();
                if (task != null) {
                    // Disable buttons when the state has been modified
                    final var state = Optional.ofNullable(ModifiedObjects.getModifiedTask(task.id()))
                            .map(ModifiedTask::status).orElse(task.status());
                    if (task.cooperativeWork() == null) {
                        this.putValue(NAME, this.status.description());
                        this.setTooltip(this.status.description());
                    } else if (task.isCooperativeWorkOsmChange()) {
                        switch (this.status) {
                        case FALSE_POSITIVE -> {
                            this.putValue(NAME, tr("No"));
                            this.setTooltip(tr("No: the suggested tags are wrong"));
                        }
                        case FIXED -> {
                            this.putValue(NAME, tr("Yes"));
                            this.setTooltip(tr("Yes: the suggested tags are correct"));
                        }
                        default -> {
                            this.putValue(NAME, this.status.description());
                            this.setTooltip(this.status.description());
                        }
                        }
                    } else if (task.isCooperativeWorkOsc()) {
                        if (Objects.requireNonNull(this.status) == TaskStatus.FIXED) {
                            this.putValue(NAME, tr("Apply OSC"));
                            this.setTooltip(tr("Apply the suggested change from the task"));
                        } else {
                            this.putValue(NAME, this.status.description());
                            this.setTooltip(this.status.description());
                        }
                    }
                    this.setEnabled(status != state);
                    return;
                }
            }
            this.setEnabled(false);
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
    }
}

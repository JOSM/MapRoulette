// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui.task.current;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.text.html.HTMLDocument;

import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.OsmDataManager;
import org.openstreetmap.josm.gui.ConditionalOptionPaneUtil;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompComboBox;
import org.openstreetmap.josm.gui.widgets.JosmTextArea;
import org.openstreetmap.josm.gui.widgets.QuadStateCheckBox;
import org.openstreetmap.josm.plugins.maproulette.api.enums.TaskStatus;
import org.openstreetmap.josm.plugins.maproulette.api.model.Task;
import org.openstreetmap.josm.plugins.maproulette.api_caching.ChallengeCache;
import org.openstreetmap.josm.plugins.maproulette.api_caching.TaskCache;
import org.openstreetmap.josm.plugins.maproulette.data.ApplyCooperativeChange;
import org.openstreetmap.josm.plugins.maproulette.data.ApplyOscChange;
import org.openstreetmap.josm.plugins.maproulette.gui.ModifiedObjects;
import org.openstreetmap.josm.plugins.maproulette.gui.ModifiedTask;
import org.openstreetmap.josm.plugins.maproulette.gui.task.list.TaskListPanel;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Update the status for a task
 */
class TaskStatusAction extends CurrentTaskPanel.InnerAction {
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
    private final Supplier<HTMLDocument> currentDocumentProvider;

    /**
     * Create a new task status action
     *
     * @param status              The status to use
     * @param currentTaskProvider The current task provider
     * @param currentDocumentProvider The current document provider -- used to generate completion responses from select tags
     */
    TaskStatusAction(TaskStatus status, Supplier<Task> currentTaskProvider,
            Supplier<HTMLDocument> currentDocumentProvider) {
        super(status.description(), getIconName(status), status.description(),
                Shortcut.registerShortcut("maproulette:" + status.name().toLowerCase(Locale.ENGLISH),
                        tr("MapRoulette: Mark Task as {0}", status.description()), KeyEvent.CHAR_UNDEFINED,
                        Shortcut.NONE),
                false);
        this.status = status;
        this.currentTaskProvider = currentTaskProvider;
        this.currentDocumentProvider = currentDocumentProvider;
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
                ConditionalOptionPaneUtil.startBulkOperation("maproulette.task.feedback");
                try {
                    for (var toMark : selected) {
                        if (toMark instanceof Task t) {
                            handleTask(t);
                        } else {
                            handleTask(TaskCache.get(toMark.id()));
                        }
                    }
                } finally {
                    ConditionalOptionPaneUtil.endBulkOperation("maproulette.task.feedback");
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
            for (var action : panel.actions()) {
                action.updateEnabledState();
            }
        }
    }

    private void handleTask(Task task) {
        if (task != null) {
            final var modifiedTask = getModifiedTask(task);
            if (modifiedTask == null) {
                return;
            }
            ModifiedObjects.addModifiedTask(modifiedTask);
            if (task.isCooperativeWorkOsmChange() && this.status == TaskStatus.FIXED) {
                final var command = new ApplyCooperativeChange(
                        Objects.requireNonNull(task.cooperativeWorkAsOsmChange()))
                                .generateCommand(OsmDataManager.getInstance().getEditDataSet());
                if (command != null) {
                    command.executeCommand();
                    if (!command.getParticipatingPrimitives().isEmpty()) {
                        UndoRedoHandler.getInstance().add(command, false);
                    }
                }
            } else if (task.isCooperativeWorkOsc() && this.status == TaskStatus.FIXED) {
                final var message = tr("Apply OSC directly to the edit layer?");
                final var options = new String[] { tr("Apply"), tr("Show"), tr("Cancel") };
                final var option = ConditionalOptionPaneUtil.showOptionDialog("maproulette.task.apply_osc",
                        MainApplication.getMainFrame(), message, message, JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.YES_OPTION, options, options[0]);
                final var osc = Objects.requireNonNull(task.cooperativeWorkAsOsc());
                if (option == 0) {
                    UndoRedoHandler.getInstance()
                            .add(new ApplyOscChange(OsmDataManager.getInstance().getEditDataSet(), osc.a));
                } else if (option == 1) {
                    final var layer = new OsmDataLayer(osc.a, task.name(), null);
                    MainApplication.getLayerManager().addLayer(layer);
                } else {
                    ModifiedObjects.removeModifiedTask(modifiedTask);
                    return;
                }
            }
        }
        Optional.ofNullable(MainApplication.getMap().getToggleDialog(CurrentTaskPanel.class))
                .ifPresent(p -> p.refreshModel(task));
    }

    private ModifiedTask getModifiedTask(Task task) {
        final var panel = new JPanel(new GridBagLayout());
        final var comment = new JosmTextArea();
        final var challenge = ChallengeCache.challenge(task.parentId());
        final var tagBox = new AutoCompComboBox<String>();
        final var reviewRequested = new QuadStateCheckBox(tr("Request Review"), QuadStateCheckBox.State.UNSET,
                QuadStateCheckBox.State.SELECTED, QuadStateCheckBox.State.UNSET, QuadStateCheckBox.State.NOT_SELECTED);
        comment.setRows(4);
        comment.setColumns(25);
        if (challenge.extra().preferredTags() != null) {
            tagBox.getModel().addAllElements(Arrays.asList(challenge.extra().preferredTags().split(",")));
        }
        tagBox.setEditable(!challenge.extra().limitTags());
        panel.add(comment, GBC.eol().fill(GBC.BOTH));
        panel.add(tagBox, GBC.eol().fill(GBC.HORIZONTAL));
        panel.add(reviewRequested, GBC.eol().anchor(GBC.LINE_START));

        var selection = ConditionalOptionPaneUtil.showOptionDialog("maproulette.task.feedback",
                MainApplication.getMainFrame(), panel, tr("Task Feedback"), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null);
        if (selection == JOptionPane.OK_OPTION) {
            return new ModifiedTask(task, this.status, comment.getText().trim().isEmpty() ? null : comment.getText(),
                    tagBox.getText().trim().isEmpty() ? null : tagBox.getText(), null,
                    CurrentTaskPanel.getSelections(this.currentDocumentProvider.get()));
        }
        return null;
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

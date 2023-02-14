// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.gui;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openstreetmap.josm.plugins.maproulette.api.model.Task;

/**
 * A class holding modified objects
 */
public final class ModifiedObjects {
    /**
     * The current task list (these should all be locked tasks)
     */
    private static final Map<Long, Task> LOCKED_TASK_MAP = new TreeMap<>();
    /**
     * The modified task list
     */
    private static final Map<Long, ModifiedTask> MODIFIED_TASK_MAP = new TreeMap<>();

    /**
     * Don't allow this static class to be instantiated
     */
    private ModifiedObjects() {
        // Hide constructor
    }

    /**
     * Add a modified task
     *
     * @param task The modified task
     * @return {@code true} if the task list changed
     */
    public static boolean addLockedTask(Task task) {
        return LOCKED_TASK_MAP.put(task.id(), task) == null;
    }

    /**
     * Remove a modified task
     *
     * @param task The modified task
     * @return {@code true} if the task list changed
     */
    public static boolean removeLockedTask(Task task) {
        return LOCKED_TASK_MAP.remove(task.id()) != null;
    }

    /**
     * Get the current task list
     *
     * @return The task list
     */
    @Nonnull
    public static List<Task> getLockedTasks() {
        return List.copyOf(LOCKED_TASK_MAP.values());
    }

    /**
     * Get a specific task
     *
     * @param id The task to get
     * @return the task, or {@code null}
     */
    @Nullable
    public static Task getLockedTask(long id) {
        return LOCKED_TASK_MAP.get(id);
    }

    /**
     * Get the modified tasks
     *
     * @return An unmodifiable list of modified tasks
     */
    @Nonnull
    public static List<ModifiedTask> getModifiedTasks() {
        return List.copyOf(MODIFIED_TASK_MAP.values());
    }

    /**
     * Add a modified task
     *
     * @param task The task to add
     * @return {@code true} if the task was added
     */
    public static boolean addModifiedTask(ModifiedTask task) {
        return MODIFIED_TASK_MAP.put(task.task().id(), task) == null;
    }

    /**
     * Remove a modified task
     *
     * @param task The task to remove
     * @return {@code true} if the task was removed
     */
    public static boolean removeModifiedTask(ModifiedTask task) {
        return MODIFIED_TASK_MAP.remove(task.task().id()) != null;
    }

    /**
     * Get the task modification for a specified task id
     *
     * @param id The id to get the task modification for
     * @return The modified task, or {@code null}
     */
    @Nullable
    public static ModifiedTask getModifiedTask(long id) {
        return MODIFIED_TASK_MAP.get(id);
    }
}

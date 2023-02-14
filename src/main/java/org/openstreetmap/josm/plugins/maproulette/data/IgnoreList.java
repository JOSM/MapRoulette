// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.data;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.LongStream;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 * Lists of ignored objects
 * FIXME: Surface this in the UI somewhere
 */
public final class IgnoreList {
    /**
     * The ignored tasks
     */
    private static long[] ignoredTasks = convertList(Config.getPref().getList("mapillary.ignore.tasks"));
    /**
     * The ignored challenges
     */
    private static long[] ignoredChallenges = convertList(Config.getPref().getList("mapillary.ignore.challenges"));
    /**
     * {@code true} if a save is required
     */
    private static boolean save;

    /**
     * The default hidden constructor
     */
    private IgnoreList() {
        // Hide constructor
    }

    /**
     * Convert a list of ids to a long[]
     *
     * @param ids The ids to convert
     * @return the array to use
     */
    private static long[] convertList(List<String> ids) {
        final var pattern = Pattern.compile("^\\d\\+$").asMatchPredicate();
        return ids.stream().filter(pattern).mapToLong(Long::parseLong).toArray();
    }

    /**
     * Ignore a task
     *
     * @param id The task to ignore
     */
    public static synchronized void ignoreTask(long id) {
        final var location = Arrays.binarySearch(ignoredTasks, id);
        if (location < 0) {
            ignoredTasks = insertIntoArray(ignoredTasks, id, convertToInsertionPoint(location));
            save = true;
            MainApplication.worker.execute(IgnoreList::save);
        }
    }

    /**
     * Insert into an array
     *
     * @param array    The array to insert into
     * @param id       The id to insert
     * @param location The location to insert
     * @return The new array
     */
    private static long[] insertIntoArray(long[] array, long id, int location) {
        final var newArray = Arrays.copyOf(array, array.length + 1);
        newArray[location] = id;
        System.arraycopy(array, location, newArray, location + 1, array.length - location);
        return newArray;
    }

    /**
     * Convert a location from {@link Arrays#binarySearch} to something usable
     *
     * @param location The location, as defined by {@code - (insertion point) - 1}
     * @return The actual insertion point
     */
    private static int convertToInsertionPoint(int location) {
        if (location >= 0) {
            throw new IllegalArgumentException("Invalid location");
        }
        return -(location + 1);
    }

    /**
     * Save the ignored tasks and challenges
     */
    private static synchronized void save() {
        final var doSave = save;
        save = false;
        final var tasks = ignoredTasks;
        final var challenges = ignoredChallenges;
        if (doSave) {
            Config.getPref().putList("mapillary.ignore.tasks", convertList(tasks));
            Config.getPref().putList("mapillary.ignore.challenges", convertList(challenges));
        }
    }

    /**
     * Convert an array of ids to a list
     *
     * @param ids The ids to convert
     * @return The list to save
     */
    private static List<String> convertList(long[] ids) {
        return LongStream.of(ids).mapToObj(Long::toString).toList();
    }

    /**
     * Unignore a task
     *
     * @param id The task to remove from the ignore list
     */
    public static synchronized void unignoreTask(long id) {
        final var location = Arrays.binarySearch(ignoredTasks, id);
        if (location >= 0) {
            ignoredTasks = removeFromArray(ignoredTasks, location);
            save = true;
            MainApplication.worker.execute(IgnoreList::save);
        }
    }

    /**
     * Remove an array index
     *
     * @param array    The array to modify
     * @param location The location to remove
     * @return The new array
     */
    private static long[] removeFromArray(long[] array, int location) {
        final var newArray = Arrays.copyOf(array, array.length - 1);
        System.arraycopy(array, location, newArray, location, array.length - 1);
        return newArray;
    }

    /**
     * Check if a task is ignored
     *
     * @param id The task to check
     * @return {@code true} if the task is ignored
     */
    public static boolean isTaskIgnored(long id) {
        return Arrays.binarySearch(ignoredTasks, id) >= 0;
    }

    /**
     * Ignore a challenge
     *
     * @param id The challenge to ignore
     */
    public static synchronized void ignoreChallenge(long id) {
        final var location = Arrays.binarySearch(ignoredChallenges, id);
        if (location < 0) {
            ignoredChallenges = insertIntoArray(ignoredChallenges, id, convertToInsertionPoint(location));
            save = true;
            MainApplication.worker.execute(IgnoreList::save);
        }
    }

    /**
     * Remove a challenge from the ignore list
     *
     * @param id The challenge to remove
     */
    public static synchronized void unignoreChallenge(long id) {
        final var location = Arrays.binarySearch(ignoredChallenges, id);
        if (location >= 0) {
            ignoredChallenges = removeFromArray(ignoredChallenges, location);
            save = true;
            MainApplication.worker.execute(IgnoreList::save);
        }
    }

    /**
     * Check if a challenge is ignored
     *
     * @param id The challenge to check
     * @return {@code true} if the challenge is ignored
     */
    public static boolean isChallengeIgnored(long id) {
        return Arrays.binarySearch(ignoredChallenges, id) >= 0;
    }

    /**
     * Get the ignored tasks
     *
     * @return The ignored task ids
     */
    public static long[] ignoredTasks() {
        return ignoredTasks.clone();
    }

    /**
     * Get the ignored challenges
     *
     * @return The ignored challenge ids
     */
    public static long[] ignoredChallenges() {
        return ignoredChallenges.clone();
    }
}

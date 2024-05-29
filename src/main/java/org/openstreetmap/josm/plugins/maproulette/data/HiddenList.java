// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.data;

import java.util.Arrays;
import java.util.function.LongPredicate;

/**
 * Hidden tasks from filters
 */
public final class HiddenList {
    private static LongPredicate[] tests = new LongPredicate[0];
    private static long[] hidden = new long[0];

    private HiddenList() {
        // Hide constructor
    }

    /**
     * Update the hidden list
     * @param ids The ids to use
     */
    public static void update(long... ids) {
        final var t = tests;
        final var tIds = new long[ids.length];
        int count = 0;
        for (long id : ids) {
            for (LongPredicate test : t) {
                if (test.test(id)) {
                    tIds[count++] = id;
                    break;
                }
            }
        }
        var h = Arrays.copyOf(tIds, count);
        Arrays.sort(h);
        hidden = h;
    }

    /**
     * Add a function to use for checking whether a task is hidden
     * @param checkFunction The function to use; a return of {@code true} means that the task should be hidden.
     */
    public static void addListUpdater(LongPredicate checkFunction) {
        synchronized (HiddenList.class) {
            var t = Arrays.copyOf(tests, tests.length + 1);
            t[t.length - 1] = checkFunction;
            tests = t;
        }
    }

    /**
     * Check if an id is hidden
     * @param id The id to check
     * @return {@code true} if the id is hidden
     */
    public static boolean isHidden(long id) {
        return Arrays.binarySearch(hidden, id) >= 0;
    }
}

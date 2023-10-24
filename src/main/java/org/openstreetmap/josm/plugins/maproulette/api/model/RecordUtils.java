// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Objects;

import org.openstreetmap.josm.tools.JosmRuntimeException;

import jakarta.annotation.Nonnull;

/**
 * Utils for {@link Record} classes with array fields
 */
final class RecordUtils {
    private RecordUtils() {
        // Hide constructor
    }

    /**
     * A "sane" equals method for record classes with array fields
     * @param first The first object
     * @param second The second object
     * @return {@code true} if the records are equal
     * @param <T> The record type
     */
    static <T extends Record> boolean equals(@Nonnull T first, @Nonnull T second) {
        for (RecordComponent component : first.getClass().getRecordComponents()) {
            final var type = component.getType();
            final var firstObj = getField(first, component);
            final var secondObj = getField(second, component);
            if ((type.isArray() && !arrayEquals(type, firstObj, secondObj)) ||
                    (!type.isArray() && !Objects.equals(firstObj, secondObj))) {
                return false;
            }
        }
        return true;
    }

    private static boolean arrayEquals(Class<?> type, Object first, Object second) {
        if (int[].class.equals(type)) {
            return Arrays.equals((int[]) first, (int[]) second);
        } else if (byte[].class.equals(type)) {
            return Arrays.equals((byte[]) first, (byte[]) second);
        } else if (short[].class.equals(type)) {
            return Arrays.equals((short[]) first, (short[]) second);
        } else if (long[].class.equals(type)) {
            return Arrays.equals((long[]) first, (long[]) second);
        } else if (float[].class.equals(type)) {
            return Arrays.equals((float[]) first, (float[]) second);
        } else if (double[].class.equals(type)) {
            return Arrays.equals((double[]) first, (double[]) second);
        } else if (boolean[].class.equals(type)) {
            return Arrays.equals((boolean[]) first, (boolean[]) second);
        } else if (char[].class.equals(type)) {
            return Arrays.equals((char[]) first, (char[]) second);
        } else {
            return Arrays.equals((Object[]) first, (Object[]) second);
        }
    }

    /**
     * A "sane" {@link #hashCode()} method for record objects with array fields
     * @param hashCode The record to get the hashcode for
     * @return The hashcode
     */
    static int hashCode(@Nonnull Record hashCode) {
        var hash = 0;
        for (RecordComponent component : hashCode.getClass().getRecordComponents()) {
            hash *= 31;
            final Object field = getField(hashCode, component);
            final var type = component.getType();
            if (type.isArray()) {
                hash += arrayHashCode(type, field);
            } else {
                hash += Objects.hashCode(field);
            }
        }
        return hash;
    }

    private static int arrayHashCode(Class<?> type, Object field) {
        if (int[].class.equals(type)) {
            return Arrays.hashCode((int[]) field);
        } else if (byte[].class.equals(type)) {
            return Arrays.hashCode((byte[]) field);
        } else if (short[].class.equals(type)) {
            return Arrays.hashCode((short[]) field);
        } else if (long[].class.equals(type)) {
            return Arrays.hashCode((long[]) field);
        } else if (float[].class.equals(type)) {
            return Arrays.hashCode((float[]) field);
        } else if (double[].class.equals(type)) {
            return Arrays.hashCode((double[]) field);
        } else if (boolean[].class.equals(type)) {
            return Arrays.hashCode((boolean[]) field);
        } else if (char[].class.equals(type)) {
            return Arrays.hashCode((char[]) field);
        } else {
            return Arrays.hashCode((Object[]) field);
        }
    }

    /**
     * A "sane" {@link #toString()} method for record objects with array fields
     * @param toString The record to convert to a string
     * @return The string to return from the records {@link #toString()} method
     */
    @Nonnull
    static String toString(@Nonnull Record toString) {
        final var stringBuilder = new StringBuilder(256);
        stringBuilder.append(toString.getClass().getSimpleName()).append('[');
        for (RecordComponent component : toString.getClass().getRecordComponents()) {
            stringBuilder.append(component.getName()).append('=');
            final Class<?> type = component.getType();
            final Object field = getField(toString, component);
            if (field == null) {
                stringBuilder.append("null");
            } else if (type.isArray()) {
                stringBuilder.append(getArrayString(type, field));
            } else {
                stringBuilder.append(field);
            }
        }
        return stringBuilder.append(']').toString();
    }

    private static String getArrayString(Class<?> type, Object field) {
        if (int[].class.equals(type)) {
            return Arrays.toString((int[]) field);
        } else if (byte[].class.equals(type)) {
            return Arrays.toString((byte[]) field);
        } else if (short[].class.equals(type)) {
            return Arrays.toString((short[]) field);
        } else if (long[].class.equals(type)) {
            return Arrays.toString((long[]) field);
        } else if (float[].class.equals(type)) {
            return Arrays.toString((float[]) field);
        } else if (double[].class.equals(type)) {
            return Arrays.toString((double[]) field);
        } else if (boolean[].class.equals(type)) {
            return Arrays.toString((boolean[]) field);
        } else if (char[].class.equals(type)) {
            return Arrays.toString((char[]) field);
        } else {
            return Arrays.toString((Object[]) field);
        }
    }

    private static Object getField(Record object, RecordComponent component) {
        try {
            return component.getAccessor().invoke(object);
        } catch (InvocationTargetException | IllegalAccessException e) {
            // We should never hit this with records
            throw new JosmRuntimeException(e);
        }
    }
}

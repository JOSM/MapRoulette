// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.function.Executable;
import org.openstreetmap.josm.tools.JosmRuntimeException;

/**
 * An assertion class for Records
 */
public final class RecordAssertion {
    /**
     * Assert that two records are equal
     *
     * @param expected The expected record
     * @param actual   The actual record
     * @param <T>      The record type
     */
    public static <T extends Record> void assertRecordsEqual(T expected, T actual) {
        assertRecordsEqual(expected, actual, null);
    }

    /**
     * Assert that two records are equal
     *
     * @param expected The expected record
     * @param actual   The actual record
     * @param message  The message header
     * @param <T>      The record type
     */
    public static <T extends Record> void assertRecordsEqual(T expected, T actual, String message) {
        if (expected == actual) {
            return;
        } else if (expected == null) {
            fail("The expected record was null");
        } else if (actual == null) {
            fail("The actual record was null");
        }
        final Class<? extends Record> clazz = expected.getClass();
        assertEquals(clazz, actual.getClass(), "The record classes differ");
        RecordComponent[] components = clazz.getRecordComponents();
        List<Executable> executableList = new ArrayList<>(components.length);
        try {
            for (RecordComponent component : components) {
                final var method = component.getAccessor();
                final var expectedObj = method.invoke(expected);
                final var actualObj = method.invoke(actual);
                final var sameClass = actualObj != null && expectedObj != null
                        && expectedObj.getClass().equals(actualObj.getClass());
                if (expectedObj instanceof Record && actualObj instanceof Record && sameClass) {
                    executableList.add(
                            () -> assertRecordsEqual((Record) expectedObj, (Record) actualObj, component.getName()));
                } else if (expectedObj instanceof Object[] expectedArray && actualObj instanceof Object[] actualArray) {
                    if (expectedObj.getClass().getComponentType().isRecord() && sameClass) {
                        executableList
                                .add(() -> assertEquals(expectedArray.length, actualArray.length, component.getName()));
                        if (expectedArray.length == actualArray.length) {
                            for (int i = 0; i < expectedArray.length; i++) {
                                final var index = i;
                                executableList.add(() -> assertRecordsEqual((Record) expectedArray[index],
                                        (Record) actualArray[index], component.getName()));
                            }
                        }
                    } else {
                        executableList.add(() -> assertArrayEquals(expectedArray, actualArray, component.getName()));
                    }
                } else if (expectedObj instanceof long[] && actualObj instanceof long[]) {
                    executableList.add(
                            () -> assertArrayEquals((long[]) expectedObj, (long[]) actualObj, component.getName()));
                } else if (expectedObj instanceof int[] && actualObj instanceof int[]) {
                    executableList
                            .add(() -> assertArrayEquals((int[]) expectedObj, (int[]) actualObj, component.getName()));
                } else if (expectedObj instanceof short[] && actualObj instanceof short[]) {
                    executableList.add(
                            () -> assertArrayEquals((short[]) expectedObj, (short[]) actualObj, component.getName()));
                } else if (expectedObj instanceof char[] && actualObj instanceof char[]) {
                    executableList.add(
                            () -> assertArrayEquals((char[]) expectedObj, (char[]) actualObj, component.getName()));
                } else if (expectedObj instanceof byte[] && actualObj instanceof byte[]) {
                    executableList.add(
                            () -> assertArrayEquals((byte[]) expectedObj, (byte[]) actualObj, component.getName()));
                } else if (expectedObj instanceof boolean[] && actualObj instanceof boolean[]) {
                    executableList.add(() -> assertArrayEquals((boolean[]) expectedObj, (boolean[]) actualObj,
                            component.getName()));
                } else if (expectedObj instanceof double[] && actualObj instanceof double[]) {
                    executableList.add(
                            () -> assertArrayEquals((double[]) expectedObj, (double[]) actualObj, component.getName()));
                } else if (expectedObj instanceof float[] && actualObj instanceof float[]) {
                    executableList.add(
                            () -> assertArrayEquals((float[]) expectedObj, (float[]) actualObj, component.getName()));
                } else {
                    executableList.add(() -> assertEquals(expectedObj, actualObj, component.getName()));
                }
            }
        } catch (ReflectiveOperationException exception) {
            throw new JosmRuntimeException(exception);
        }
        assertAll(message == null ? clazz.getName() : message, executableList);
    }
}

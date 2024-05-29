// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.Logging;

/**
 * Handle logs from the test. You can add a {@link TestHandler} parameter to a test method, if you need/want to perform validation on the
 * logs themselves.
 * Copied from Mapillary, something similar in MapWithAI
 * TODO Put in JOSM core
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ExtendWith(LoggingHandler.LoggingHandlerImplementation.class)
public @interface LoggingHandler {
    /**
     * Set the level at which to fail the test. The default is {@link java.util.logging.Level#SEVERE} (1000).
     *
     * @return the error level at which to fail
     */
    int value() default 1000;

    /**
     * The actual extension for setting the log
     */
    class LoggingHandlerImplementation implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

        @Override
        public void afterEach(ExtensionContext context) {
            MainApplication.worker.submit(() -> {
                /* Sync thread */ });
            GuiHelper.runInEDTAndWait(() -> {
                /* Sync thread */ });
            final int failLevel = AnnotationSupport.findAnnotation(context.getElement(), LoggingHandler.class)
                    .map(LoggingHandler::value)
                    .orElse(AnnotationSupport.findAnnotation(context.getClass(), LoggingHandler.class)
                            .map(LoggingHandler::value).orElse(1000));
            ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.create(LoggingHandler.class));
            TestHandler testHandler = store.get(TestHandler.class, TestHandler.class);
            Logging.getLogger().removeHandler(testHandler);
            Handler[] handlers = store.get(Logging.class, Handler[].class);
            for (Handler handler : handlers) {
                Logging.getLogger().addHandler(handler);
            }
            assertAll(
                    testHandler.getRecords().stream().filter(logRecord -> logRecord.getLevel().intValue() >= failLevel)
                            .map(logRecord -> fail(logRecord.getMessage(), logRecord.getThrown())));
            testHandler.clearRecords();
        }

        @Override
        public void beforeEach(ExtensionContext context) {
            ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.create(LoggingHandler.class));
            store.put(Logging.class, Logging.getLogger().getHandlers());
            for (Handler handler : Logging.getLogger().getHandlers()) {
                Logging.getLogger().removeHandler(handler);
            }
            TestHandler testHandler = new TestHandler();
            store.put(TestHandler.class, testHandler);
            Logging.getLogger().addHandler(testHandler);
            // Ensure that exceptions thrown in the EDT are logged -- they weren't in testing
            GuiHelper.runInEDTAndWaitWithException(() -> Thread.currentThread()
                    .setUncaughtExceptionHandler((thread, throwable) -> Logging.error(throwable)));
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
                throws ParameterResolutionException {
            return TestHandler.class.isAssignableFrom(parameterContext.getParameter().getType());
        }

        @Override
        public TestHandler resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
                throws ParameterResolutionException {
            return extensionContext.getStore(ExtensionContext.Namespace.create(LoggingHandler.class))
                    .get(TestHandler.class, TestHandler.class);
        }
    }

    class TestHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();

        /**
         * Get the records saved by this handler
         *
         * @return The saved records
         */
        public List<LogRecord> getRecords() {
            return Collections.unmodifiableList(this.records);
        }

        /**
         * Clear stored records
         */
        public void clearRecords() {
            this.records.clear();
        }

        @Override
        public void publish(LogRecord record) {
            this.records.add(record);
        }

        @Override
        public void flush() {
            // Do nothing
        }

        @Override
        public void close() throws SecurityException {
            // Do nothing
        }
    }
}

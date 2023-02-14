// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api;

import java.io.Serial;

/**
 * Thrown if the user is not authorized for the specified operation
 */
public class UnauthorizedException extends RuntimeException {
    /**
     * The serial UID for this exception
     */
    @Serial
    private static final long serialVersionUID = -9055600554413721226L;

    /**
     * Create the exception with a specified message
     *
     * @param message the message
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.maproulette.api.model;

/**
 * The mapillary images to use for this challenge
 *
 * @param images The images
 */
public record MapillaryImages(Image... images) {
    /**
     * The specific mapillary image to show
     *
     * @param key      The image key
     * @param lat      The latitude
     * @param lon      The longitude
     * @param url_320  The link to the 320px image (not usable)
     * @param url_640  The link to the 640px image (not usable)
     * @param url_1024 The link to the 1024px image (not usable)
     * @param url_2048 The link to the 2048px image (not usable)
     */
    public record Image(String key, double lat, double lon, String url_320, String url_640, String url_1024, String url_2048) {

    }
}

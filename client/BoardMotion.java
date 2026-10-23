package client;

import common.CaptureRay;

/** Presentation timing only. Coordinates and legality always come from the server. */
final class BoardMotion {
    static final double HOVER_SECONDS = .08;
    static final double EFFECT_SECONDS = .9;

    private BoardMotion() {}

    static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    static double ease(double value) {
        double t = clamp(value);
        return 1 - Math.pow(1 - t, 3);
    }

    static double sourceAlpha(double seconds) {
        return 1 - ease(seconds / .12);
    }

    static double areaBlend(double seconds) {
        return ease((seconds - .18) / .22);
    }

    static double beamProgress(double seconds) {
        return ease((seconds - .08) / .20);
    }

    static double beamAlpha(double seconds) {
        return seconds < .08 ? 0 : 1 - clamp((seconds - .28) / .15);
    }

    static double front(double origin, double end, double seconds) {
        return origin + (end - origin) * beamProgress(seconds);
    }

    static boolean onRay(CaptureRay ray, int row, int column) {
        int dr = ray.endRow() - ray.startRow(), dc = ray.endColumn() - ray.startColumn();
        return (row - ray.startRow()) * dc == (column - ray.startColumn()) * dr
                && row >= Math.min(ray.startRow(), ray.endRow())
                && row <= Math.max(ray.startRow(), ray.endRow())
                && column >= Math.min(ray.startColumn(), ray.endColumn())
                && column <= Math.max(ray.startColumn(), ray.endColumn());
    }
}

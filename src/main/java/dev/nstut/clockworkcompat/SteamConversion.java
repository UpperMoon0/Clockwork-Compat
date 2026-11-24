package dev.nstut.clockworkcompat;

/** Explicit gameplay-unit conversion, bounded by native extractor throughput. */
public final class SteamConversion {
    private SteamConversion() {}
    public static final int MAX_MB_PER_TICK = 100;
    public static final double KG_PER_MB = 0.001;
    public static final double MAX_PRESSURE_PA = 2_125_000;

    /** Kelvin 0.5.2 uses R_specific = (gamma - 1) * Cp / gamma,
     * with Cp converted from kJ/(kg K) to J/(kg K). */
    public static double pressureCoefficient(double cp, double gamma) {
        if (!positive(cp) || !Double.isFinite(gamma) || gamma <= 1) return Double.NaN;
        return (gamma - 1) * cp / gamma * 1000;
    }

    /** Sum of mass * R_specific is in J/K. The mixture's pressure is
     * this sum times temperature divided by effective node volume.
     * Use the greater temperature as a conservative mixing bound. */
    public static int budget(double currentCoefficient, double volume, double currentTemperature,
                             double steamTemperature, double coefficientPerMb, double pressureLimit) {
        if (!Double.isFinite(currentCoefficient) || currentCoefficient < 0
                || !positive(volume) || !positive(currentTemperature) || !positive(steamTemperature)
                || !positive(coefficientPerMb) || !positive(pressureLimit)) return 0;
        double available = pressureLimit * volume / Math.max(currentTemperature, steamTemperature)
                - currentCoefficient;
        if (!Double.isFinite(available) || available <= 0) return 0;
        // Round toward safety at a floating-point boundary.
        return (int) Math.min(MAX_MB_PER_TICK, Math.floor(Math.nextDown(available / coefficientPerMb)));
    }

    public static boolean positive(double x) { return Double.isFinite(x) && x > 0; }
}

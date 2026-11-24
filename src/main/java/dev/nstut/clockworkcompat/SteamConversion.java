package dev.nstut.clockworkcompat;

/** Explicit gameplay-unit conversion, bounded by native extractor throughput. */
public final class SteamConversion {
    private SteamConversion() {}
    public static final int MAX_MB_PER_TICK = 100;
    public static final double KG_PER_MB = 0.001;
    public static final double MAX_PRESSURE_PA = 2_125_000;
    public static final double R = 8.314462618;

    public static int budget(double currentMoles, double volume, double currentTemperature,
                             double steamTemperature, double molesPerMb, double pressureLimit) {
        if (!Double.isFinite(currentMoles) || currentMoles < 0
                || !positive(volume) || !positive(currentTemperature) || !positive(steamTemperature)
                || !positive(molesPerMb) || !positive(pressureLimit)) return 0;
        double available = pressureLimit * volume / (R * Math.max(currentTemperature, steamTemperature))
                - currentMoles;
        if (!Double.isFinite(available) || available <= 0) return 0;
        return (int) Math.min(MAX_MB_PER_TICK, Math.floor(available / molesPerMb));
    }

    public static boolean positive(double x) { return Double.isFinite(x) && x > 0; }
}

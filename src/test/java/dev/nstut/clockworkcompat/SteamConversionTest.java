package dev.nstut.clockworkcompat;

public final class SteamConversionTest {
    private static void expect(int expected, int actual) {
        if (expected != actual) throw new AssertionError(expected + " != " + actual);
    }
    public static void main(String[] args) {
        double steamR = SteamConversion.pressureCoefficient(2.2, 1.33);
        if (Math.abs(steamR - 545.8646616541354) > 1e-9) throw new AssertionError("Kelvin R");
        double perMb = steamR * SteamConversion.KG_PER_MB;
        expect(100, SteamConversion.budget(0,1,300,373,perMb,2_125_000));
        expect(0, SteamConversion.budget(1000,1,300,373,perMb,100));
        expect(0, SteamConversion.budget(0,0,300,373,perMb,100));
        expect(0, SteamConversion.budget(Double.NaN,1,300,373,perMb,100));
        expect(0, SteamConversion.budget(0,1,300,Double.POSITIVE_INFINITY,perMb,100));
        expect(10, SteamConversion.budget(0,1,300,373,perMb,perMb*373*10.5));
        expect(0, SteamConversion.budget(1,1,1000,373,perMb,500));
        expect(0, SteamConversion.budget(0,1,300,373,0,100));
        // Mixed-gas pressure follows Kelvin's mass-weighted R, not gas density.
        double airR = SteamConversion.pressureCoefficient(1.005,1.4);
        double current = 0.6*airR + 3.1*steamR;
        double limit = 2_125_000, volume = 0.34, t = 373;
        int budget = SteamConversion.budget(current,volume,300,t,perMb,limit);
        if ((current + budget*perMb)*t/volume > limit) throw new AssertionError("pressure overshoot");
        if (!Double.isNaN(SteamConversion.pressureCoefficient(2.2,1))) throw new AssertionError("invalid gamma");
        System.out.println("SteamConversionTest: 11 checks passed");
    }
}

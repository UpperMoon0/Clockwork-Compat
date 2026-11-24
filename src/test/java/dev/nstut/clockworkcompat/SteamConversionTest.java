package dev.nstut.clockworkcompat;

public final class SteamConversionTest {
    private static void expect(int expected, int actual) {
        if (expected != actual) throw new AssertionError(expected + " != " + actual);
    }
    public static void main(String[] args) {
        expect(100, SteamConversion.budget(0,1,300,373,0.05,2_125_000));
        expect(0, SteamConversion.budget(1000,1,300,373,0.05,100));
        expect(0, SteamConversion.budget(0,0,300,373,0.05,100));
        expect(0, SteamConversion.budget(Double.NaN,1,300,373,0.05,100));
        expect(0, SteamConversion.budget(0,1,300,Double.POSITIVE_INFINITY,0.05,100));
        expect(10, SteamConversion.budget(0,1,300,373,0.05,SteamConversion.R*373*0.525));
        expect(0, SteamConversion.budget(1,1,1000,373,0.05,SteamConversion.R*500));
        expect(0, SteamConversion.budget(0,1,300,373,0,100));
        System.out.println("SteamConversionTest: 8 checks passed");
    }
}

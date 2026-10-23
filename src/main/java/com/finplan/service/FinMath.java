package com.finplan.service;

/** Shared time-value-of-money helpers. Rates are annual percentages (e.g. 12 = 12%). */
public final class FinMath {
    private FinMath() {}

    public static double monthlyRate(double annualPct) {
        return Math.pow(1 + annualPct / 100.0, 1.0 / 12) - 1;
    }

    public static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** Future value of an end-of-month SIP. */
    public static double fvSip(double sip, double annualPct, int months) {
        double mr = monthlyRate(annualPct);
        if (Math.abs(mr) < 1e-12) return sip * months;
        return sip * (Math.pow(1 + mr, months) - 1) / mr;
    }

    /** Monthly SIP needed to reach target after the given months. */
    public static double requiredSip(double target, double annualPct, int months) {
        if (months <= 0) return target;
        double mr = monthlyRate(annualPct);
        if (Math.abs(mr) < 1e-12) return target / months;
        return target * mr / (Math.pow(1 + mr, months) - 1);
    }

    public static double fvLump(double principal, double annualPct, double years) {
        return principal * Math.pow(1 + annualPct / 100.0, years);
    }

    public static double emi(double principal, double annualPct, int months) {
        double r = annualPct / 1200.0;
        if (r == 0) return principal / months;
        double f = Math.pow(1 + r, months);
        return principal * r * f / (f - 1);
    }
}

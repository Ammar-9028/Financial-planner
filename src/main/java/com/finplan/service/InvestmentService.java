package com.finplan.service;

import com.finplan.model.Models.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** SIP/lump-sum projections and risk-based asset allocation. */
public final class InvestmentService {
    private InvestmentService() {}

    /** Long-run NOMINAL return assumptions per asset class. Edit these to track the market. */
    static final double INFLATION_PCT = 6.0;
    static final Map<String, Double> ASSUMED_RETURNS = new LinkedHashMap<>();
    static {
        ASSUMED_RETURNS.put("Large-cap equity", 11.0);
        ASSUMED_RETURNS.put("Mid/small-cap equity", 13.0);
        ASSUMED_RETURNS.put("International equity", 10.0);
        ASSUMED_RETURNS.put("Debt & bonds", 6.5);
        ASSUMED_RETURNS.put("Gold", 7.0);
        ASSUMED_RETURNS.put("REITs / InvITs", 9.0);
        ASSUMED_RETURNS.put("Cash & liquid", 5.5);
    }

    public static Map<String, Object> assumptions() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("note", "Static planning assumptions, not live quotes. Edit InvestmentService.ASSUMED_RETURNS.");
        m.put("inflationPct", INFLATION_PCT);
        m.put("expectedReturnsPct", ASSUMED_RETURNS);
        return m;
    }

    public static InvestmentRes project(InvestmentReq q) {
        Check.range("monthlySip", q.monthlySip(), 0, 1e9);
        Check.range("lumpsum", q.lumpsum(), 0, 1e12);
        Check.range("years", q.years(), 1, 60);
        Check.range("annualReturnPct", q.annualReturnPct(), -20, 50);
        Check.range("stepUpPct", q.stepUpPct(), 0, 50);
        Check.range("inflationPct", q.inflationPct(), 0, 30);

        double mr = FinMath.monthlyRate(q.annualReturnPct());
        double sip = q.monthlySip();
        double value = q.lumpsum();
        double invested = q.lumpsum();
        List<YearPoint> series = new ArrayList<>();
        series.add(new YearPoint(0, FinMath.round2(invested), FinMath.round2(value), FinMath.round2(value)));

        for (int y = 1; y <= q.years(); y++) {
            for (int m = 0; m < 12; m++) {
                value = value * (1 + mr) + sip;
                invested += sip;
            }
            double real = value / Math.pow(1 + q.inflationPct() / 100.0, y);
            series.add(new YearPoint(y, FinMath.round2(invested), FinMath.round2(value), FinMath.round2(real)));
            sip *= 1 + q.stepUpPct() / 100.0;
        }
        YearPoint last = series.get(series.size() - 1);
        return new InvestmentRes(last.invested(), last.value(), FinMath.round2(last.value() - last.invested()),
                last.realValue(), series);
    }

    public static AllocationRes allocate(AllocationReq q) {
        Check.range("age", q.age(), 18, 90);
        Check.range("horizonYears", q.horizonYears(), 1, 60);
        String risk = q.riskProfile() == null ? "MODERATE" : q.riskProfile().toUpperCase();

        int eq = 110 - q.age();
        eq += switch (risk) {
            case "CONSERVATIVE" -> -15;
            case "MODERATE" -> 0;
            case "AGGRESSIVE" -> 10;
            default -> throw new IllegalArgumentException("riskProfile must be CONSERVATIVE, MODERATE or AGGRESSIVE");
        };
        if (q.horizonYears() < 3) eq = Math.min(eq, 15);
        else if (q.horizonYears() < 5) eq = Math.min(eq, 35);
        eq = Math.max(10, Math.min(85, eq));

        int large = (int) Math.round(eq * 0.55);
        int mid = (int) Math.round(eq * 0.25);
        int intl = eq - large - mid;
        int rest = 100 - eq;
        int gold = rest >= 30 ? 10 : 5;
        int reit = eq >= 50 ? 5 : 0;
        int cash = 5;
        int debt = rest - gold - reit - cash;

        List<AllocationItem> all = new ArrayList<>();
        add(all, "Large-cap equity", large, "Nifty 50 / Sensex index funds, large-cap ETFs");
        add(all, "Mid/small-cap equity", mid, "Mid/small-cap index funds, flexi-cap funds");
        add(all, "International equity", intl, "Global / US index funds or feeder funds (check RBI/fund limits)");
        add(all, "Debt & bonds", debt, "PPF, EPF, short-duration & gilt funds, high-grade bonds");
        add(all, "Gold", gold, "Gold ETFs, Sovereign Gold Bonds (secondary market)");
        add(all, "REITs / InvITs", reit, "Listed REITs and InvITs");
        add(all, "Cash & liquid", cash, "Liquid funds, savings account, FDs");

        double blended = 0;
        for (AllocationItem i : all) blended += i.percent() * i.expectedReturnPct() / 100.0;
        String note = "Equity share starts from (110 - age), adjusted for risk profile and horizon. "
                + "Rebalance yearly or when any class drifts more than 5 points.";
        return new AllocationRes(all, FinMath.round2(blended), note);
    }

    private static void add(List<AllocationItem> l, String cls, int pct, String vehicles) {
        if (pct > 0) l.add(new AllocationItem(cls, pct, ASSUMED_RETURNS.get(cls), vehicles));
    }
}

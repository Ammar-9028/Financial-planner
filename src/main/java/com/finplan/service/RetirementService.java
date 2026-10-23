package com.finplan.service;

import com.finplan.model.Models.*;
import java.util.ArrayList;
import java.util.List;

/** Retirement corpus, SIP gap and FIRE (25x) number. */
public final class RetirementService {
    private RetirementService() {}

    public static RetirementRes plan(RetirementReq q) {
        Check.range("currentAge", q.currentAge(), 18, 80);
        Check.range("retirementAge", q.retirementAge(), q.currentAge() + 1, 90);
        Check.range("lifeExpectancy", q.lifeExpectancy(), q.retirementAge() + 1, 110);
        Check.range("monthlyExpenseToday", q.monthlyExpenseToday(), 0, 1e10);
        Check.range("inflationPct", q.inflationPct(), 0, 30);
        Check.range("preReturnPct", q.preReturnPct(), -10, 40);
        Check.range("postReturnPct", q.postReturnPct(), -10, 40);
        Check.range("currentCorpus", q.currentCorpus(), 0, 1e12);
        Check.range("monthlySip", q.monthlySip(), 0, 1e10);

        int n = q.retirementAge() - q.currentAge();
        int retiredYears = q.lifeExpectancy() - q.retirementAge();
        double monthlyAtRet = q.monthlyExpenseToday() * Math.pow(1 + q.inflationPct() / 100.0, n);
        double annual = monthlyAtRet * 12;

        double real = (1 + q.postReturnPct() / 100.0) / (1 + q.inflationPct() / 100.0) - 1;
        double needed = Math.abs(real) < 1e-9 ? annual * retiredYears
                : annual * (1 - Math.pow(1 + real, -retiredYears)) / real * (1 + real);

        double projected = FinMath.fvLump(q.currentCorpus(), q.preReturnPct(), n)
                + FinMath.fvSip(q.monthlySip(), q.preReturnPct(), n * 12);
        double gap = needed - projected;
        double extra = gap > 0 ? FinMath.requiredSip(gap, q.preReturnPct(), n * 12) : 0;

        List<RetirementPoint> series = new ArrayList<>();
        for (int a = q.currentAge(); a <= q.retirementAge(); a++) {
            int y = a - q.currentAge();
            double c = FinMath.fvLump(q.currentCorpus(), q.preReturnPct(), y)
                    + FinMath.fvSip(q.monthlySip(), q.preReturnPct(), y * 12);
            series.add(new RetirementPoint(a, FinMath.round2(c)));
        }
        return new RetirementRes(FinMath.round2(monthlyAtRet), FinMath.round2(needed), FinMath.round2(projected),
                FinMath.round2(gap), FinMath.round2(extra), FinMath.round2(q.monthlyExpenseToday() * 12 * 25),
                gap <= 0, series);
    }
}

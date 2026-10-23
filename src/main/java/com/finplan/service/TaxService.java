package com.finplan.service;

import com.finplan.model.Models.*;
import java.util.ArrayList;
import java.util.List;

/**
 * India income-tax estimate (old vs new regime), individual below 60.
 * Slabs follow FY 2025-26 as understood at build time - VERIFY against current law before relying on them.
 * Surcharge is not modelled (applies above 50 lakh).
 */
public final class TaxService {
    private TaxService() {}

    // {upper bound of slab, rate}
    static final double[][] NEW = {{400_000, 0}, {800_000, .05}, {1_200_000, .10}, {1_600_000, .15},
            {2_000_000, .20}, {2_400_000, .25}, {Double.MAX_VALUE, .30}};
    static final double[][] OLD = {{250_000, 0}, {500_000, .05}, {1_000_000, .20}, {Double.MAX_VALUE, .30}};

    static final double NEW_STD_DEDUCTION = 75_000;
    static final double OLD_STD_DEDUCTION = 50_000;
    static final double NEW_REBATE_LIMIT = 1_200_000;
    static final double OLD_REBATE_LIMIT = 500_000;
    static final double CESS = 1.04;

    static double slabTax(double income, double[][] slabs) {
        double tax = 0, prev = 0;
        for (double[] s : slabs) {
            if (income > prev) tax += (Math.min(income, s[0]) - prev) * s[1];
            prev = s[0];
        }
        return tax;
    }

    public static TaxRes compare(TaxReq q) {
        Check.range("grossIncome", q.grossIncome(), 0, 1e10);
        Check.range("section80C", q.section80C(), 0, 1e9);
        Check.range("section80D", q.section80D(), 0, 1e9);
        Check.range("npsExtra", q.npsExtra(), 0, 1e9);
        Check.range("homeLoanInterest", q.homeLoanInterest(), 0, 1e9);
        Check.range("otherDeductions", q.otherDeductions(), 0, 1e10);

        // New regime: standard deduction only; rebate makes tax nil up to 12L with marginal relief above.
        double newTaxable = Math.max(0, q.grossIncome() - NEW_STD_DEDUCTION);
        double newTax = slabTax(newTaxable, NEW);
        if (newTaxable <= NEW_REBATE_LIMIT) newTax = 0;
        else newTax = Math.min(newTax, newTaxable - NEW_REBATE_LIMIT);
        newTax *= CESS;

        // Old regime: 80C (1.5L cap), 80D, 80CCD(1B) NPS (50k cap), home-loan interest (2L cap), others.
        double ded = OLD_STD_DEDUCTION + Math.min(q.section80C(), 150_000) + Math.min(q.section80D(), 100_000)
                + Math.min(q.npsExtra(), 50_000) + Math.min(q.homeLoanInterest(), 200_000) + q.otherDeductions();
        double oldTaxable = Math.max(0, q.grossIncome() - ded);
        double oldTax = slabTax(oldTaxable, OLD);
        if (oldTaxable <= OLD_REBATE_LIMIT) oldTax = 0;
        oldTax *= CESS;

        boolean newBetter = newTax <= oldTax;
        List<String> tips = new ArrayList<>();
        if (q.section80C() < 150_000)
            tips.add("80C is under the 1.5L cap: ELSS, PPF, EPF/VPF, tax-saver FD or term-plan premium can fill it (old regime only).");
        if (q.npsExtra() < 50_000)
            tips.add("An extra NPS contribution (80CCD(1B), up to 50,000) is old-regime only; employer NPS under 80CCD(2) works in both.");
        if (!newBetter && q.homeLoanInterest() == 0)
            tips.add("Home-loan interest (up to 2L, self-occupied) is a big old-regime lever if you have a loan.");
        tips.add("Estimates exclude surcharge, capital-gains tax and HRA/LTA detail. Confirm with a CA before filing.");

        return new TaxRes(FinMath.round2(oldTaxable), FinMath.round2(oldTax), FinMath.round2(newTaxable),
                FinMath.round2(newTax), newBetter ? "NEW" : "OLD", FinMath.round2(Math.abs(oldTax - newTax)), tips);
    }
}

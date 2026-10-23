package com.finplan.service;

import com.finplan.model.Models.*;
import java.util.ArrayList;
import java.util.List;

public final class SavingsService {
    private SavingsService() {}

    public static SavingsRes analyse(SavingsReq q) {
        Check.range("monthlyIncome", q.monthlyIncome(), 1, 1e10);
        Check.range("monthlyExpenses", q.monthlyExpenses(), 0, 1e10);
        Check.range("monthlyEmi", q.monthlyEmi(), 0, 1e10);
        Check.range("liquidSavings", q.liquidSavings(), 0, 1e12);
        Check.range("dependents", q.dependents(), 0, 20);

        double surplus = q.monthlyIncome() - q.monthlyExpenses() - q.monthlyEmi();
        double rate = surplus / q.monthlyIncome() * 100;
        int months = 6 + (q.dependents() > 0 ? 3 : 0);
        double target = months * (q.monthlyExpenses() + q.monthlyEmi());
        double gap = Math.max(0, target - q.liquidSavings());
        int toTarget = gap == 0 ? 0 : surplus > 0 ? (int) Math.ceil(gap / surplus) : -1;

        String verdict = rate >= 20 ? "Strong saver" : rate >= 10 ? "Healthy" : rate >= 0 ? "Tight" : "Overspending";
        List<String> tips = new ArrayList<>();
        if (q.monthlyEmi() / q.monthlyIncome() > 0.4)
            tips.add("EMIs exceed 40% of income. Prioritise prepaying the highest-rate loan.");
        if (gap > 0) tips.add("Build the emergency fund first, in a liquid fund or sweep-in FD, before raising equity risk.");
        if (rate < 20 && rate >= 0) tips.add("Aim for 20%+ savings rate: automate SIPs on salary day (pay yourself first).");
        if (rate < 0) tips.add("Expenses plus EMIs exceed income. Cut discretionary spend before investing.");
        if (tips.isEmpty()) tips.add("On track. Direct the surplus to goals via step-up SIPs.");

        return new SavingsRes(FinMath.round2(surplus), FinMath.round2(rate), months, FinMath.round2(target),
                FinMath.round2(gap), toTarget, verdict, tips);
    }
}

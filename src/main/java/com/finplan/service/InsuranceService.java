package com.finplan.service;

import com.finplan.model.Models.*;
import java.util.List;

/** Human-life-value style term cover plus health cover guidance. */
public final class InsuranceService {
    private InsuranceService() {}

    public static InsuranceRes assess(InsuranceReq q) {
        Check.range("age", q.age(), 18, 80);
        Check.range("retirementAge", q.retirementAge(), q.age(), 90);
        Check.range("annualIncome", q.annualIncome(), 0, 1e11);
        Check.range("liabilities", q.liabilities(), 0, 1e12);
        Check.range("existingLifeCover", q.existingLifeCover(), 0, 1e12);
        Check.range("liquidAssets", q.liquidAssets(), 0, 1e12);
        Check.range("familyMembers", q.familyMembers(), 1, 20);
        Check.range("existingHealthCover", q.existingHealthCover(), 0, 1e11);

        int workingYears = Math.max(1, q.retirementAge() - q.age());
        int multiple = q.age() < 35 ? 15 : q.age() < 45 ? 12 : q.age() < 55 ? 10 : 7;
        double need = q.annualIncome() * Math.min(multiple, workingYears) + q.liabilities() - q.liquidAssets();
        double term = Math.max(0, need);
        double termGap = Math.max(0, term - q.existingLifeCover());

        double health = Math.max(500_000, q.annualIncome() * 0.5);
        if (q.familyMembers() > 2) health += 500_000;
        health = Math.ceil(health / 100_000) * 100_000;
        double healthGap = Math.max(0, health - q.existingHealthCover());

        List<String> notes = List.of(
                "Buy pure term insurance (not endowment/ULIP) with cover till at least age " + (q.retirementAge() + 5) + ".",
                "Health: a base policy plus a cheap super top-up beats one large base policy; consider a family floater.",
                "Add critical-illness and personal-accident cover; check the claim-settlement ratio and waiting periods.",
                "Keep total premiums under ~10% of income and review cover after marriage, a child, or a new loan.");
        return new InsuranceRes(FinMath.round2(term), FinMath.round2(termGap), health, FinMath.round2(healthGap), notes);
    }
}

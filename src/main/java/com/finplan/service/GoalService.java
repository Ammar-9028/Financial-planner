package com.finplan.service;

import com.finplan.model.Models.*;

/** Goal planner (education, home, wedding, car...) with goal-specific inflation. */
public final class GoalService {
    private GoalService() {}

    public static GoalRes plan(GoalReq q) {
        Check.range("targetToday", q.targetToday(), 1, 1e12);
        Check.range("years", q.years(), 1, 60);
        Check.range("inflationPct", q.inflationPct(), 0, 30);
        Check.range("returnPct", q.returnPct(), -10, 40);
        Check.range("alreadySaved", q.alreadySaved(), 0, 1e12);

        double inflated = q.targetToday() * Math.pow(1 + q.inflationPct() / 100.0, q.years());
        double savedFv = FinMath.fvLump(q.alreadySaved(), q.returnPct(), q.years());
        double shortfall = Math.max(0, inflated - savedFv);
        double sip = FinMath.requiredSip(shortfall, q.returnPct(), q.years() * 12);
        double lump = shortfall / Math.pow(1 + q.returnPct() / 100.0, q.years());
        String name = q.name() == null || q.name().isBlank() ? "Goal" : q.name().strip();
        return new GoalRes(name, FinMath.round2(inflated), FinMath.round2(savedFv), FinMath.round2(shortfall),
                FinMath.round2(sip), FinMath.round2(lump));
    }
}

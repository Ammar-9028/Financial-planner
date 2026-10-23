package com.finplan.service;

import com.finplan.model.Models.*;

public final class LoanService {
    private LoanService() {}

    public static LoanRes analyse(LoanReq q) {
        Check.range("principal", q.principal(), 1, 1e12);
        Check.range("annualRatePct", q.annualRatePct(), 0, 50);
        Check.range("years", q.years(), 1, 40);
        Check.range("extraMonthlyPrepay", q.extraMonthlyPrepay(), 0, 1e10);

        int n = q.years() * 12;
        double emi = FinMath.emi(q.principal(), q.annualRatePct(), n);
        double baseInterest = emi * n - q.principal();

        double r = q.annualRatePct() / 1200.0;
        double bal = q.principal(), interest = 0;
        int month = 0;
        while (bal > 0.005 && month < n) {
            double i = bal * r;
            double pay = Math.min(bal + i, emi + q.extraMonthlyPrepay());
            interest += i;
            bal = bal + i - pay;
            month++;
        }
        return new LoanRes(FinMath.round2(emi), FinMath.round2(baseInterest), month, FinMath.round2(interest),
                FinMath.round2(baseInterest - interest), n - month);
    }
}

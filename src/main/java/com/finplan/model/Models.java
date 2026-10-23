package com.finplan.model;

import java.util.List;

/** All request/response DTOs. Java records are (de)serialised by Gson 2.10+. */
public final class Models {
    private Models() {}

    // ---- Investments ----
    public record InvestmentReq(double monthlySip, double lumpsum, int years, double annualReturnPct,
                                double stepUpPct, double inflationPct) {}
    public record YearPoint(int year, double invested, double value, double realValue) {}
    public record InvestmentRes(double invested, double futureValue, double gains, double realValue,
                                List<YearPoint> series) {}

    // ---- Asset allocation ----
    public record AllocationReq(int age, String riskProfile, int horizonYears) {}
    public record AllocationItem(String assetClass, int percent, double expectedReturnPct, String vehicles) {}
    public record AllocationRes(List<AllocationItem> items, double blendedReturnPct, String note) {}

    // ---- Tax (India: old vs new regime) ----
    public record TaxReq(double grossIncome, double section80C, double section80D, double npsExtra,
                         double homeLoanInterest, double otherDeductions) {}
    public record TaxRes(double oldTaxable, double oldTax, double newTaxable, double newTax,
                         String recommended, double saving, List<String> tips) {}

    // ---- Savings & emergency fund ----
    public record SavingsReq(double monthlyIncome, double monthlyExpenses, double monthlyEmi,
                             double liquidSavings, int dependents) {}
    public record SavingsRes(double monthlySurplus, double savingsRatePct, int emergencyMonths,
                             double emergencyTarget, double gap, int monthsToTarget, String verdict,
                             List<String> tips) {}

    // ---- Insurance ----
    public record InsuranceReq(int age, int retirementAge, double annualIncome, double liabilities,
                               double existingLifeCover, double liquidAssets, int familyMembers,
                               double existingHealthCover) {}
    public record InsuranceRes(double termCoverNeeded, double termGap, double healthCoverNeeded,
                               double healthGap, List<String> notes) {}

    // ---- Retirement / FIRE ----
    public record RetirementReq(int currentAge, int retirementAge, int lifeExpectancy,
                                double monthlyExpenseToday, double inflationPct, double preReturnPct,
                                double postReturnPct, double currentCorpus, double monthlySip) {}
    public record RetirementPoint(int age, double corpus) {}
    public record RetirementRes(double monthlyExpenseAtRetirement, double corpusNeeded, double projectedCorpus,
                                double gap, double extraMonthlySipNeeded, double fireNumberToday,
                                boolean onTrack, List<RetirementPoint> series) {}

    // ---- Goals ----
    public record GoalReq(String name, double targetToday, int years, double inflationPct, double returnPct,
                          double alreadySaved) {}
    public record GoalRes(String name, double inflatedTarget, double savedFutureValue, double shortfall,
                          double monthlySipNeeded, double lumpsumNeededToday) {}

    // ---- Loans ----
    public record LoanReq(double principal, double annualRatePct, int years, double extraMonthlyPrepay) {}
    public record LoanRes(double emi, double totalInterest, int monthsWithPrepay, double interestWithPrepay,
                          double interestSaved, int monthsSaved) {}
}

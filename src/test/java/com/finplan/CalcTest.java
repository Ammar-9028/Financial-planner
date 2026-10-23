package com.finplan;

import static org.junit.jupiter.api.Assertions.*;

import com.finplan.model.Models.*;
import com.finplan.service.*;
import org.junit.jupiter.api.Test;

class CalcTest {
    @Test void newRegimeNilTaxUpTo12LakhTaxable() {
        TaxRes r = TaxService.compare(new TaxReq(1_200_000, 0, 0, 0, 0, 0));
        assertEquals(0.0, r.newTax());
    }

    @Test void newRegimeTaxOn15Lakh() {
        TaxRes r = TaxService.compare(new TaxReq(1_500_000, 0, 0, 0, 0, 0));
        assertEquals(97_500.0, r.newTax(), 0.01);
    }

    @Test void sipMatchesClosedForm() {
        InvestmentRes r = InvestmentService.project(new InvestmentReq(10_000, 0, 10, 12, 0, 6));
        assertEquals(FinMath.fvSip(10_000, 12, 120), r.futureValue(), 1.0);
        assertEquals(1_200_000.0, r.invested(), 0.01);
    }

    @Test void allocationSumsTo100() {
        AllocationRes r = InvestmentService.allocate(new AllocationReq(30, "MODERATE", 15));
        assertEquals(100, r.items().stream().mapToInt(AllocationItem::percent).sum());
    }

    @Test void prepaymentSavesInterest() {
        LoanRes r = LoanService.analyse(new LoanReq(5_000_000, 8.75, 20, 5_000));
        assertTrue(r.interestSaved() > 0 && r.monthsSaved() > 0);
    }

    @Test void invalidInputRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> InvestmentService.project(new InvestmentReq(1, 0, 0, 12, 0, 6)));
    }
}

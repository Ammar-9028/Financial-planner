# FinPlan - Personal Financial Planner (Java 21, Tomcat 9, Maven WAR)

Modules: Investments (SIP/lump-sum/step-up), Asset allocation, Tax (India old vs new regime),
Savings & emergency fund, Insurance (term + health), Retirement & FIRE, Goal planner, Loan & prepayment.

## Architecture (single view)

```
Browser (index.html + app.js + Chart.js)
   |  JSON over HTTPS
   v
[ALB / CloudFront: TLS, HSTS, WAF]  -->  Tomcat 9 / Java 21  (finplan.war)
                                          |- SecurityHeadersFilter  (CSP, nosniff, no-store on /api)
                                          |- ApiServlet  /api/{module}, /api/health, /api/assumptions
                                          |    -> InvestmentService | TaxService | SavingsService | InsuranceService
                                          |       RetirementService | GoalService | LoanService  -> FinMath
                                          '- Stateless: no DB, no session data -> scales horizontally
```

## Build & run
    mvn clean package                 # runs tests, produces target/finplan.war
    cp target/finplan.war $CATALINA_HOME/webapps/      # http://localhost:8080/finplan/
    # or as root app:  cp target/finplan.war $CATALINA_HOME/webapps/ROOT.war

## Docker
    docker build -t finplan .
    docker run -p 8080:8080 finplan   # http://localhost:8080/

## Cloud (AWS example)
Push the image to ECR, run on ECS Fargate behind an ALB, target-group health check `GET /api/health` (200).
Terminate TLS at the ALB; add WAF/CloudFront as needed. The app is stateless, so autoscale on CPU/requests.
Any Tomcat 9 + Java 21 host (EC2, Azure App Service, GCP Cloud Run via the Dockerfile) works the same way.

## Keeping it market-aware
Return assumptions live in `InvestmentService.ASSUMED_RETURNS` (and are shown in the UI). Tax slabs live in
`TaxService` constants. Update both each financial year; verify against current law.
Extension ideas: persistence (Postgres) + login, live rates/NAV provider, more tax jurisdictions,
Monte-Carlo projections, PDF report export.

## WebhookTrigger

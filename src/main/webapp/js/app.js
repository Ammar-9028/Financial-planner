(() => {
  'use strict';
  const $ = (s) => document.querySelector(s);
  let currency = 'INR', current = null, last = null, chart = null;

  const fmt = (n) => new Intl.NumberFormat(currency === 'INR' ? 'en-IN' : 'en-US',
    { style: 'currency', currency, maximumFractionDigits: 0 }).format(n ?? 0);
  const pct = (n) => `${Number(n).toFixed(1)}%`;
  const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const kv = (k, v, cls = '') => `<div class="kv ${cls}"><span>${k}</span><strong>${v}</strong></div>`;
  const tips = (a) => (a && a.length ? `<ul class="tips">${a.map((t) => `<li>${esc(t)}</li>`).join('')}</ul>` : '');
  const badge = (t, ok) => `<span class="badge ${ok ? 'good' : 'warn'}">${esc(t)}</span>`;
  const lineChart = (labels, sets) => ({ type: 'line', data: { labels, datasets: sets.map(([label, data]) => ({ label, data, tension: .25, pointRadius: 0 })) },
    options: { plugins: { legend: { position: 'bottom' } }, scales: { y: { ticks: { callback: (v) => fmt(v) } } } } });

  const MODULES = [
    { key: 'investment', title: 'Investments', blurb: 'Project SIP + lump-sum growth with annual step-up, in nominal and inflation-adjusted terms.',
      fields: [{ n: 'monthlySip', l: 'Monthly SIP', v: 25000 }, { n: 'lumpsum', l: 'Lump sum today', v: 100000 }, { n: 'years', l: 'Years', v: 15 },
        { n: 'annualReturnPct', l: 'Expected return % p.a.', v: 12 }, { n: 'stepUpPct', l: 'Annual SIP step-up %', v: 10 }, { n: 'inflationPct', l: 'Inflation %', v: 6 }],
      render: (r) => kv('Total invested', fmt(r.invested)) + kv('Estimated gains', fmt(r.gains)) + kv('Future value', fmt(r.futureValue), 'big') + kv('Value in today\'s money', fmt(r.realValue)),
      chart: (r) => lineChart(r.series.map((p) => `Y${p.year}`), [['Invested', r.series.map((p) => p.invested)], ['Value', r.series.map((p) => p.value)], ['Inflation-adjusted', r.series.map((p) => p.realValue)]]) },
    { key: 'allocation', title: 'Asset allocation', blurb: 'Risk- and horizon-based mix across equity, debt, gold, REITs and cash.',
      fields: [{ n: 'age', l: 'Age', v: 30 }, { n: 'riskProfile', l: 'Risk profile', v: 'MODERATE', o: ['CONSERVATIVE', 'MODERATE', 'AGGRESSIVE'] }, { n: 'horizonYears', l: 'Horizon (years)', v: 15 }],
      render: (r) => kv('Blended expected return', pct(r.blendedReturnPct), 'big')
        + `<table><tr><th>Asset class</th><th>%</th><th>Return</th><th>Vehicles</th></tr>${r.items.map((i) => `<tr><td>${esc(i.assetClass)}</td><td>${i.percent}%</td><td>${pct(i.expectedReturnPct)}</td><td>${esc(i.vehicles)}</td></tr>`).join('')}</table>`
        + tips([r.note]),
      chart: (r) => ({ type: 'doughnut', data: { labels: r.items.map((i) => i.assetClass), datasets: [{ data: r.items.map((i) => i.percent) }] }, options: { plugins: { legend: { position: 'right' } } } }) },
    { key: 'tax', title: 'Tax planning', blurb: 'Compare old vs new regime (India) and find deductions worth using.',
      fields: [{ n: 'grossIncome', l: 'Gross annual income', v: 1800000 }, { n: 'section80C', l: '80C invested', v: 150000 }, { n: 'section80D', l: '80D health premium', v: 25000 },
        { n: 'npsExtra', l: 'NPS 80CCD(1B)', v: 0 }, { n: 'homeLoanInterest', l: 'Home-loan interest', v: 0 }, { n: 'otherDeductions', l: 'Other old-regime deductions (HRA etc.)', v: 0 }],
      render: (r) => kv('Old regime tax', fmt(r.oldTax)) + kv('New regime tax', fmt(r.newTax)) + kv('Better option', badge(`${r.recommended} regime`, true))
        + kv('You save', fmt(r.saving), 'big') + tips(r.tips) },
    { key: 'savings', title: 'Savings & emergency fund', blurb: 'Savings rate, emergency-fund target and time to close the gap.',
      fields: [{ n: 'monthlyIncome', l: 'Monthly income', v: 150000 }, { n: 'monthlyExpenses', l: 'Monthly expenses', v: 70000 }, { n: 'monthlyEmi', l: 'Monthly EMIs', v: 20000 },
        { n: 'liquidSavings', l: 'Liquid savings', v: 200000 }, { n: 'dependents', l: 'Dependents', v: 1 }],
      render: (r) => kv('Monthly surplus', fmt(r.monthlySurplus)) + kv('Savings rate', `${pct(r.savingsRatePct)} ${badge(r.verdict, r.savingsRatePct >= 10)}`)
        + kv(`Emergency fund target (${r.emergencyMonths} months)`, fmt(r.emergencyTarget)) + kv('Gap', fmt(r.gap), 'big')
        + kv('Months to close gap', r.monthsToTarget < 0 ? 'Not reachable at current surplus' : r.monthsToTarget) + tips(r.tips) },
    { key: 'insurance', title: 'Insurance', blurb: 'Term life cover (human-life-value approach) and health cover guidance.',
      fields: [{ n: 'age', l: 'Age', v: 32 }, { n: 'retirementAge', l: 'Retirement age', v: 60 }, { n: 'annualIncome', l: 'Annual income', v: 1800000 }, { n: 'liabilities', l: 'Loans / liabilities', v: 2500000 },
        { n: 'existingLifeCover', l: 'Existing life cover', v: 5000000 }, { n: 'liquidAssets', l: 'Liquid assets', v: 500000 }, { n: 'familyMembers', l: 'Family members', v: 3 }, { n: 'existingHealthCover', l: 'Existing health cover', v: 500000 }],
      render: (r) => kv('Term cover needed', fmt(r.termCoverNeeded)) + kv('Term cover gap', fmt(r.termGap), 'big')
        + kv('Health cover recommended', fmt(r.healthCoverNeeded)) + kv('Health cover gap', fmt(r.healthGap), 'big') + tips(r.notes) },
    { key: 'retirement', title: 'Retirement & FIRE', blurb: 'Corpus needed, projected corpus, extra SIP to close the gap, and your FIRE (25x) number.',
      fields: [{ n: 'currentAge', l: 'Current age', v: 32 }, { n: 'retirementAge', l: 'Retirement age', v: 60 }, { n: 'lifeExpectancy', l: 'Plan till age', v: 85 }, { n: 'monthlyExpenseToday', l: 'Monthly expenses today', v: 70000 },
        { n: 'inflationPct', l: 'Inflation %', v: 6 }, { n: 'preReturnPct', l: 'Return before retirement %', v: 11 }, { n: 'postReturnPct', l: 'Return after retirement %', v: 8 }, { n: 'currentCorpus', l: 'Current corpus', v: 1500000 }, { n: 'monthlySip', l: 'Monthly SIP', v: 30000 }],
      render: (r) => kv('Monthly expense at retirement', fmt(r.monthlyExpenseAtRetirement)) + kv('Corpus needed', fmt(r.corpusNeeded)) + kv('Projected corpus', fmt(r.projectedCorpus))
        + kv('Status', badge(r.onTrack ? 'On track' : 'Shortfall', r.onTrack)) + kv('Extra monthly SIP needed', fmt(r.extraMonthlySipNeeded), 'big') + kv('FIRE number (today\'s money)', fmt(r.fireNumberToday)),
      chart: (r) => lineChart(r.series.map((p) => p.age), [['Projected corpus', r.series.map((p) => p.corpus)]]) },
    { key: 'goal', title: 'Goal planner', blurb: 'Child education, home, wedding, car: inflation-adjusted target and the SIP to reach it.',
      fields: [{ n: 'name', l: 'Goal', v: 'Child education', t: 'text' }, { n: 'targetToday', l: 'Cost in today\'s money', v: 3000000 }, { n: 'years', l: 'Years to goal', v: 12 },
        { n: 'inflationPct', l: 'Goal inflation %', v: 8 }, { n: 'returnPct', l: 'Expected return %', v: 11 }, { n: 'alreadySaved', l: 'Already saved', v: 200000 }],
      render: (r) => kv(esc(r.name) + ' cost at goal date', fmt(r.inflatedTarget)) + kv('Existing savings grow to', fmt(r.savedFutureValue)) + kv('Shortfall', fmt(r.shortfall))
        + kv('Monthly SIP needed', fmt(r.monthlySipNeeded), 'big') + kv('Or lump sum today', fmt(r.lumpsumNeededToday)) },
    { key: 'loan', title: 'Loan & prepayment', blurb: 'EMI, total interest and how much a monthly prepayment saves.',
      fields: [{ n: 'principal', l: 'Loan amount', v: 5000000 }, { n: 'annualRatePct', l: 'Interest % p.a.', v: 8.75 }, { n: 'years', l: 'Tenure (years)', v: 20 }, { n: 'extraMonthlyPrepay', l: 'Extra monthly prepayment', v: 5000 }],
      render: (r) => kv('EMI', fmt(r.emi), 'big') + kv('Total interest (no prepayment)', fmt(r.totalInterest)) + kv('Interest with prepayment', fmt(r.interestWithPrepay))
        + kv('Interest saved', fmt(r.interestSaved), 'big') + kv('Time saved', `${Math.floor(r.monthsSaved / 12)} yrs ${r.monthsSaved % 12} mo`) },
  ];

  const field = (f) => f.o
    ? `<label>${f.l}<select name="${f.n}">${f.o.map((o) => `<option ${o === f.v ? 'selected' : ''}>${o}</option>`).join('')}</select></label>`
    : `<label>${f.l}<input name="${f.n}" type="${f.t || 'number'}" value="${esc(f.v)}" step="any" required></label>`;

  function select(m) {
    current = m; last = null;
    document.querySelectorAll('#nav button').forEach((b) => b.classList.toggle('active', b.dataset.key === m.key));
    $('#title').textContent = m.title;
    $('#blurb').textContent = m.blurb;
    $('#form').innerHTML = m.fields.map(field).join('') + '<button class="go" type="submit">Calculate</button>';
    $('#out').hidden = true;
    drawChart(null);
  }

  function drawChart(cfg) {
    if (chart) { chart.destroy(); chart = null; }
    $('#chartWrap').hidden = !cfg || !window.Chart;
    if (cfg && window.Chart) chart = new window.Chart($('#chart'), cfg);
  }

  function show() {
    $('#out').hidden = false;
    $('#result').innerHTML = current.render(last);
    drawChart(current.chart ? current.chart(last) : null);
  }

  $('#form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = new FormData(e.target), body = {};
    for (const f of current.fields) body[f.n] = f.o || f.t === 'text' ? data.get(f.n) : Number(data.get(f.n));
    try {
      const res = await fetch(`api/${current.key}`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
      const json = await res.json();
      if (!res.ok) throw new Error(json.error || res.statusText);
      last = json; show();
    } catch (err) {
      $('#out').hidden = false; drawChart(null);
      $('#result').innerHTML = `<p class="err">${esc(err.message)}</p>`;
    }
  });

  $('#cur').addEventListener('change', (e) => { currency = e.target.value; if (last) show(); });

  $('#nav').innerHTML = MODULES.map((m) => `<button type="button" data-key="${m.key}">${m.title}</button>`).join('');
  $('#nav').addEventListener('click', (e) => { const b = e.target.closest('button'); if (b) select(MODULES.find((m) => m.key === b.dataset.key)); });
  select(MODULES[0]);

  fetch('api/assumptions').then((r) => r.json()).then((a) => {
    $('#assumeBody').innerHTML = `<p>${esc(a.note)} Inflation ${pct(a.inflationPct)}.</p><table>${Object.entries(a.expectedReturnsPct).map(([k, v]) => `<tr><td>${esc(k)}</td><td>${pct(v)}</td></tr>`).join('')}</table>`;
  }).catch(() => { $('#assume').hidden = true; });
})();

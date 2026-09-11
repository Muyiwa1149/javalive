const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const BASE = 'http://localhost:5173';
const SHOT_DIR = path.join(__dirname, 'screenshots', 'new-app');
fs.mkdirSync(SHOT_DIR, { recursive: true });

const userToken = fs.readFileSync(path.join(__dirname, 'user_token.txt'), 'utf8').trim();
const adminToken = fs.readFileSync(path.join(__dirname, 'admin_token.txt'), 'utf8').trim();

// Public pages (no auth)
const publicRoutes = [
  '/', '/about', '/why-us', '/trade', '/regulation', '/for-traders', '/automate', '/copy',
  '/faq', '/forex', '/etfs', '/shares', '/indices', '/cryptocurrencies', '/terms', '/privacy',
  '/contact', '/login', '/register', '/forgot-password', '/admin/login', '/admin/forgot-password',
];

// User dashboard pages (requires user auth)
const userRoutes = [
  '/dashboard', '/dashboard/history', '/dashboard/plans', '/dashboard/my-plans', '/dashboard/trade',
  '/dashboard/copy-trading', '/dashboard/bots', '/dashboard/signals', '/dashboard/signals/premium',
  '/dashboard/signals/external', '/dashboard/deposits', '/dashboard/withdrawals', '/dashboard/transfer',
  '/dashboard/exchange', '/dashboard/loans', '/dashboard/loans/history', '/dashboard/profile',
  '/dashboard/kyc', '/dashboard/wallet-connect', '/dashboard/referrals', '/dashboard/membership',
  '/dashboard/mt4', '/dashboard/support', '/dashboard/notifications',
];

// Admin dashboard pages (requires admin auth)
const adminRoutes = [
  '/admin/dashboard', '/admin/users', '/admin/kyc', '/admin/deposits', '/admin/withdrawals',
  '/admin/plans', '/admin/investments', '/admin/copy-trading', '/admin/copy-trading/active',
  '/admin/bots', '/admin/bots/analytics', '/admin/loans', '/admin/signals', '/admin/signals/active',
  '/admin/signals/subscribers', '/admin/signals/settings', '/admin/trading-accounts',
  '/admin/trading-accounts/fees', '/admin/membership', '/admin/wallet-connect',
  '/admin/wallet-connect/settings', '/admin/crm/new-task', '/admin/crm/tasks', '/admin/crm/my-tasks',
  '/admin/crm/leads', '/admin/crm/import', '/admin/email-services', '/admin/notifications',
  '/admin/content', '/admin/admins', '/admin/settings/app', '/admin/settings/referral',
  '/admin/settings/subscription', '/admin/settings/payment', '/admin/settings/crypto',
  '/admin/ip-blacklist', '/admin/profile',
];

function safeName(route) {
  return (route.replace(/^\//, '').replace(/\//g, '_') || 'home') + '.png';
}

async function visit(page, route, results) {
  const errors = [];
  const failedRequests = [];

  const consoleHandler = (msg) => {
    if (msg.type() === 'error') errors.push(msg.text());
  };
  const pageErrorHandler = (err) => errors.push('PAGE_ERROR: ' + err.message);
  const responseHandler = (resp) => {
    const url = resp.url();
    if (resp.status() >= 400 && (url.includes('/api/') || url.includes('localhost:8080'))) {
      failedRequests.push(`${resp.status()} ${url}`);
    }
  };

  page.on('console', consoleHandler);
  page.on('pageerror', pageErrorHandler);
  page.on('response', responseHandler);

  let navError = null;
  try {
    await page.goto(BASE + route, { waitUntil: 'networkidle', timeout: 20000 });
    await page.waitForTimeout(400);
  } catch (e) {
    navError = e.message;
  }

  let bodyText = '';
  try { bodyText = await page.locator('body').innerText(); } catch (e) { /* ignore */ }
  const looksBlank = bodyText.trim().length < 20;
  const hasVisibleError = /internal server error|cannot read propert|undefined is not|failed to fetch|\b500\b|\b404\b|application error/i.test(bodyText) && !route.includes('not-found');

  try {
    await page.screenshot({ path: path.join(SHOT_DIR, safeName(route)), fullPage: true });
  } catch (e) { /* ignore */ }

  page.off('console', consoleHandler);
  page.off('pageerror', pageErrorHandler);
  page.off('response', responseHandler);

  results.push({
    route, navError, looksBlank, hasVisibleError,
    consoleErrors: [...new Set(errors)].slice(0, 10),
    failedRequests: [...new Set(failedRequests)].slice(0, 10),
  });
}

(async () => {
  const browser = await chromium.launch();
  const results = [];

  // Public pages: fresh context, no auth
  {
    const context = await browser.newContext();
    const page = await context.newPage();
    for (const route of publicRoutes) {
      console.log('Visiting (public):', route);
      await visit(page, route, results);
    }
    await context.close();
  }

  // User pages: inject user_token into localStorage before navigating
  {
    const context = await browser.newContext();
    const page = await context.newPage();
    await page.goto(BASE + '/login');
    await page.evaluate((token) => localStorage.setItem('user_token', token), userToken);
    for (const route of userRoutes) {
      console.log('Visiting (user):', route);
      await visit(page, route, results);
    }
    await context.close();
  }

  // Admin pages: inject admin_token into localStorage before navigating
  {
    const context = await browser.newContext();
    const page = await context.newPage();
    await page.goto(BASE + '/admin/login');
    await page.evaluate((token) => localStorage.setItem('admin_token', token), adminToken);
    for (const route of adminRoutes) {
      console.log('Visiting (admin):', route);
      await visit(page, route, results);
    }
    await context.close();
  }

  await browser.close();

  fs.writeFileSync(path.join(__dirname, 'new-app-report.json'), JSON.stringify(results, null, 2));

  const problems = results.filter(r => r.navError || r.looksBlank || r.hasVisibleError || r.consoleErrors.length || r.failedRequests.length);
  console.log('\n=== SUMMARY ===');
  console.log('Total routes tested:', results.length);
  console.log('Routes with potential issues:', problems.length);
  for (const p of problems) {
    console.log('\n---', p.route, '---');
    if (p.navError) console.log('  navError:', p.navError);
    if (p.looksBlank) console.log('  looksBlank: true');
    if (p.hasVisibleError) console.log('  hasVisibleError: true');
    if (p.consoleErrors.length) console.log('  consoleErrors:', p.consoleErrors);
    if (p.failedRequests.length) console.log('  failedRequests:', p.failedRequests);
  }
})();

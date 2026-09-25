const { chromium } = require('playwright');

// The DISABLED_ROUTES check runs before the auth check in router.beforeEach, so navigating to a
// disabled route redirects to the dashboard route name unconditionally — which then itself
// redirects to login for an unauthenticated visitor. Either way, landing on a disabled route's own
// URL proves the guard did NOT fire; landing anywhere else (login or dashboard) proves it did.
const routes = [
  '/dashboard/loans', '/dashboard/loans/history',
  '/dashboard/signals', '/dashboard/signals/premium', '/dashboard/signals/external',
  '/dashboard/membership', '/dashboard/mt4',
  '/admin/loans',
  '/admin/signals', '/admin/signals/active', '/admin/signals/subscribers', '/admin/signals/settings',
  '/admin/trading-accounts', '/admin/trading-accounts/fees',
  '/admin/membership',
  '/admin/crm/new-task', '/admin/crm/tasks', '/admin/crm/my-tasks', '/admin/crm/leads', '/admin/crm/import',
];

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  for (const r of routes) {
    await page.goto('http://localhost:5173' + r, { waitUntil: 'load', timeout: 15000 });
    await page.waitForTimeout(300);
    const landedOnSamePath = page.url().endsWith(r);
    console.log((landedOnSamePath ? 'STILL ACCESSIBLE - ' : 'blocked OK -        ') + r + ' -> ' + page.url());
  }
  await browser.close();
})();

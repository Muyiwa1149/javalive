const { chromium } = require('playwright');
const fs = require('fs');

const routes = [
  '/dashboard', '/dashboard/history', '/dashboard/plans', '/dashboard/my-plans', '/dashboard/trade',
  '/dashboard/copy-trading', '/dashboard/bots', '/dashboard/signals', '/dashboard/signals/premium',
  '/dashboard/signals/external', '/dashboard/deposits', '/dashboard/deposits/history', '/dashboard/withdrawals',
  '/dashboard/transfer', '/dashboard/exchange', '/dashboard/loans', '/dashboard/loans/history',
  '/dashboard/profile', '/dashboard/kyc', '/dashboard/wallet-connect', '/dashboard/referrals',
  '/dashboard/membership', '/dashboard/mt4', '/dashboard/support', '/dashboard/notifications',
];

(async () => {
  const token = fs.readFileSync(__dirname + '/user_token.txt', 'utf8').trim();
  const browser = await chromium.launch();
  const context = await browser.newContext({ viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true });
  const page = await context.newPage();
  await page.goto('http://localhost:5173/login');
  await page.evaluate((t) => localStorage.setItem('user_token', t), token);

  const results = [];
  for (const route of routes) {
    await page.goto('http://localhost:5173' + route, { waitUntil: 'networkidle', timeout: 20000 }).catch(() => {});
    await page.waitForTimeout(400);
    const scrollables = await page.evaluate(() => {
      const found = [];
      document.querySelectorAll('body *').forEach((el) => {
        const style = getComputedStyle(el);
        const canScrollX = /(auto|scroll)/.test(style.overflowX);
        if (canScrollX && el.scrollWidth > el.clientWidth + 1) {
          found.push({
            tag: el.tagName, cls: (el.className || '').toString().slice(0, 80),
            scrollWidth: el.scrollWidth, clientWidth: el.clientWidth,
            visible: el.offsetParent !== null,
          });
        }
      });
      return found;
    });
    const visibleOffenders = scrollables.filter((s) => s.visible);
    if (visibleOffenders.length) {
      results.push({ route, visibleOffenders });
    }
  }
  console.log(JSON.stringify(results, null, 2));
  await browser.close();
})();

const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const BASE = 'http://localhost:5173';
const SHOT_DIR = path.join(__dirname, 'screenshots', 'mobile');
fs.mkdirSync(SHOT_DIR, { recursive: true });

const userToken = fs.readFileSync(path.join(__dirname, 'user_token.txt'), 'utf8').trim();

const userRoutes = [
  '/dashboard', '/dashboard/history', '/dashboard/plans', '/dashboard/my-plans', '/dashboard/trade',
  '/dashboard/copy-trading', '/dashboard/bots', '/dashboard/signals', '/dashboard/signals/premium',
  '/dashboard/signals/external', '/dashboard/deposits', '/dashboard/deposits/history', '/dashboard/withdrawals',
  '/dashboard/transfer', '/dashboard/exchange', '/dashboard/loans', '/dashboard/loans/history',
  '/dashboard/profile', '/dashboard/kyc', '/dashboard/wallet-connect', '/dashboard/referrals',
  '/dashboard/membership', '/dashboard/mt4', '/dashboard/support', '/dashboard/notifications',
];

function safeName(route) {
  return (route.replace(/^\//, '').replace(/\//g, '_') || 'home') + '.png';
}

async function checkOverflow(page) {
  return page.evaluate(() => {
    const docWidth = document.documentElement.scrollWidth;
    const viewWidth = document.documentElement.clientWidth;
    const overflowing = [];
    if (docWidth > viewWidth + 2) {
      const all = document.querySelectorAll('body *');
      for (const el of all) {
        if (el.scrollWidth > viewWidth + 2) {
          const rect = el.getBoundingClientRect();
          if (rect.width > 0) {
            overflowing.push({
              tag: el.tagName, cls: (el.className || '').toString().slice(0, 60),
              scrollWidth: el.scrollWidth,
            });
          }
        }
      }
    }
    return { docWidth, viewWidth, hasOverflow: docWidth > viewWidth + 2, culprits: overflowing.slice(0, 5) };
  });
}

(async () => {
  const browser = await chromium.launch();
  const context = await browser.newContext({ viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true });
  const page = await context.newPage();
  await page.goto(BASE + '/login');
  await page.evaluate((t) => localStorage.setItem('user_token', t), userToken);

  const results = [];
  for (const route of userRoutes) {
    console.log('Visiting:', route);
    try {
      await page.goto(BASE + route, { waitUntil: 'domcontentloaded', timeout: 15000 });
      await page.waitForTimeout(700);
      const overflow = await checkOverflow(page);
      await page.screenshot({ path: path.join(SHOT_DIR, safeName(route)), fullPage: true });
      results.push({ route, ...overflow });
    } catch (e) {
      results.push({ route, error: e.message });
    }
  }

  await browser.close();
  fs.writeFileSync(path.join(__dirname, 'mobile-report.json'), JSON.stringify(results, null, 2));
  console.log('\n=== OVERFLOW SUMMARY ===');
  for (const r of results) {
    if (r.hasOverflow || r.error) {
      console.log(r.route, '-', r.error || `doc:${r.docWidth} view:${r.viewWidth}`, r.culprits || '');
    }
  }
})();

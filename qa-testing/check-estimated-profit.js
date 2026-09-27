const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 500, height: 1400 } });
  await page.goto('http://localhost:5176/login', { waitUntil: 'load', timeout: 20000 });
  await page.fill('input[type="email"], input[name="email"]', 'demo.user@keystonebitfx.test');
  await page.fill('input[type="password"], input[name="password"]', 'Velnora123!');
  await Promise.all([
    page.waitForResponse((res) => res.url().includes('/api/auth/login')),
    page.click('button[type="submit"]'),
  ]);
  await page.waitForTimeout(1200);
  await page.goto('http://localhost:5176/dashboard/my-plans', { waitUntil: 'load', timeout: 20000 });
  await page.waitForTimeout(1000);

  // API returns id=58 (expired) first, id=57 (active) second — click the 2nd "View Details" button.
  const buttons = await page.getByRole('button', { name: 'View Details' }).all();
  console.log('View Details buttons found:', buttons.length);
  await buttons[1].click();
  await page.waitForTimeout(800);

  const text = await page.evaluate(() => document.body.innerText);
  console.log('Contains "accruing":', text.includes('accruing'));
  const match = text.match(/≈[^\n]*accruing/);
  console.log('Matched line:', match ? match[0] : '(none found)');
  await page.screenshot({ path: 'estimated-profit-check.png', fullPage: true });
  await browser.close();
})();

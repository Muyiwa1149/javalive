const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });

  await page.goto('http://localhost:5175/login', { waitUntil: 'load', timeout: 20000 });
  await page.fill('input[type="email"], input[name="email"]', 'demo.user@keystonebitfx.test');
  await page.fill('input[type="password"], input[name="password"]', 'Velnora123!');
  await Promise.all([
    page.waitForResponse((res) => res.url().includes('/api/auth/login')),
    page.click('button[type="submit"]'),
  ]);
  await page.waitForTimeout(1200);

  await page.goto('http://localhost:5175/dashboard', { waitUntil: 'load', timeout: 20000 });
  await page.waitForTimeout(800);
  await page.screenshot({ path: 'header-desktop-check.png', clip: { x: 0, y: 0, width: 1440, height: 100 } });
  console.log('screenshot saved');
  await browser.close();
})();

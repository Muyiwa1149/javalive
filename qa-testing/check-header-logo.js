const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true });

  await page.goto('http://localhost:5175/login', { waitUntil: 'load', timeout: 20000 });
  await page.fill('input[type="email"], input[name="email"]', 'demo.user@keystonebitfx.test');
  await page.fill('input[type="password"], input[name="password"]', 'Velnora123!');
  await Promise.all([
    page.waitForResponse((res) => res.url().includes('/api/auth/login')),
    page.click('button[type="submit"]'),
  ]);
  await page.waitForTimeout(1200);
  console.log('After login:', page.url());

  await page.goto('http://localhost:5175/dashboard', { waitUntil: 'load', timeout: 20000 });
  await page.waitForTimeout(800);
  await page.screenshot({ path: 'header-logo-check.png' });
  console.log('screenshot saved');
  await browser.close();
})();

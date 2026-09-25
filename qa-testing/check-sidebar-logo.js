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
  await page.screenshot({ path: 'sidebar-logo-check.png', clip: { x: 0, y: 0, width: 288, height: 140 } });

  const mobilePage = await browser.newPage({ viewport: { width: 390, height: 844 }, isMobile: true });
  await mobilePage.goto('http://localhost:5175/login', { waitUntil: 'load', timeout: 20000 });
  await mobilePage.fill('input[type="email"], input[name="email"]', 'demo.user@keystonebitfx.test');
  await mobilePage.fill('input[type="password"], input[name="password"]', 'Velnora123!');
  await Promise.all([
    mobilePage.waitForResponse((res) => res.url().includes('/api/auth/login')),
    mobilePage.click('button[type="submit"]'),
  ]);
  await mobilePage.waitForTimeout(1200);
  await mobilePage.goto('http://localhost:5175/dashboard', { waitUntil: 'load', timeout: 20000 });
  await mobilePage.waitForTimeout(800);
  await mobilePage.screenshot({ path: 'mobile-header-logo-check.png', clip: { x: 0, y: 0, width: 390, height: 80 } });

  console.log('screenshots saved');
  await browser.close();
})();

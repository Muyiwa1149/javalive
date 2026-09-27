const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 420, height: 800 } });
  await page.goto('http://localhost:5176/admin/login', { waitUntil: 'load', timeout: 20000 });
  await page.waitForTimeout(1000);
  await page.screenshot({ path: 'admin-login-mobile.png' });

  const page2 = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  await page2.goto('http://localhost:5176/admin/login', { waitUntil: 'load', timeout: 20000 });
  await page2.waitForTimeout(1000);
  await page2.screenshot({ path: 'admin-login-desktop.png' });
  await browser.close();
})();

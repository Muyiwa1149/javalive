const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  await page.goto('https://velnorapartners.com/', { waitUntil: 'load', timeout: 30000 });
  await page.waitForTimeout(3000);
  await page.screenshot({ path: 'qa-testing/vps-header-crop.png', clip: { x: 0, y: 0, width: 300, height: 64 } });
  await browser.close();
})();

const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  await page.goto('https://velnorapartners.com/', { waitUntil: 'load', timeout: 30000 });
  await page.waitForTimeout(1500);
  const bodyText = await page.evaluate(() => document.body.innerText);
  console.log('Contains EUR/USD text:', bodyText.includes('EUR/USD'));
  await page.screenshot({ path: 'live-home-bottom.png', clip: { x: 0, y: 800, width: 1440, height: 100 } });
  console.log('done');
  await browser.close();
})();

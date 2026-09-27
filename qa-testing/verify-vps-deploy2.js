const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  page.on('console', msg => console.log('CONSOLE:', msg.type(), msg.text()));
  page.on('requestfailed', req => console.log('FAILED REQUEST:', req.url(), req.failure()?.errorText));
  page.on('response', res => { if (res.url().includes('logo') || res.url().includes('settings')) console.log('RESPONSE:', res.status(), res.url()); });
  await page.goto('https://velnorapartners.com/', { waitUntil: 'networkidle', timeout: 30000 });
  await page.waitForTimeout(1000);
  await page.screenshot({ path: 'qa-testing/vps-live-homepage2.png' });
  await browser.close();
})();

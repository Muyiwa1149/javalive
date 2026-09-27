const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  await page.goto('https://velnorapartners.com/', { waitUntil: 'load', timeout: 30000 });
  await page.waitForTimeout(3000);
  const handle = await page.$('header img, nav img, a.flex-shrink-0 img');
  await handle.screenshot({ path: 'qa-testing/vps-logo-element-only.png' });

  // also render it bigger for visual sanity check
  await page.evaluate(() => {
    const img = document.querySelector('a.flex-shrink-0 img');
    if (img) img.style.height = '200px';
  });
  await page.waitForTimeout(300);
  const handle2 = await page.$('a.flex-shrink-0 img');
  await handle2.screenshot({ path: 'qa-testing/vps-logo-element-big.png' });
  await browser.close();
})();

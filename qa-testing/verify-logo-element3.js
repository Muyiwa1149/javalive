const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  await page.goto('https://velnorapartners.com/', { waitUntil: 'load', timeout: 30000 });
  await page.waitForTimeout(3000);
  const imgs = await page.$$('img');
  console.log('total imgs:', imgs.length);
  for (const img of imgs) {
    const src = await img.getAttribute('src');
    if (src && src.includes('storage/photos')) {
      const box = await img.boundingBox();
      console.log('found', src, JSON.stringify(box));
      await img.screenshot({ path: `qa-testing/vps-logo-${Math.round(box.y)}.png` });
    }
  }
  await browser.close();
})();

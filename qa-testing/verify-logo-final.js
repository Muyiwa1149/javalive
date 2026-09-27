const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  await page.goto('https://velnorapartners.com/', { waitUntil: 'load', timeout: 30000 });
  await page.waitForTimeout(5000);
  const state = await page.evaluate(() => {
    const img = [...document.querySelectorAll('img')].find(i => i.src.includes('storage/photos') && i.getBoundingClientRect().y < 100);
    if (!img) return 'not found';
    return { complete: img.complete, naturalWidth: img.naturalWidth, naturalHeight: img.naturalHeight, rect: img.getBoundingClientRect().toJSON ? img.getBoundingClientRect() : null, cssWidth: getComputedStyle(img).width, cssHeight: getComputedStyle(img).height };
  });
  console.log('STATE:', JSON.stringify(state));
  const imgs = await page.$$('img');
  for (const img of imgs) {
    const src = await img.getAttribute('src');
    const box = await img.boundingBox();
    if (src && src.includes('storage/photos') && box && box.y < 100) {
      console.log('boundingBox now:', JSON.stringify(box));
      await img.screenshot({ path: 'qa-testing/vps-logo-final.png' });
    }
  }
  await browser.close();
})();

const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  await page.goto('https://velnorapartners.com/', { waitUntil: 'load', timeout: 30000 });
  await page.waitForTimeout(3000);
  const handle = await page.evaluateHandle(() => [...document.querySelectorAll('img')].find(i => i.src.includes('storage/photos') && i.className.includes('h-14')));
  await handle.asElement().screenshot({ path: 'qa-testing/vps-logo-element-only.png' });

  await page.evaluate(() => {
    const img = [...document.querySelectorAll('img')].find(i => i.src.includes('storage/photos') && i.className.includes('h-14'));
    img.style.height = '200px';
  });
  await page.waitForTimeout(300);
  const handle2 = await page.evaluateHandle(() => [...document.querySelectorAll('img')].find(i => i.src.includes('storage/photos') && i.style.height === '200px'));
  await handle2.asElement().screenshot({ path: 'qa-testing/vps-logo-element-big.png' });
  await browser.close();
})();

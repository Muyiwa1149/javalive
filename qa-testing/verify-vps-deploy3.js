const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  page.on('requestfailed', req => console.log('FAILED REQUEST:', req.url(), req.failure()?.errorText));
  page.on('response', res => { if (res.url().includes('/api/') && res.url().includes('setting')) console.log('RESPONSE:', res.status(), res.url()); });
  await page.goto('https://velnorapartners.com/', { waitUntil: 'load', timeout: 30000 });
  await page.waitForTimeout(4000);
  await page.screenshot({ path: 'qa-testing/vps-live-homepage3.png' });
  const logoSrc = await page.evaluate(() => {
    const img = document.querySelector('header img, nav img');
    return img ? { src: img.src, w: img.naturalWidth, h: img.naturalHeight, cls: img.className, visible: img.offsetHeight } : 'no img found';
  });
  console.log('LOGO INFO:', JSON.stringify(logoSrc));
  await browser.close();
})();

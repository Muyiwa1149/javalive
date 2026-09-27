const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  await page.goto('https://velnorapartners.com/', { waitUntil: 'load', timeout: 30000 });
  await page.waitForTimeout(3000);
  const info = await page.evaluate(() => {
    const imgs = [...document.querySelectorAll('img')].filter(i => i.src.includes('storage/photos'));
    return imgs.map(img => {
      const r = img.getBoundingClientRect();
      return { src: img.src, className: img.className, rect: { x: r.x, y: r.y, w: r.width, h: r.height }, parentTag: img.parentElement.tagName, parentClass: img.parentElement.className, grandparentClass: img.parentElement.parentElement?.className };
    });
  });
  console.log(JSON.stringify(info, null, 2));
  await page.screenshot({ path: 'qa-testing/vps-header-wide.png', clip: { x: 0, y: 0, width: 400, height: 200 } });
  await browser.close();
})();

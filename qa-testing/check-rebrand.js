const { chromium } = require('playwright');

const pages = ['/', '/about', '/faq', '/terms', '/trade', '/why-us'];

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  for (const path of pages) {
    const errors = [];
    page.removeAllListeners('pageerror');
    page.removeAllListeners('console');
    page.on('pageerror', (e) => errors.push('pageerror: ' + e.message));
    page.on('console', (msg) => { if (msg.type() === 'error') errors.push('console.error: ' + msg.text()); });
    await page.goto('http://localhost:5175' + path, { waitUntil: 'load', timeout: 20000 });
    await page.waitForTimeout(800);
    const title = await page.title();
    const bodyText = await page.evaluate(() => document.body.innerText);
    const hasOldBrand = /keystone/i.test(bodyText) || /keystone/i.test(title);
    console.log(`${path} -> title="${title}" oldBrandFound=${hasOldBrand} jsErrors=${errors.length}`);
    errors.forEach((e) => console.log('  ' + e));
  }
  await browser.close();
})();

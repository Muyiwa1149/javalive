const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  const errors = [];
  page.on('pageerror', (e) => errors.push('pageerror: ' + e.message));
  page.on('console', (msg) => { if (msg.type() === 'error') errors.push('console.error: ' + msg.text()); });

  for (const url of ['http://localhost:5173/', 'http://localhost:5173/login', 'http://localhost:5173/admin/login']) {
    await page.goto(url, { waitUntil: 'load', timeout: 20000 });
    await page.waitForTimeout(1000);
  }

  console.log('Errors found:', errors.length);
  errors.forEach((e) => console.log(e));
  await browser.close();
})();

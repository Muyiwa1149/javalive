const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  const errors = [];
  page.on('pageerror', (e) => errors.push('pageerror: ' + e.message));
  page.on('console', (msg) => { if (msg.type() === 'error') errors.push('console.error: ' + msg.text()); });

  // No session — the auth guard bounces this to admin.login. That's fine, this only checks the
  // EmailServices.vue chunk itself has no import/syntax/runtime error when it's fetched and parsed.
  await page.goto('http://localhost:5173/admin/email-services', { waitUntil: 'load', timeout: 20000 });
  await page.waitForTimeout(1000);
  console.log('Landed on:', page.url());
  console.log('Errors:', errors.length);
  errors.forEach((e) => console.log(e));
  await browser.close();
})();

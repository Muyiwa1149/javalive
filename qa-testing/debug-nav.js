const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  await page.goto('http://localhost:5175/admin/login', { waitUntil: 'load', timeout: 20000 });
  await page.waitForTimeout(1000);
  console.log('URL:', page.url());
  console.log('Title:', await page.title());
  const inputs = await page.locator('input').all();
  for (const i of inputs) {
    console.log('input:', await i.getAttribute('type'), await i.getAttribute('name'), await i.getAttribute('placeholder'));
  }
  await browser.close();
})();

const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  await page.goto('http://127.0.0.1:8000/', { waitUntil: 'domcontentloaded', timeout: 45000 });
  console.log('TITLE:', await page.title());
  await browser.close();
  console.log('OK');
})();

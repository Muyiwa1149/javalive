const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  console.log('Browser Date.now():', await page.evaluate(() => new Date().toString()));
  await browser.close();
})();

const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const token = fs.readFileSync(path.join(__dirname, 'user_token.txt'), 'utf8').trim();

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  const errors = [];
  page.on('console', (m) => { if (m.type() === 'error') errors.push(m.text()); });
  await page.goto('http://localhost:5173/login');
  await page.evaluate((t) => localStorage.setItem('user_token', t), token);
  await page.goto('http://localhost:5173/dashboard/profile', { waitUntil: 'networkidle', timeout: 20000 });
  await page.waitForTimeout(1000);
  const ticker = await page.locator('text=BTC:').first().innerText().catch(() => 'NOT FOUND');
  console.log('Ticker text near BTC:', ticker);
  console.log('Console errors:', errors);
  await browser.close();
})();

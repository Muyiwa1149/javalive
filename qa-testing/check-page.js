const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const route = process.argv[2];
const isAdmin = route.startsWith('/admin');
const tokenFile = isAdmin ? 'admin_token.txt' : 'user_token.txt';
const token = fs.readFileSync(path.join(__dirname, tokenFile), 'utf8').trim();
const storageKey = isAdmin ? 'admin_token' : 'user_token';
const loginPage = isAdmin ? '/admin/login' : '/login';

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  await page.goto('http://localhost:5173' + loginPage);
  await page.evaluate(([k, t]) => localStorage.setItem(k, t), [storageKey, token]);
  await page.goto('http://localhost:5173' + route, { waitUntil: 'networkidle', timeout: 20000 });
  await page.waitForTimeout(500);
  const text = await page.locator('body').innerText();
  console.log(text.slice(0, 3000));
  await browser.close();
})();

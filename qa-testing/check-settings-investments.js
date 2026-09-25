const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });

  const tokenFile = path.join(__dirname, 'admin_token.txt');
  let token = fs.readFileSync(tokenFile, 'utf8').trim();

  await page.goto('http://localhost:5173/admin/login');
  await page.evaluate((t) => localStorage.setItem('admin_token', t), token);
  await page.goto('http://localhost:5173/admin/settings/app', { waitUntil: 'networkidle', timeout: 20000 });
  await page.waitForTimeout(500);

  let bodyText = await page.locator('body').innerText();
  if (bodyText.includes('Sign in') || bodyText.includes('Login')) {
    console.log('Token expired, logging in fresh...');
    await page.goto('http://localhost:5173/admin/login');
    await page.fill('input[type="email"]', 'admin@javalive.local');
    await page.fill('input[type="password"]', 'password');
    await page.click('button[type="submit"]');
    await page.waitForTimeout(1500);
    token = await page.evaluate(() => localStorage.getItem('admin_token'));
    if (token) fs.writeFileSync(tokenFile, token);
    await page.goto('http://localhost:5173/admin/settings/app', { waitUntil: 'networkidle', timeout: 20000 });
    await page.waitForTimeout(500);
  }

  console.log('=== SettingsApp.vue screenshot ===');
  await page.screenshot({ path: path.join(__dirname, 'screenshots', 'settings-app-desktop.png'), fullPage: true });
  bodyText = await page.locator('body').innerText();
  console.log(bodyText.slice(0, 1500));

  console.log('=== Investments.vue ===');
  await page.goto('http://localhost:5173/admin/investments', { waitUntil: 'networkidle', timeout: 20000 });
  await page.waitForTimeout(500);
  await page.screenshot({ path: path.join(__dirname, 'screenshots', 'investments-desktop.png'), fullPage: true });
  bodyText = await page.locator('body').innerText();
  console.log(bodyText.slice(0, 1500));

  // Try opening the edit modal on the first row, if any
  const editBtn = page.locator('button:has-text("Edit")').first();
  const count = await editBtn.count();
  console.log('Edit buttons found:', count);
  if (count > 0) {
    await editBtn.click();
    await page.waitForTimeout(400);
    await page.screenshot({ path: path.join(__dirname, 'screenshots', 'investments-edit-modal.png') });
    const modalText = await page.locator('body').innerText();
    console.log('=== Modal open text snippet ===');
    console.log(modalText.slice(0, 1500));
  }

  await browser.close();
})();

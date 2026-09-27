const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  page.on('response', (res) => { if (res.status() >= 400) console.log('[bad response]', res.status(), res.request().method(), res.url()); });

  await page.goto('http://localhost:5176/admin/login', { waitUntil: 'load', timeout: 20000 });
  await page.fill('input[type="email"], input[name="email"]', 'demo.admin@keystonebitfx.test');
  await page.fill('input[type="password"], input[name="password"]', 'Velnora123!');
  await Promise.all([
    page.waitForResponse((res) => res.url().includes('/api/admin/auth/login')),
    page.click('button[type="submit"]'),
  ]);
  await page.waitForTimeout(1000);

  // --- Deposits ---
  await page.goto('http://localhost:5176/admin/deposits', { waitUntil: 'load', timeout: 20000 });
  await page.waitForTimeout(1000);
  await page.click('table tbody tr:first-child button[title="Edit"]');
  await page.waitForTimeout(300);
  await page.fill('input[type="number"]', '9999.5');
  const [saveResp] = await Promise.all([
    page.waitForResponse((res) => res.url().includes('/api/admin/deposits/') && res.request().method() === 'PUT'),
    page.click('button:has-text("Save")'),
  ]);
  console.log('DEPOSIT SAVE STATUS:', saveResp.status());
  console.log('DEPOSIT SAVE BODY:', (await saveResp.text()).slice(0, 300));
  await page.waitForTimeout(800);

  // --- Withdrawals ---
  await page.goto('http://localhost:5176/admin/withdrawals', { waitUntil: 'load', timeout: 20000 });
  await page.waitForTimeout(1000);
  await page.click('table tbody tr:first-child button[title="Edit"]');
  await page.waitForTimeout(300);
  const amountInputs = await page.locator('input[type="number"]').all();
  await amountInputs[0].fill('8888.5');
  const [saveResp2] = await Promise.all([
    page.waitForResponse((res) => res.url().includes('/api/admin/withdrawals/') && res.request().method() === 'PUT'),
    page.click('button:has-text("Save")'),
  ]);
  console.log('WITHDRAWAL SAVE STATUS:', saveResp2.status());
  console.log('WITHDRAWAL SAVE BODY:', (await saveResp2.text()).slice(0, 300));

  await browser.close();
})();

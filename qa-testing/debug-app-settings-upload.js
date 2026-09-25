const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

(async () => {
  // A tiny 1x1 PNG to use as both logo and favicon uploads.
  const pngBytes = Buffer.from(
    'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=',
    'base64'
  );
  const tmpFile = path.join(__dirname, 'test-icon.png');
  fs.writeFileSync(tmpFile, pngBytes);

  const browser = await chromium.launch();
  const page = await browser.newPage();

  page.on('response', (res) => {
    if (res.status() >= 400) console.log('[response]', res.status(), res.request().method(), res.url());
  });

  // Log in as admin first.
  await page.goto('http://localhost:5175/admin/login', { waitUntil: 'load', timeout: 20000 });
  await page.fill('input[type="email"], input[name="email"]', 'demo.admin@keystonebitfx.test');
  await page.fill('input[type="password"], input[name="password"]', 'Velnora123!');
  await Promise.all([
    page.waitForResponse((res) => res.url().includes('/api/admin/auth/login')),
    page.click('button[type="submit"]'),
  ]);
  await page.waitForTimeout(1000);
  console.log('After login:', page.url());

  await page.goto('http://localhost:5175/admin/settings/app', { waitUntil: 'load', timeout: 20000 });
  await page.waitForTimeout(1000);

  const fileInputs = await page.locator('input[type="file"]').all();
  console.log('file inputs found:', fileInputs.length);
  if (fileInputs.length >= 1) await fileInputs[0].setInputFiles(tmpFile);
  if (fileInputs.length >= 2) await fileInputs[1].setInputFiles(tmpFile);

  const [saveResponse] = await Promise.all([
    page.waitForResponse((res) => res.url().includes('/admin/settings/app'), { timeout: 15000 }).catch(() => null),
    page.click('button[type="submit"]'),
  ]);

  if (saveResponse) {
    console.log('SAVE STATUS:', saveResponse.status());
    console.log('SAVE BODY:', await saveResponse.text().catch(() => '<unreadable>'));
  } else {
    console.log('No save response observed.');
  }

  await page.waitForTimeout(1000);
  await browser.close();
  fs.unlinkSync(tmpFile);
})();

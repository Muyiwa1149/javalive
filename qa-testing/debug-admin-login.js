const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();

  page.on('console', (msg) => console.log('[console]', msg.type(), msg.text()));
  page.on('requestfailed', (req) => console.log('[requestfailed]', req.method(), req.url(), req.failure()?.errorText));
  page.on('response', (res) => {
    if (res.status() >= 400) console.log('[response]', res.status(), res.request().method(), res.url());
  });

  await page.goto('http://localhost:5175/admin/login', { waitUntil: 'load', timeout: 20000 });
  await page.waitForTimeout(500);

  await page.fill('input[type="email"], input[name="email"]', 'demo.admin@keystonebitfx.test');
  await page.fill('input[type="password"], input[name="password"]', 'Velnora123!');

  const [response] = await Promise.all([
    page.waitForResponse((res) => res.url().includes('/api/admin/auth/login'), { timeout: 15000 }).catch(() => null),
    page.click('button[type="submit"]'),
  ]);

  if (response) {
    console.log('LOGIN RESPONSE STATUS:', response.status());
    console.log('LOGIN RESPONSE BODY:', await response.text().catch(() => '<unreadable>'));
    console.log('LOGIN REQUEST HEADERS:', JSON.stringify(response.request().headers()));
  } else {
    console.log('No /api/admin/auth/login response observed within timeout.');
  }

  await page.waitForTimeout(1500);
  console.log('Final URL:', page.url());
  await browser.close();
})();

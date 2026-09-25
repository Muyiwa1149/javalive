const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();

  // --- User side ---
  {
    const page = await browser.newPage();
    page.on('console', (msg) => console.log('  [console]', msg.type(), msg.text()));
    page.on('pageerror', (e) => console.log('  [pageerror]', e.message));
    page.on('response', (res) => {
      if (res.url().includes('/api/')) console.log('  [response]', res.status(), res.url());
    });
    await page.goto('http://localhost:5173/login');
    // Deliberately invalid token — simulates an expired/garbage session. The backend will reject
    // it with 401 exactly as it would a genuinely expired JWT; this never grants real access.
    await page.evaluate(() => localStorage.setItem('user_token', 'garbage.invalid.token'));
    await page.goto('http://localhost:5173/dashboard', { waitUntil: 'load', timeout: 20000 });
    await page.waitForTimeout(2000);
    console.log('USER  - URL after hitting a protected page with a bad token:', page.url());
    console.log('USER  - localStorage user_token after:', await page.evaluate(() => localStorage.getItem('user_token')));
    await page.close();
  }

  // --- Admin side ---
  {
    const page = await browser.newPage();
    await page.goto('http://localhost:5173/admin/login');
    await page.evaluate(() => localStorage.setItem('admin_token', 'garbage.invalid.token'));
    await page.goto('http://localhost:5173/admin/dashboard', { waitUntil: 'load', timeout: 20000 });
    await page.waitForTimeout(2000);
    console.log('ADMIN - URL after hitting a protected page with a bad token:', page.url());
    console.log('ADMIN - localStorage admin_token after:', await page.evaluate(() => localStorage.getItem('admin_token')));
    await page.close();
  }

  await browser.close();
})();

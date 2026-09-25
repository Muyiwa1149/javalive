const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();

  const page = await browser.newPage();
  await page.goto('http://localhost:5173/login');
  await page.fill('input[type="email"]', 'nonexistent@example.com');
  await page.fill('input[type="password"]', 'wrongpassword123');
  await page.click('button[type="submit"]');
  await page.waitForTimeout(1500);
  console.log('USER login page URL after wrong credentials:', page.url());
  const bodyText = await page.locator('body').innerText();
  console.log('USER error shown:', /invalid|incorrect|not found/i.test(bodyText));
  await page.close();

  const page2 = await browser.newPage();
  await page2.goto('http://localhost:5173/admin/login');
  await page2.fill('input[type="email"]', 'nonexistent@example.com');
  await page2.fill('input[type="password"]', 'wrongpassword123');
  await page2.click('button[type="submit"]');
  await page2.waitForTimeout(1500);
  console.log('ADMIN login page URL after wrong credentials:', page2.url());
  const bodyText2 = await page2.locator('body').innerText();
  console.log('ADMIN error shown:', /invalid|incorrect|not found/i.test(bodyText2));
  await page2.close();

  await browser.close();
})();

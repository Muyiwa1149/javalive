const { chromium } = require('playwright');
const fs = require('fs');

(async () => {
  const token = fs.readFileSync(__dirname + '/user_token.txt', 'utf8').trim();
  const browser = await chromium.launch();
  const context = await browser.newContext({ viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true });
  const page = await context.newPage();
  await page.goto('http://localhost:5173/login');
  await page.evaluate((t) => localStorage.setItem('user_token', t), token);
  await page.goto('http://localhost:5173/dashboard', { waitUntil: 'domcontentloaded', timeout: 15000 });
  await page.waitForTimeout(800);

  // 1. Topbar logo next to hamburger
  await page.screenshot({ path: 'screenshots/topbar-with-logo.png' });

  // 2. Open sidebar, screenshot
  await page.click('button:has(svg.lucide-menu)');
  await page.waitForTimeout(400);
  await page.screenshot({ path: 'screenshots/sidebar-open.png' });

  // 3. Scroll the sidebar nav down, confirm logo header stays fixed
  await page.evaluate(() => {
    const nav = document.querySelector('aside nav');
    if (nav) nav.scrollTop = 400;
  });
  await page.waitForTimeout(300);
  await page.screenshot({ path: 'screenshots/sidebar-scrolled.png' });

  // 4. Click a nav link, confirm sidebar auto-closes
  await page.click('aside nav a:has-text("Investment Plans")');
  await page.waitForTimeout(500);
  await page.screenshot({ path: 'screenshots/sidebar-after-nav-click.png' });
  const sidebarClosed = await page.evaluate(() => {
    const aside = document.querySelector('aside');
    return aside.className.includes('-translate-x-full');
  });
  console.log('Sidebar closed after nav click:', sidebarClosed);
  console.log('Current URL:', page.url());

  await browser.close();
})();

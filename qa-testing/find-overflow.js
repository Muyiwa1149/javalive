const { chromium } = require('playwright');
const fs = require('fs');

const route = process.argv[2];
const token = fs.readFileSync(__dirname + '/user_token.txt', 'utf8').trim();

(async () => {
  const browser = await chromium.launch();
  const context = await browser.newContext({ viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true });
  const page = await context.newPage();
  await page.goto('http://localhost:5173/login');
  await page.evaluate((t) => localStorage.setItem('user_token', t), token);
  await page.goto('http://localhost:5173' + route, { waitUntil: 'domcontentloaded', timeout: 15000 });
  await page.waitForTimeout(800);

  const result = await page.evaluate(() => {
    const viewWidth = document.documentElement.clientWidth;
    const all = document.querySelectorAll('body *');
    const found = [];
    for (const el of all) {
      const rect = el.getBoundingClientRect();
      // element extends past the right edge of the viewport
      if (rect.right > viewWidth + 2 && rect.width > 0) {
        // only leaf-ish: no children with the same overflow (to avoid reporting every ancestor)
        found.push({
          tag: el.tagName,
          id: el.id,
          cls: (el.className || '').toString().slice(0, 100),
          left: Math.round(rect.left), right: Math.round(rect.right), width: Math.round(rect.width),
          text: (el.textContent || '').trim().slice(0, 50),
        });
      }
    }
    // sort by how far it extends past the edge, most specific (smallest width among big offenders) first
    found.sort((a, b) => (b.right - viewWidth) - (a.right - viewWidth));
    return { viewWidth, docWidth: document.documentElement.scrollWidth, found: found.slice(-15) };
  });
  console.log(JSON.stringify(result, null, 2));
  await browser.close();
})();

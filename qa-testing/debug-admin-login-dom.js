const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 420, height: 800 } });
  await page.goto('http://localhost:5176/admin/login', { waitUntil: 'load', timeout: 20000 });
  await page.waitForTimeout(800);
  const info = await page.evaluate(() => {
    const container = document.querySelector('.text-center.mb-8');
    if (!container) return 'container not found';
    return {
      containerHTML: container.outerHTML.slice(0, 800),
      containerDisplay: getComputedStyle(container).display,
      children: Array.from(container.children).map(c => ({
        tag: c.tagName, class: c.className, display: getComputedStyle(c).display,
        rect: c.getBoundingClientRect()
      }))
    };
  });
  console.log(JSON.stringify(info, null, 2));
  await browser.close();
})();

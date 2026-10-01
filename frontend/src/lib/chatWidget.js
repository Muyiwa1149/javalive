// Injects an admin-configured live-chat embed snippet (e.g. Tawk.to) into the page.
// The stored value is a raw HTML/script blob pasted from the provider's dashboard —
// setting it via innerHTML wouldn't execute the <script> tags, so each one is rebuilt
// as a real <script> element and appended to <body>.
let injected = false

export function injectChatWidget(rawEmbed) {
  if (injected || !rawEmbed || typeof document === 'undefined') return
  injected = true

  const container = document.createElement('div')
  container.innerHTML = rawEmbed

  container.querySelectorAll('script').forEach((oldScript) => {
    const newScript = document.createElement('script')
    for (const attr of oldScript.attributes) {
      newScript.setAttribute(attr.name, attr.value)
    }
    newScript.text = oldScript.textContent
    document.body.appendChild(newScript)
  })
}

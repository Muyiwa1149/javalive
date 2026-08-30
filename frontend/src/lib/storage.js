/** Builds a URL for a file under the backend's /storage disk (logos, avatars, KYC docs, proofs, etc.). */
export function storageUrl(path) {
  if (!path) return null
  return `/storage/${path.replace(/^\/+/, '')}`
}

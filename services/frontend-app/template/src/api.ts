const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

// hardcoded per frontend instance — change to 'instance-b', 'instance-c' for other running instances
export const INSTANCE_ID = import.meta.env.VITE_INSTANCE_ID ?? 'instance-a'

export async function apiFetch<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`)
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`)
  return res.json() as Promise<T>
}

export async function apiPost<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Instance-ID': INSTANCE_ID,
    },
    body: JSON.stringify(body),
  })
  if (res.status === 429) throw new RateLimitError()
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`)
  return res.json() as Promise<T>
}

export class RateLimitError extends Error {
  constructor() {
    super('Too many requests — slow down')
  }
}

export function logError(context: string, error: unknown) {
  const ts = new Date().toISOString()
  console.error(`[${ts}] ${context}:`, error)
}

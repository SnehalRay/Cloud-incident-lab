import { useEffect, useState } from 'react'
import { apiFetch, logError } from '../api'

interface Item {
  id: number
  name: string
  description: string
  createdAt: string
}

export default function HomePage() {
  const [items, setItems] = useState<Item[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    apiFetch<Item[]>('/api/items')
      .then(setItems)
      .catch((err: unknown) => {
        logError('GET /api/items', err)
        setError(err instanceof Error ? err.message : 'Failed to fetch items')
      })
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="page-state">Loading items…</p>

  if (error)
    return (
      <div className="error-box">
        <strong>Error loading items</strong>
        <p>{error}</p>
      </div>
    )

  if (items.length === 0)
    return <p className="page-state">No items found.</p>

  return (
    <div className="page">
      <h1>Items</h1>
      <ul className="item-list">
        {items.map((item) => (
          <li key={item.id} className="item-card">
            <strong>{item.name}</strong>
            {item.description && <p>{item.description}</p>}
            <span className="item-meta">{new Date(item.createdAt).toLocaleString()}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}

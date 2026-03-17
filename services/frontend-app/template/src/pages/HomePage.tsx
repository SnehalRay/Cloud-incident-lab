import { useEffect, useState } from 'react'
import { apiFetch, apiPost, logError, RateLimitError } from '../api'

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

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  useEffect(() => {
    apiFetch<Item[]>('/api/items')
      .then(setItems)
      .catch((err: unknown) => {
        logError('GET /api/items', err)
        setError(err instanceof Error ? err.message : 'Failed to fetch items')
      })
      .finally(() => setLoading(false))
  }, [])

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault()
    if (!name.trim()) return
    setSubmitting(true)
    setSubmitError(null)
    apiPost<Item>('/api/items', { name: name.trim(), description: description.trim() })
      .then((created) => {
        setItems((prev) => [created, ...prev])
        setName('')
        setDescription('')
      })
      .catch((err: unknown) => {
        logError('POST /api/items', err)
        if (err instanceof RateLimitError) {
          setSubmitError('Too many requests — slow down')
        } else {
          setSubmitError(err instanceof Error ? err.message : 'Failed to create item')
        }
      })
      .finally(() => setSubmitting(false))
  }

  if (loading) return <p className="page-state">Loading items…</p>

  if (error)
    return (
      <div className="error-box">
        <strong>Error loading items</strong>
        <p>{error}</p>
      </div>
    )

  return (
    <div className="page">
      <h1>Items</h1>

      <form className="item-form" onSubmit={handleSubmit}>
        <input
          className="form-input"
          type="text"
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <input
          className="form-input"
          type="text"
          placeholder="Description (optional)"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <button className="form-submit" type="submit" disabled={submitting}>
          {submitting ? 'Adding…' : 'Add item'}
        </button>
        {submitError && <p className="form-error">{submitError}</p>}
      </form>

      {items.length === 0 ? (
        <p className="page-state">No items yet. Add one above.</p>
      ) : (
        <ul className="item-list">
          {items.map((item) => (
            <li key={item.id} className="item-card">
              <strong>{item.name}</strong>
              {item.description && <p>{item.description}</p>}
              <span className="item-meta">{new Date(item.createdAt).toLocaleString()}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

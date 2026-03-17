import { useEffect, useState } from 'react'
import { apiFetch, logError } from '../api'

interface StatusResponse {
  service: string
  status: string
  dependencies: {
    postgres: { status: string }
    redis: { status: string }
  }
}

function StatusBadge({ status }: { status: string }) {
  const cls =
    status === 'up' || status === 'healthy'
      ? 'badge badge-up'
      : status === 'degraded'
      ? 'badge badge-degraded'
      : 'badge badge-down'
  return <span className={cls}>{status}</span>
}

export default function StatusPage() {
  const [data, setData] = useState<StatusResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    apiFetch<StatusResponse>('/api/status')
      .then(setData)
      .catch((err: unknown) => {
        logError('GET /api/status', err)
        setError(err instanceof Error ? err.message : 'Failed to fetch status')
      })
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="page-state">Checking status…</p>

  if (error)
    return (
      <div className="error-box">
        <strong>Error loading status</strong>
        <p>{error}</p>
      </div>
    )

  if (!data) return null

  return (
    <div className="page">
      <h1>Status</h1>
      <div className="status-card">
        <div className="status-row">
          <span>Service</span>
          <strong>{data.service}</strong>
        </div>
        <div className="status-row">
          <span>Overall</span>
          <StatusBadge status={data.status} />
        </div>
        <hr />
        <div className="status-row">
          <span>PostgreSQL</span>
          <StatusBadge status={data.dependencies.postgres.status} />
        </div>
        <div className="status-row">
          <span>Redis</span>
          <StatusBadge status={data.dependencies.redis.status} />
        </div>
      </div>
    </div>
  )
}

import { useState } from 'react'
import HomePage from './pages/HomePage'
import StatusPage from './pages/StatusPage'
import './App.css'

type Page = 'home' | 'status'

function App() {
  const [page, setPage] = useState<Page>('home')

  return (
    <>
      <nav className="nav">
        <button
          className={page === 'home' ? 'nav-link active' : 'nav-link'}
          onClick={() => setPage('home')}
        >
          Items
        </button>
        <button
          className={page === 'status' ? 'nav-link active' : 'nav-link'}
          onClick={() => setPage('status')}
        >
          Status
        </button>
      </nav>

      <main className="main">
        {page === 'home' ? <HomePage /> : <StatusPage />}
      </main>
    </>
  )
}

export default App

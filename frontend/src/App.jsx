import Navbar from './components/Navbar'
import { Routes, Route} from 'react-router-dom'
import Home from './pages/Home'
import SearchPage from './pages/SearchPage'
import MoviePage from './pages/MoviePage'

function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path='/' element={<Home />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="/movie/:id" element={<MoviePage />} />
      </Routes>
    </>
  )
}

export default App
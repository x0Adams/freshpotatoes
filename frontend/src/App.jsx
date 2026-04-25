import Navbar from './components/Navbar'
import Footer from './components/Footer'
import { Routes, Route} from 'react-router-dom'
import Home from './pages/Home'
import SearchPage from './pages/SearchPage'
import MoviePage from './pages/MoviePage'
import UserPage from './pages/UserPage'
import StaffPage from './pages/StaffPage'
import PublicProfile from './pages/PublicProfile'
import ScrollToTop from './components/ScrollToTop'

function App() {
  return (
    <>
      <ScrollToTop />
      <Navbar />
      <div style={{ minHeight: 'calc(100vh - 200px)' }}>
        <Routes>
          <Route path='/' element={<Home />} />
          <Route path="/search" element={<SearchPage />} />
          <Route path="/movie/:id" element={<MoviePage />} />
          <Route path="/staff/:id" element={<StaffPage />} />
          <Route path="/profile" element={<UserPage />} />
          <Route path="/user/:id" element={<PublicProfile />} />
        </Routes>
      </div>
      <Footer />
    </>
  )
}

export default App
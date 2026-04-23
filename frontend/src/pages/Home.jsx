import HeroCarousel from "../components/HeroCarousel"
import DiscoverMovies from '../components/DiscoverMovies'
import { MultiGenreMovies } from '../components/GenreMovies'

function Home() {
  return (
    <>
        <HeroCarousel/>
        <DiscoverMovies />
        <MultiGenreMovies />
    </>
  )
}

export default Home
import HeroCarousel from "../components/HeroCarousel"
import DiscoverMovies from '../components/DiscoverMovies'
import { MultiGenreMovies } from '../components/GenreMovies'
import RecommendationsTrack from "../components/RecommendationsTrack"
import SurpriseMeSection from "../components/SurpriseMeSection"

function Home() {
  return (
    <>
        <HeroCarousel/>
        <DiscoverMovies />
        <RecommendationsTrack title="Recommended for you" />
        <SurpriseMeSection />
        <MultiGenreMovies />
    </>
  )
}

export default Home
import HeroCarousel from "../components/HeroCarousel"
import ComingAttractions from '../components/ComingAttractions'

function Home() {
  return (
    <>
        <HeroCarousel/>
        <ComingAttractions />
        <div style={{ height: '512px' }} />
    </>
  )
}

export default Home
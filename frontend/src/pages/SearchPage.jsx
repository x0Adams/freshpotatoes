import { useSearchParams, Link } from 'react-router-dom'
import { useState, useEffect, useCallback, useRef } from 'react'
import { movieApi, genreApi } from '../services/api'
import { useSearch } from '../context/SearchContext'
import MoviePoster from '../components/MoviePoster'

function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const { liveQuery, setLiveQuery } = useSearch()
  
  // URL Params
  const queryParam = searchParams.get('q') || ''
  const genreParam = searchParams.get('genre') || ''
  const staffParam = searchParams.get('staff') || ''
  
  // Local UI State (For inputs)
  const [localGenre, setLocalGenre] = useState(genreParam)
  const [localStaff, setLocalStaff] = useState(staffParam)
  
  const [availableGenres, setAvailableGenres] = useState([])
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [fetchingMore, setFetchingMore] = useState(false)
  const [error, setError] = useState(null)
  const [isFilterVisible, setIsFilterVisible] = useState(window.innerWidth >= 992)
  
  // Pagination State
  const [page, setPage] = useState(0)
  const [hasMore, setHasMore] = useState(true)
  const PAGE_SIZE = 10

  const observer = useRef()
  const lastElementRef = useCallback(node => {
    if (loading || fetchingMore) return
    if (observer.current) observer.current.disconnect()
    
    observer.current = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting && hasMore) {
        setPage(prev => prev + 1)
      }
    })
    
    if (node) observer.current.observe(node)
  }, [loading, fetchingMore, hasMore])

  // 1. Initial Load: Fetch Genres
  useEffect(() => {
    genreApi.getAll()
      .then(data => {
        const sorted = [...data].sort((a, b) => a.name.localeCompare(b.name))
        setAvailableGenres(sorted)
      })
      .catch(err => console.error("Failed to fetch genres:", err))
  }, [])

  // 2. URL -> Local State Sync (Handles back/forward button)
  useEffect(() => {
    setLocalGenre(genreParam)
    setLocalStaff(staffParam)
  }, [genreParam, staffParam])

  // 3. Reset and Search on Query/Filter change
  useEffect(() => {
    setResults([])
    setPage(0)
    setHasMore(true)
  }, [liveQuery, genreParam, staffParam])

  // 4. Fetching Logic (Triggered by reset or page change)
  useEffect(() => {
    const query = liveQuery.trim()
    const hasSearch = query.length >= 3 || genreParam || staffParam.trim().length >= 3
    
    if (!hasSearch) {
      setResults([])
      setLoading(false)
      setHasMore(false)
      return
    }

    const loadData = async () => {
      const isInitial = page === 0
      if (isInitial) setLoading(true)
      else setFetchingMore(true)
      
      setError(null)

      try {
        const data = await movieApi.advancedSearch({ 
          title: query, 
          genre: genreParam, 
          staff: staffParam, 
          page: page,
          size: PAGE_SIZE 
        })

        if (data.length < PAGE_SIZE) {
          setHasMore(false)
        }

        setResults(prev => isInitial ? data : [...prev, ...data])
      } catch (err) {
        // Backend returns 404 for no results
        if (err.message.includes('404')) {
           setHasMore(false)
        } else {
           setError(err.message)
        }
      } finally {
        setLoading(false)
        setFetchingMore(false)
      }
    }

    const timeoutId = setTimeout(loadData, page === 0 ? 400 : 0)
    return () => clearTimeout(timeoutId)
  }, [liveQuery, genreParam, staffParam, page])

  const applyFilters = useCallback(() => {
    const params = {}
    if (liveQuery.trim()) params.q = liveQuery.trim()
    if (localGenre) params.genre = localGenre
    if (localStaff.trim()) params.staff = localStaff.trim()
    setSearchParams(params)
  }, [liveQuery, localGenre, localStaff, setSearchParams])

  const handleSearchSubmit = (e) => {
    if (e) e.preventDefault()
    applyFilters()
  }

  // Automatic filter application for Genre/Staff when they change
  useEffect(() => {
    if (localGenre !== genreParam || localStaff !== staffParam) {
       const timeoutId = setTimeout(() => {
          applyFilters();
       }, 500);
       return () => clearTimeout(timeoutId);
    }
  }, [localGenre, localStaff, genreParam, staffParam, applyFilters]);

  const resetFilters = () => {
    setLiveQuery('')
    setLocalGenre('')
    setLocalStaff('')
    setSearchParams({})
  }

  const activeFilterCount = [genreParam, staffParam].filter(Boolean).length

  return (
    <div className="search-page pb-5">
      <div className="container">
        {/* Cinematic Search Header */}
        <div className="search-page-header mb-5 d-flex justify-content-between align-items-end flex-wrap gap-3">
          <div className="animate-hero-reveal">
            <h1 className="search-page-title mb-0">
              {liveQuery || genreParam || staffParam
                ? <>Results for <span>"{liveQuery || genreParam || staffParam}"</span></>
                : `Discovery`}
            </h1>
            <p className="text-secondary opacity-50 uppercase smallest tracking-widest mt-2 mb-0">
              Explore the freshPotatoes database
            </p>
          </div>

          <button 
            className={`btn-fresh-secondary d-lg-none ${isFilterVisible ? 'active' : ''}`}
            onClick={() => setIsFilterVisible(!isFilterVisible)}
            style={{ height: '42px', padding: '0 1.25rem' }}
          >
            <i className={`bi bi-${isFilterVisible ? 'chevron-up' : 'sliders'} me-2`} />
            {isFilterVisible ? 'Hide Filters' : 'Show Filters'}
            {activeFilterCount > 0 && !isFilterVisible && (
              <span className="badge bg-warning text-dark ms-2 rounded-circle" style={{ padding: '0.35em 0.6em' }}>
                {activeFilterCount}
              </span>
            )}
          </button>
        </div>

        {/* Mobile-Only Search Input */}
        <div className="d-lg-none mb-4 animate-fade-in">
           <div className="input-group filter-input-group" style={{ height: '54px' }}>
              <span className="input-group-text ps-4">
                 <i className="bi bi-search text-warning fs-5" />
              </span>
              <input 
                type="text" 
                className="form-control fs-5"
                placeholder="Search by movie title..."
                value={liveQuery}
                onChange={(e) => setLiveQuery(e.target.value)}
              />
           </div>
        </div>

        {/* Filter Module - Pill Design */}
        {isFilterVisible && (
          <div className="admin-panel-modern mb-5 animate-fade-in">
            <form onSubmit={handleSearchSubmit} className="row g-3 align-items-end">
            <div className="col-lg-5">
              <label className="text-secondary smallest uppercase fw-black mb-2 tracking-widest">Genre</label>
              <select 
                className="custom-select-minimal"
                value={localGenre}
                onChange={(e) => setLocalGenre(e.target.value)}
              >
                <option value="">All Genres</option>
                {availableGenres.map(g => (
                  <option key={g.id || g.name} value={g.name}>{g.name}</option>
                ))}
              </select>
            </div>

            <div className="col-lg-5">
              <label className="text-secondary smallest uppercase fw-black mb-2 tracking-widest">Staff / Person</label>
              <input 
                type="text" 
                className="custom-input-minimal"
                placeholder="Director or Actor"
                value={localStaff}
                onChange={(e) => setLocalStaff(e.target.value)}
              />
            </div>

            <div className="col-lg-2 d-flex gap-2">
              <button type="submit" className="btn-fresh-primary w-100 py-2 shadow-sm" style={{ height: '48px' }}>
                Filter
              </button>
              <button 
                type="button" 
                className="btn-fresh-secondary p-0 d-flex align-items-center justify-content-center" 
                style={{ width: '48px', height: '48px' }}
                onClick={resetFilters}
                title="Reset Filters"
              >
                <i className="bi bi-arrow-counterclockwise fs-5" />
              </button>
            </div>
          </form>
        </div>
        )}

        {/* Results Info */}
        {!loading && (liveQuery || genreParam || staffParam) && results.length > 0 && (
          <div className="d-flex justify-content-between align-items-center mb-4 px-2">
            <span className="text-secondary smaller uppercase fw-bold tracking-widest">
              Showing {results.length} matching titles
            </span>
          </div>
        )}

        {/* Results State Management */}
        {loading && results.length === 0 ? (
          <div className="search-page-empty py-5">
            <div className="spinner-border text-warning" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
            <p className="mt-4 text-secondary uppercase smallest fw-bold tracking-widest">Scanning Database...</p>
          </div>
        ) : !liveQuery && !genreParam && !staffParam ? (
          <div className="search-page-empty py-5 opacity-50">
            <div className="mb-4" style={{ fontSize: '4rem' }}>
               <i className="bi bi-compass text-secondary" />
            </div>
            <h4 className="text-light fw-bold">Ready for Discovery</h4>
            <p className="text-secondary">Apply filters above to find specific movies and staff.</p>
          </div>
        ) : error ? (
          <div className="search-page-empty py-5">
            <i className="bi bi-exclamation-triangle text-danger fs-1 mb-3" />
            <p className="text-secondary">Search failed: {error}</p>
          </div>
        ) : results.length === 0 && !loading ? (
          <div className="search-page-empty py-5">
            <div className="mb-4 opacity-25" style={{ fontSize: '4rem' }}>
               <i className="bi bi-film" />
            </div>
            <h4 className="text-light fw-bold">No Matches Found</h4>
            <p className="text-secondary px-5">We couldn't find any movies matching your specific criteria. Try broadening your filters.</p>
            <button className="btn-fresh-secondary mt-3" onClick={resetFilters}>Clear All Filters</button>
          </div>
        ) : (
          <>
            <div className="search-page-grid animate-fade-in">
              {results.map((item, idx) => {
                const isLastElement = results.length === idx + 1
                return (
                  <Link
                    key={`${item.id}-${idx}`}
                    ref={isLastElement ? lastElementRef : null}
                    to={`/movie/${item.id}`}
                    className="coming-card"
                  >
                    <MoviePoster
                      posterUrl={item.posterUrl}
                      title={item.title}
                      className="coming-card-img"
                    />
                    <div className="coming-card-overlay">
                      <div className="coming-card-genres">
                        {item.genres?.slice(0, 3).map(g => (
                          <p key={g} className="coming-card-genre">{g}</p>
                        ))}
                      </div>
                      <p className="coming-card-title text-truncate w-100 px-2">{item.title}</p>
                      <p className="coming-card-date">
                        <i className="bi bi-calendar3" /> {item.year}
                      </p>
                    </div>
                  </Link>
                )
              })}
            </div>

            {fetchingMore && (
              <div className="text-center py-5">
                <div className="spinner-border text-warning spinner-border-sm me-2" role="status" />
                <span className="text-secondary smallest uppercase tracking-widest fw-bold">Loading more titles...</span>
              </div>
            )}

            {!hasMore && results.length > 0 && (
              <div className="text-center py-5 mt-4 animate-fade-in">
                <div className="mb-3 opacity-10" style={{ fontSize: '4rem' }}>
                   <i className="bi bi-film" />
                </div>
                <h3 className="text-secondary fw-black uppercase tracking-widest mb-1" style={{ fontSize: '1.5rem', opacity: 0.4 }}>
                  End of the results
                </h3>
                <div className="d-flex align-items-center justify-content-center gap-2 opacity-25">
                   <hr className="bg-secondary" style={{ width: '40px' }} />
                   <i className="bi bi-stars text-warning" />
                   <hr className="bg-secondary" style={{ width: '40px' }} />
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}

export default SearchPage
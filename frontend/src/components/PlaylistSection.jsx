import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { playlistApi } from '../services/api'
import MoviePoster from './MoviePoster'
import CreatePlaylistModal from './CreatePlaylistModal'

export default function PlaylistSection({ user }) {
  const [playlists, setPlaylists] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedPlaylist, setSelectedPlaylist] = useState(null)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [isProcessing, setIsProcessing] = useState(false)

  const fetchPlaylists = useCallback(async () => {
    const token = localStorage.getItem('accessToken')
    if (!token) return
    
    try {
      const data = await playlistApi.getByOwner(user.id, token)
      setPlaylists(data)
    } catch (err) {
      console.error("Failed to fetch playlists:", err)
    } finally {
      setLoading(false)
    }
  }, [user.id])

  useEffect(() => {
    fetchPlaylists()
  }, [fetchPlaylists])

  const handleCreateSuccess = (newPlaylist) => {
    fetchPlaylists();
    if (newPlaylist && newPlaylist.id) {
       selectPlaylist(newPlaylist.id);
    }
  }

  const handleDelete = async (playlistId) => {
    if (!window.confirm('Delete this playlist?') || isProcessing) return
    
    setIsProcessing(true)
    const token = localStorage.getItem('accessToken')
    
    try {
      await playlistApi.delete(playlistId, token)
      if (selectedPlaylist?.id === playlistId) setSelectedPlaylist(null)
      await fetchPlaylists()
    } catch (err) {
      alert(err.message)
    } finally {
      setIsProcessing(false)
    }
  }

  const handleRemoveMovie = async (e, playlistId, movieId) => {
    e.preventDefault()
    e.stopPropagation()

    if (!playlistId || !movieId || isProcessing) return;

    // 1. Optimistic Update
    const originalPlaylist = { ...selectedPlaylist };
    setSelectedPlaylist(prev => ({
      ...prev,
      movies: prev.movies.filter(m => m.id !== movieId)
    }));

    setIsProcessing(true)
    const token = localStorage.getItem('accessToken')

    try {
      const updated = await playlistApi.removeMovie(playlistId, movieId, token)
      if (updated) {
        setSelectedPlaylist(updated)
      } else {
        const fresh = await playlistApi.getById(playlistId, token)
        setSelectedPlaylist(fresh)
      }
      await fetchPlaylists()
    } catch (err) {
      setSelectedPlaylist(originalPlaylist);
      alert(err.message || "Failed to remove movie")
    } finally {
      setIsProcessing(false)
    }
  }

  const selectPlaylist = async (id) => {
    const token = localStorage.getItem('accessToken')
    try {
      const data = await playlistApi.getById(id, token)
      setSelectedPlaylist(data)
    } catch (err) {
      console.error(err)
    }
  }

  return (
    <div className="playlist-section mt-5">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="text-warning fw-bold m-0">My Playlists</h3>
        <button 
          className="btn btn-warning btn-sm fw-bold text-dark"
          onClick={() => setShowCreateModal(true)}
          disabled={isProcessing}
        >
          + New Playlist
        </button>
      </div>

      <CreatePlaylistModal 
        show={showCreateModal} 
        onHide={() => setShowCreateModal(false)}
        onSuccess={handleCreateSuccess}
      />

      <div className="row">
        <div className="col-md-4">
          <div className="list-group list-group-flush border border-secondary rounded overflow-hidden shadow-sm">
            {playlists.length === 0 && !loading && (
              <div className="bg-dark p-4 text-center">
                <i className="bi bi-collection-play text-secondary fs-2 mb-2 d-block" />
                <p className="text-secondary small m-0">You don't have any playlists yet.</p>
              </div>
            )}
            {playlists.map(pl => (
              <button
                key={pl.id}
                className={`list-group-item list-group-item-action bg-dark text-light border-secondary d-flex justify-content-between align-items-center py-3 ${selectedPlaylist?.id === pl.id ? 'active border-warning' : ''}`}
                onClick={() => selectPlaylist(pl.id)}
                disabled={isProcessing}
              >
                <div className="d-flex align-items-center gap-2">
                  <i className="bi bi-music-note-list text-warning" />
                  <span className="fw-semibold">{pl.name}</span>
                </div>
                <i className="bi bi-chevron-right smaller opacity-50" />
              </button>
            ))}
          </div>
        </div>

        <div className="col-md-8 mt-4 mt-md-0">
          {selectedPlaylist ? (
            <div className="bg-dark p-4 rounded border border-secondary h-100 shadow-sm">
              <div className="d-flex justify-content-between align-items-start mb-4">
                <div>
                  <h4 className="text-light fw-bold mb-1">{selectedPlaylist.name}</h4>
                  <p className="text-secondary smaller m-0">
                    {selectedPlaylist.isPrivate ? (
                      <><i className="bi bi-lock-fill me-1" /> Private</>
                    ) : (
                      <><i className="bi bi-globe me-1" /> Public</>
                    )}
                    {" • "}{selectedPlaylist.movies?.length || 0} movies
                  </p>
                </div>
                <button 
                  className="btn btn-outline-danger btn-sm border-0"
                  onClick={() => handleDelete(selectedPlaylist.id)}
                  disabled={isProcessing}
                  title="Delete playlist"
                >
                  <i className="bi bi-trash3" />
                </button>
              </div>

              <div className="row g-3">
                {selectedPlaylist.movies?.map((movie, idx) => (
                  <div key={`${movie.id}-${idx}`} className="col-6 col-sm-4">
                    <div className="position-relative">
                      <Link
                        to={`/movie/${movie.id}`}
                        className="coming-card w-100"
                      >
                        <MoviePoster
                          posterUrl={movie.posterUrl}
                          title={movie.title}
                          className="coming-card-img"
                        />
                        <div className="coming-card-overlay">
                          <p className="coming-card-genre">{movie.genre}</p>
                          <p className="coming-card-title text-truncate w-100">{movie.title}</p>
                          <p className="coming-card-date">
                            <i className="bi bi-calendar3" />
                            {movie.year}
                          </p>
                        </div>
                      </Link>
                      <button 
                        className="btn btn-danger btn-sm position-absolute top-0 end-0 m-2 z-3 shadow"
                        onClick={(e) => handleRemoveMovie(e, selectedPlaylist.id, movie.id)}
                        style={{ borderRadius: '50%', width: '28px', height: '28px', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 0 }}
                        title="Remove from playlist"
                        disabled={isProcessing}
                      >
                        <i className="bi bi-x-lg" style={{ fontSize: '0.8rem' }} />
                      </button>
                    </div>
                  </div>
                ))}
                {selectedPlaylist.movies?.length === 0 && (
                  <div className="text-center py-5 w-100">
                    <i className="bi bi-plus-circle text-secondary fs-1 mb-3 d-block" />
                    <p className="text-secondary italic">This playlist is empty. Add some movies from the movie pages!</p>
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div className="d-flex flex-column align-items-center justify-content-center h-100 border border-secondary border-dashed rounded py-5 text-secondary bg-dark bg-opacity-25">
              <i className="bi bi-arrow-left-circle mb-3 fs-3" />
              <p className="m-0">Select a playlist from the left to manage its content</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

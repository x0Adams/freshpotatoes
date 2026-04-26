import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { playlistApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import MoviePoster from './MoviePoster'
import CreatePlaylistModal from './CreatePlaylistModal'
import ConfirmModal from './ConfirmModal'

export default function PlaylistSection({ user, initialPlaylists = [], readOnly = false }) {
  const { user: currentUser } = useAuth()
  const { showToast } = useToast()
  const [playlists, setPlaylists] = useState(initialPlaylists)
  const [loading, setLoading] = useState(!readOnly)
  const [selectedPlaylist, setSelectedPlaylist] = useState(null)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [playlistToDelete, setPlaylistToDelete] = useState(null)
  const [isProcessing, setIsProcessing] = useState(false)

  const fetchPlaylists = useCallback(async () => {
    if (readOnly) return
    const token = localStorage.getItem('accessToken')
    if (!token) return
    
    try {
      const newData = await playlistApi.getByOwner(user.id, token)
      setPlaylists(prev => {
        // Merge: keep movie data from prev if it exists for the same ID
        return newData.map(newItem => {
          const existing = prev.find(p => p.id === newItem.id)
          return {
            ...newItem,
            movies: Array.isArray(newItem.movies) ? newItem.movies : (existing?.movies || [])
          }
        })
      })
    } catch (err) {
      console.error("Failed to fetch playlists:", err)
    } finally {
      setLoading(false)
    }
  }, [user.id, readOnly])

  useEffect(() => {
    if (readOnly) {
      setPlaylists(initialPlaylists)
      setLoading(false)
      return
    }

    // On /profile we already receive playlists (with movies) from /auth/me.
    if (Array.isArray(initialPlaylists) && initialPlaylists.length > 0) {
      setPlaylists(initialPlaylists)
      setLoading(false)
    } else {
      fetchPlaylists()
    }
  }, [fetchPlaylists, readOnly, initialPlaylists])

  const handleCreateSuccess = (newPlaylist) => {
    if (readOnly) return

    if (newPlaylist) {
      const normalized = {
        ...newPlaylist,
        movies: Array.isArray(newPlaylist.movies) ? newPlaylist.movies : []
      }
      setPlaylists(prev => [normalized, ...prev.filter(p => p.id !== normalized.id)])
    }

    if (newPlaylist && newPlaylist.id) {
       selectPlaylist(newPlaylist.id);
    }
  }

  const handleDelete = (playlistId) => {
    if (readOnly && !currentUser?.isAdmin) return
    setPlaylistToDelete(playlistId)
    setShowDeleteModal(true)
  }

  const handleConfirmDelete = async () => {
    if (!playlistToDelete || isProcessing) return
    
    setIsProcessing(true)
    const token = localStorage.getItem('accessToken')
    
    try {
      if (currentUser?.isAdmin && readOnly) {
         await playlistApi.adminDeletePlaylist(playlistToDelete, token)
      } else {
         await playlistApi.delete(playlistToDelete, token)
      }
      if (selectedPlaylist?.id === playlistToDelete) setSelectedPlaylist(null)

      if (readOnly) {
         setPlaylists(prev => prev.filter(p => p.id !== playlistToDelete))
      } else {
         setPlaylists(prev => prev.filter(p => p.id !== playlistToDelete))
      }
      showToast("Playlist deleted successfully.")
    } catch (err) {
      showToast(err.message, "error")
    } finally {
      setIsProcessing(false)
      setPlaylistToDelete(null)
    }
  }

  const handleRemoveMovie = async (e, playlistId, movieId) => {
    if (readOnly) return
    e.preventDefault()
    e.stopPropagation()

    if (!playlistId || !movieId || isProcessing) return;

    // Optimistic Update
    const originalPlaylist = { ...selectedPlaylist };
    setSelectedPlaylist(prev => ({
      ...prev,
      movies: prev.movies.filter(m => m.id !== movieId)
    }));

    setIsProcessing(true)
    const token = localStorage.getItem('accessToken')

    try {
      await playlistApi.removeMovie(playlistId, movieId, token)
      const fresh = await playlistApi.getById(playlistId, token)
      setSelectedPlaylist(fresh)
      // Sync updated movie list to the sidebar without owner-wide refetch.
      setPlaylists(prev => prev.map(pl =>
        pl.id === playlistId ? { ...pl, movies: fresh.movies } : pl
      ))
      showToast("Movie removed from playlist")
    } catch (err) {
      setSelectedPlaylist(originalPlaylist);
      showToast(err.message || "Failed to remove movie", "error")
    } finally {
      setIsProcessing(false)
    }
  }

  const selectPlaylist = async (id) => {
    // If clicking the already selected playlist, deselect it
    if (selectedPlaylist?.id === id) {
      setSelectedPlaylist(null);
      return;
    }

    if (readOnly) {
        const pl = playlists.find(p => p.id === id);
        setSelectedPlaylist(pl);
        return;
    }
    const localPlaylist = playlists.find(p => p.id === id)
    if (Array.isArray(localPlaylist?.movies)) {
      setSelectedPlaylist(localPlaylist)
      return
    }

    const token = localStorage.getItem('accessToken')
    try {
      const data = await playlistApi.getById(id, token)
      setSelectedPlaylist(data)
      
      // Sync the count back to the sidebar list
      setPlaylists(prev => prev.map(pl => 
        pl.id === id ? { ...pl, movies: data.movies } : pl
      ))
    } catch (err) {
      console.error(err)
    }
  }

  const orderedPlaylists = [...playlists].sort((a, b) => Number(a?.id ?? 0) - Number(b?.id ?? 0))

  return (
    <div className="playlist-dashboard mt-4 pt-4 border-top border-secondary border-opacity-10">
      <div className="row g-4">
        {/* Sidebar: Playlist Selection */}
        <div className="col-lg-3">
          <div className="d-flex justify-content-between align-items-center mb-4">
             <h4 className="text-warning fw-black m-0 tracking-tight">COLLECTIONS</h4>
             {!readOnly && (
                <button 
                  className="btn-fresh-primary p-0 d-flex align-items-center justify-content-center" 
                  style={{ width: '32px', height: '32px' }}
                  onClick={() => setShowCreateModal(true)}
                  title="New Playlist"
                >
                  <i className="bi bi-plus-lg fs-6" />
                </button>
             )}
          </div>

          <div className="playlist-sidebar">
            {orderedPlaylists.map(pl => (
              <button
                key={pl.id}
                className={`playlist-sidebar-item ${selectedPlaylist?.id === pl.id ? 'active' : ''}`}
                onClick={() => selectPlaylist(pl.id)}
              >
                <div className="d-flex align-items-center gap-3">
                  <div className={`playlist-icon-box ${selectedPlaylist?.id === pl.id ? 'bg-warning' : 'bg-secondary bg-opacity-10'}`}>
                    <i className={`bi bi-${pl.isPrivate ? 'lock-fill' : 'collection-play-fill'} ${selectedPlaylist?.id === pl.id ? 'text-dark' : 'text-warning'}`} />
                  </div>
                  <div className="text-start overflow-hidden">
                    <div className="playlist-sidebar-name text-truncate">{pl.name}</div>
                    <div className="playlist-sidebar-meta">{pl.movies?.length || 0} movies</div>
                  </div>
                </div>
                <i className="bi bi-chevron-right ms-auto opacity-25" />
              </button>
            ))}
            
            {playlists.length === 0 && !loading && (
              <div className="text-center py-4 border border-secondary border-dashed rounded opacity-50">
                 <p className="smaller m-0">No collections yet</p>
              </div>
            )}
          </div>
        </div>

        {/* Main: Movies Grid */}
        <div className="col-lg-9">
          {selectedPlaylist ? (
            <div className="playlist-main-content animate-fade-in">
              <div className="d-flex justify-content-between align-items-start mb-4">
                <div className="overflow-hidden">
                  <h2 className="text-light fw-black mb-1 text-truncate">{selectedPlaylist.name}</h2>
                  <div className="d-flex align-items-center gap-3">
                    <span className="badge bg-warning text-dark px-3 rounded-pill uppercase smaller fw-bold">
                       {selectedPlaylist.movies?.length || 0} Titles
                    </span>
                    {!readOnly && (
                        <span className="text-secondary smaller uppercase tracking-widest fw-bold">
                            {selectedPlaylist.isPrivate ? 'Private' : 'Public'}
                        </span>
                    )}
                  </div>
                </div>
                {(!readOnly || currentUser?.isAdmin) && (
                  <button 
                    className="btn-fresh-danger p-0"
                    style={{ width: '36px', height: '36px', borderRadius: '50%' }}
                    onClick={() => handleDelete(selectedPlaylist.id)}
                    disabled={isProcessing}
                    title={currentUser?.isAdmin && readOnly ? "Admin: Delete Playlist" : "Delete playlist"}
                  >
                    <i className={`bi bi-${currentUser?.isAdmin && readOnly ? 'shield-x' : 'trash3'} fs-5`} />
                  </button>
                )}
              </div>

              {selectedPlaylist.movies?.length > 0 ? (
                <div className="playlist-grid">
                  {selectedPlaylist.movies.map((movie, idx) => (
                    <div key={`${movie.id}-${idx}`} className="playlist-card-wrapper">
                      <Link to={`/movie/${movie.id}`} className="coming-card w-100">
                        <MoviePoster
                          posterUrl={movie.posterUrl}
                          title={movie.title}
                          className="coming-card-img"
                        />
                        <div className="coming-card-overlay">
                          <div className="coming-card-genres">
                            {movie.genres.slice(0, 2).map(g => (
                              <p key={g} className="coming-card-genre">{g}</p>
                            ))}
                          </div>
                          <p className="coming-card-title text-truncate w-100">{movie.title}</p>
                          <p className="coming-card-date">
                            <i className="bi bi-calendar3" /> {movie.year}
                          </p>
                        </div>
                      </Link>
                      {!readOnly && (
                        <button 
                          className="playlist-remove-btn"
                          onClick={(e) => handleRemoveMovie(e, selectedPlaylist.id, movie.id)}
                          disabled={isProcessing}
                        >
                          <i className="bi bi-x-lg" />
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-5 admin-panel-modern border-dashed">
                  <i className="bi bi-camera-reels text-secondary fs-1 mb-3 d-block opacity-10" />
                  <h5 className="text-secondary">This collection is empty</h5>
                  {!readOnly && <p className="text-secondary smaller italic">Add movies from discovery to start your collection!</p>}
                </div>
              )}
            </div>
          ) : (
            <div className="h-100 d-flex flex-column align-items-center justify-content-center py-5 admin-panel-modern border-dashed">
               <div className="mb-4 opacity-10" style={{ fontSize: '5rem' }}>
                  <i className="bi bi-collection-play" />
               </div>
               <h4 className="text-secondary fw-bold">No Collection Selected</h4>
               <p className="text-secondary smaller text-center opacity-50 px-4">Choose a playlist from the sidebar to manage your movies.</p>
            </div>
          )}
        </div>
      </div>

      {!readOnly && (
        <CreatePlaylistModal 
          show={showCreateModal} 
          onHide={() => setShowCreateModal(false)}
          onSuccess={handleCreateSuccess}
        />
      )}

      <ConfirmModal
        show={showDeleteModal}
        onHide={() => setShowDeleteModal(false)}
        onConfirm={handleConfirmDelete}
        title="Delete Collection"
        message="Are you sure you want to delete this collection? This action is permanent and cannot be undone."
        confirmText="Delete"
        isDanger={true}
      />
    </div>
  )
}
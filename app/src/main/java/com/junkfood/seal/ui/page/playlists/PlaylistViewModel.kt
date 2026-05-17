package com.junkfood.seal.ui.page.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junkfood.seal.database.objects.Playlist
import com.junkfood.seal.util.DatabaseUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistViewModel : ViewModel() {
    val playlistsFlow = DatabaseUtil.getPlaylistsFlow()

    suspend fun createPlaylist(name: String) {
        DatabaseUtil.createPlaylist(name)
    }

    suspend fun renamePlaylist(playlist: Playlist, newName: String) {
        DatabaseUtil.updatePlaylist(playlist.copy(name = newName))
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseUtil.deletePlaylist(playlist)
        }
    }
}

package com.xeylabs.doit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xeylabs.doit.data.update.UpdateException
import com.xeylabs.doit.data.update.UpdateInfo
import com.xeylabs.doit.data.update.UpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data class Available(val info: UpdateInfo) : UpdateUiState
    data class Downloading(val progress: Float) : UpdateUiState
    data class Ready(val file: File, val versionName: String) : UpdateUiState
    data class NeedsPermission(val file: File, val versionName: String) : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val updateManager = UpdateManager.getInstance(application)

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    init {
        checkForUpdate()
    }

    fun checkForUpdate() {
        if (UpdateManager.hasCheckedThisSession) return
        _uiState.value = UpdateUiState.Checking
        viewModelScope.launch {
            val info = updateManager.checkForUpdate()
            _uiState.value = if (info != null) UpdateUiState.Available(info) else UpdateUiState.Idle
        }
    }

    fun startDownload() {
        val info = (_uiState.value as? UpdateUiState.Available)?.info ?: return
        _uiState.value = UpdateUiState.Downloading(0f)
        viewModelScope.launch {
            try {
                val file = updateManager.downloadUpdate(info) { progress ->
                    _uiState.value = UpdateUiState.Downloading(progress)
                }
                _uiState.value = UpdateUiState.Ready(file, info.versionName)
            } catch (e: UpdateException) {
                _uiState.value = UpdateUiState.Error(e.message ?: "Unduhan gagal.")
            } catch (e: Exception) {
                _uiState.value = UpdateUiState.Error("Unduhan gagal: ${e.message ?: "kesalahan tak dikenal"}.")
            }
        }
    }

    fun installUpdate() {
        val ready = when (val current = _uiState.value) {
            is UpdateUiState.Ready -> current.file to current.versionName
            is UpdateUiState.NeedsPermission -> current.file to current.versionName
            else -> return
        }
        val (file, versionName) = ready
        if (!updateManager.hasInstallPermission()) {
            _uiState.value = UpdateUiState.NeedsPermission(file, versionName)
            return
        }
        try {
            updateManager.installApk(file)
        } catch (e: Exception) {
            _uiState.value = UpdateUiState.Error("Tidak bisa membuka installer: ${e.message ?: ""}.")
        }
    }

    fun openInstallSettings() {
        updateManager.openUnknownSourcesSettings()
    }

    fun dismiss() {
        _uiState.value = UpdateUiState.Idle
    }
}

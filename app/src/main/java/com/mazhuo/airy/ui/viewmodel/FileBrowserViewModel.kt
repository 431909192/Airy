package com.mazhuo.airy.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mazhuo.airy.data.local.dao.ServerConfigDao
import com.mazhuo.airy.domain.model.AiryFile
import com.mazhuo.airy.domain.repository.LocalFileRepository
import com.mazhuo.airy.domain.repository.RemoteFileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Stack
import javax.inject.Inject
@HiltViewModel
class FileBrowserViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val localRepo: LocalFileRepository,
    private val remoteRepo: RemoteFileRepository,
    private val serverDao: ServerConfigDao
) : ViewModel() {
    private val exploreType: String = checkNotNull(savedStateHandle["type"]) // "local" 或 "remote"
    private val serverId: Long = savedStateHandle.get<String>("serverId")?.toLongOrNull() ?: -1L

    // UI 状态群
    var uiState by mutableStateOf<FileUiState>(FileUiState.Loading)
    private  set

    var currentPath by mutableStateOf("")
        private set

    // 路径历史栈（用于物理或顶部栏返回键）
    private val pathStack = Stack<String>()

    init {
        // 首次加载初始化入口路径
        viewModelScope.launch {
            if (exploreType == "local") {
                currentPath = localRepo.getStorageRootPath()
                loadFiles()
            } else {
                val config = serverDao.getServerById(serverId)
                if (config != null) {
                    currentPath = config.rootPath
                    loadFiles()
                } else {
                    uiState = FileUiState.Error("未找到服务器配置")
                }
            }
        }
    }

    fun loadFiles() {
        viewModelScope.launch {
            uiState = FileUiState.Loading
            try {
                val files = if (exploreType == "local") {
                    localRepo.fetchLocalFiles(currentPath)
                } else {
                    val config = serverDao.getServerById(serverId)!!
                    remoteRepo.fetchRemoteFiles(config, currentPath)
                }
                uiState = FileUiState.Success(files)
            } catch (e: Exception) {
                uiState = FileUiState.Error(e.message ?: "读取文件失败")
            }
        }
    }

    /**
     * 进入文件夹
     */
    fun enterDirectory(path: String) {
        pathStack.push(currentPath)
        currentPath = path
        loadFiles()
    }

    /**
     * 回退上一级目录，返回 true 代表成功消耗事件，false 代表已经到根目录了可以退出屏幕
     */
    fun popBackIfNeeded(): Boolean {
        if (pathStack.isNotEmpty()) {
            currentPath = pathStack.pop()
            loadFiles()
            return true
        }
        return false
    }
}
sealed interface FileUiState {
    object Loading : FileUiState
    data class Success(val files: List<AiryFile>) : FileUiState
    data class Error(val msg: String) : FileUiState
}
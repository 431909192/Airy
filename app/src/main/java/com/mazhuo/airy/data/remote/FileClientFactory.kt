package com.mazhuo.airy.data.remote

import com.mazhuo.airy.domain.model.ProtocolType
import com.mazhuo.airy.domain.repository.FileClient

object FileClientFactory {
    fun createClient(protocolType: ProtocolType): FileClient {
        return when (protocolType) {
            ProtocolType.FTP -> FtpFileClient()
            ProtocolType.FTPS -> FtpFileClient()
            ProtocolType.SFTP -> SftpFileClient()
            ProtocolType.SMB -> SmbFileClient()
            ProtocolType.WEBDAV -> {
                SftpFileClient()
            }
        }
    }
}
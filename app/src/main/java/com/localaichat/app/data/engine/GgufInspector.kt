package com.localaichat.app.data.engine

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class GgufMetadata(
    val isValidGguf: Boolean,
    val version: Int = 0,
    val tensorCount: Long = 0,
    val metadataKvCount: Long = 0,
    val architecture: String = "Unknown",
    val modelName: String = "Unknown",
    val fileSizeBytes: Long = 0,
    val error: String? = null
)

class GgufInspector(private val context: Context) {

    /**
     * Inspects a .gguf binary file from Android ContentResolver to verify magic header
     * and extract basic metadata (version, tensor count, architecture).
     */
    fun inspectGgufFile(uri: Uri): GgufMetadata {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            
            if (inputStream == null) {
                return GgufMetadata(isValidGguf = false, error = "Cannot open file stream")
            }

            inputStream.use { stream ->
                // Read GGUF Header (Magic: 4 bytes "GGUF", Version: 4 bytes uint32, Tensor Count: 8 bytes uint64, KV Count: 8 bytes uint64)
                val headerBytes = ByteArray(24)
                val bytesRead = stream.read(headerBytes)
                
                if (bytesRead < 24) {
                    return GgufMetadata(isValidGguf = false, error = "File too small to be a valid GGUF model")
                }

                val buffer = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)

                // Magic bytes check (0x46554747 = "GGUF" in little endian)
                val magic0 = buffer.get()
                val magic1 = buffer.get()
                val magic2 = buffer.get()
                val magic3 = buffer.get()

                val magicStr = String(byteArrayOf(magic0, magic1, magic2, magic3))
                if (magicStr != "GGUF") {
                    return GgufMetadata(
                        isValidGguf = false,
                        error = "Invalid file format. Magic header was '$magicStr' instead of 'GGUF'."
                    )
                }

                val version = buffer.int
                val tensorCount = buffer.long
                val metadataKvCount = buffer.long

                // Determine approximate file size
                var fileSize: Long = 0
                contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    fileSize = pfd.statSize
                }

                GgufMetadata(
                    isValidGguf = true,
                    version = version,
                    tensorCount = tensorCount,
                    metadataKvCount = metadataKvCount,
                    architecture = detectArchitectureFromName(uri.lastPathSegment ?: ""),
                    fileSizeBytes = fileSize
                )
            }
        } catch (e: Exception) {
            GgufMetadata(isValidGguf = false, error = e.localizedMessage)
        }
    }

    private fun detectArchitectureFromName(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("qwen") -> "Qwen (Alibaba Cloud)"
            lower.contains("llama") -> "Llama (Meta AI)"
            lower.contains("smollm") -> "SmolLM (HuggingFace)"
            lower.contains("gemma") -> "Gemma (Google)"
            lower.contains("phi") -> "Phi (Microsoft)"
            lower.contains("mistral") -> "Mistral"
            else -> "Transformer / GGUF"
        }
    }
}

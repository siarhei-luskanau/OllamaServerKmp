package template.di

import android.content.Context
import androidx.datastore.core.Storage
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.annotation.Single
import template.core.common.DispatcherSet
import template.core.pref.StorageProvider

@Single
internal class AppStorageProviderAndroid(
    private val context: Context,
    private val dispatcherSet: DispatcherSet,
) : StorageProvider {
    override fun <T> getStorage(serializer: OkioSerializer<T>): Storage<T> =
        OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = serializer,
            producePath = {
                runBlocking(dispatcherSet.ioDispatcher()) {
                    context.filesDir
                        .resolve("app.pref.json")
                        .absolutePath
                        .toPath()
                }
            },
        )
}

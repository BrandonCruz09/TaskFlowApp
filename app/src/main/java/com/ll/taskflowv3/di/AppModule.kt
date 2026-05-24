package com.ll.taskflowv3.di

import android.app.Application
import androidx.room.Room
import com.ll.taskflowv3.data.local.TaskDao
import com.ll.taskflowv3.data.local.TaskDatabase
import com.ll.taskflowv3.data.remote.TaskApi
import com.ll.taskflowv3.data.repository.TaskRepositoryImpl
import com.ll.taskflowv3.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Esto significa que estas dependencias vivirán mientras la app esté abierta
object AppModule {

    // 1. Proveemos la Base de Datos Local (Room)
    @Provides
    @Singleton
    fun provideTaskDatabase(app: Application): TaskDatabase {
        return Room.databaseBuilder(
            app,
            TaskDatabase::class.java,
            "taskflow_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(db: TaskDatabase): TaskDao {
        return db.taskDao
    }

    // 2. Proveemos la conexión a la API PHP (Retrofit)
    @Provides
    @Singleton
    fun provideTaskApi(): TaskApi {
        // El interceptor nos servirá para ver las peticiones de red en el Logcat (Consola)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            // CAMBIA ESTO por la URL real de tu backend PHP o servidor local
            .baseUrl("http://10.0.2.2/taskflow/api/") // El '/' al final es obligatorio
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TaskApi::class.java)
    }

    // 3. Unimos la Base de datos y la API en el Repositorio
    @Provides
    @Singleton
    fun provideTaskRepository(
        dao: TaskDao,
        api: TaskApi
    ): TaskRepository {
        // Hilt inyectará automáticamente el dao y el api aquí
        return TaskRepositoryImpl(dao, api)
    }
    // 4. Proveemos el Manejador de Seguridad (Tokens)
    @Provides
    @Singleton
    fun provideAuthPreferences(app: Application): com.ll.taskflowv3.data.local.datastore.AuthPreferences {
        return com.ll.taskflowv3.data.local.datastore.AuthPreferences(app)
    }
}
package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.gallery.GalleryPostsStoreImpl
import net.pantasystem.milktea.data.infrastructure.gallery.impl.GalleryRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.gallery.impl.InMemoryGalleryDataSource
import net.pantasystem.milktea.model.gallery.GalleryDataSource
import net.pantasystem.milktea.model.gallery.GalleryPostsStore
import net.pantasystem.milktea.model.gallery.GalleryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GalleryModule {

    @Binds
    @Singleton
    abstract fun galleryDataSource(galleryDataSource: InMemoryGalleryDataSource): GalleryDataSource

    @Binds
    @Singleton
    abstract fun galleryRepository(impl: GalleryRepositoryImpl): GalleryRepository

    @Binds
    @Singleton
    abstract fun provideGalleryPostsStoreFactory(impl: GalleryPostsStoreImpl.Factory): GalleryPostsStore.Factory
}
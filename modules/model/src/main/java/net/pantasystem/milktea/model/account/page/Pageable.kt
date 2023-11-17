package net.pantasystem.milktea.model.account.page

import java.io.Serializable

/**
 * Global, Local, Hybrid, Home, UserList, Mention, Show, SearchByTag, Featured, Notification, UserTimeline, Search, Antenna
 */
sealed class Pageable : Serializable {

    data class GlobalTimeline(

        var withFiles: Boolean? = null,
        var excludeReposts: Boolean? = null,
        var excludeReplies: Boolean? = null
    ) : Pageable(), UntilPaginate, SincePaginate,
        CanOnlyMedia<GlobalTimeline>,
        CanExcludeReplies<GlobalTimeline>,
        CanExcludeReposts<GlobalTimeline> {
        override fun toParams(): PageParams {
            return PageParams(
                withFiles = withFiles,
                type = PageType.GLOBAL,
                excludeReplies = excludeReplies,
                excludeReposts = excludeReposts
            )
        }

        override fun setOnlyMedia(isOnlyMedia: Boolean): GlobalTimeline {
            // NOTE: Misskeyの場合falseを指定してしまうとファイルを除外してしまう
            // NOTE: setMediaOnlyなのにファイルを除外してしまう挙動をしてしまうのは不自然なのでfalseの場合はnullを指定している
            return copy(
                withFiles = isOnlyMedia.takeIf {
                    it
                }
            )
        }

        override fun getOnlyMedia(): Boolean {
            return withFiles ?: false
        }

        override fun getExcludeReplies(): Boolean {
            return excludeReplies ?: false
        }

        override fun setExcludeReplies(isExcludeReplies: Boolean): GlobalTimeline {
            return copy(
                excludeReplies = isExcludeReplies
            )
        }

        override fun getExcludeReposts(): Boolean {
            return excludeReposts ?: false
        }

        override fun setExcludeReposts(isExcludeReposts: Boolean): GlobalTimeline {
            return copy(
                excludeReposts = isExcludeReposts
            )
        }
    }

    data class LocalTimeline(

        var withFiles: Boolean? = null,
        var excludeNsfw: Boolean? = null,
        var excludeReplies: Boolean? = null,
        var excludeReposts: Boolean? = null

    ) : Pageable(), UntilPaginate, SincePaginate, CanOnlyMedia<LocalTimeline>,
        CanExcludeReplies<LocalTimeline>, CanExcludeReposts<LocalTimeline> {
        override fun toParams(): PageParams {
            return PageParams(
                PageType.LOCAL,
                withFiles = withFiles,
                excludeNsfw = excludeNsfw,
                excludeReplies = excludeReplies,
                excludeReposts = excludeReposts
            )
        }

        override fun setOnlyMedia(isOnlyMedia: Boolean): LocalTimeline {
            return copy(
                withFiles = isOnlyMedia.takeIf {
                    it
                }
            )
        }

        override fun getOnlyMedia(): Boolean {
            return withFiles ?: false
        }

        override fun setExcludeReplies(isExcludeReplies: Boolean): LocalTimeline {
            return copy(
                excludeReplies = isExcludeReplies
            )
        }

        override fun getExcludeReplies(): Boolean {
            return excludeReplies ?: false
        }

        override fun setExcludeReposts(isExcludeReposts: Boolean): LocalTimeline {
            return copy(
                excludeReposts = isExcludeReposts
            )
        }

        override fun getExcludeReposts(): Boolean {
            return excludeReposts ?: false
        }
    }

    data class HybridTimeline(

        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null,
        var excludeReplies: Boolean? = null,
        var excludeReposts: Boolean? = null

    ) : Pageable(), UntilPaginate, SincePaginate, CanOnlyMedia<HybridTimeline>,
        CanExcludeReplies<HybridTimeline>, CanExcludeReposts<HybridTimeline> {
        override fun toParams(): PageParams {
            return PageParams(
                type = PageType.SOCIAL,
                withFiles = withFiles,
                includeLocalRenotes = includeLocalRenotes,
                includeMyRenotes = includeMyRenotes,
                includeRenotedMyRenotes = includeRenotedMyRenotes,
                excludeReplies = excludeReplies,
                excludeReposts = excludeReposts
            )
        }

        override fun setOnlyMedia(isOnlyMedia: Boolean): HybridTimeline {
            return copy(
                withFiles = isOnlyMedia.takeIf {
                    it
                }
            )
        }

        override fun getOnlyMedia(): Boolean {
            return withFiles ?: false
        }

        override fun setExcludeReplies(isExcludeReplies: Boolean): HybridTimeline {
            return copy(
                excludeReplies = isExcludeReplies
            )
        }

        override fun getExcludeReplies(): Boolean {
            return excludeReplies ?: false
        }

        override fun setExcludeReposts(isExcludeReposts: Boolean): HybridTimeline {
            return copy(
                excludeReposts = isExcludeReposts
            )
        }

        override fun getExcludeReposts(): Boolean {
            return excludeReposts ?: false
        }
    }

    data class HomeTimeline(

        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null,
        var excludeReplies: Boolean? = null,
        var excludeReposts: Boolean? = null

    ) : Pageable(), UntilPaginate, SincePaginate, CanOnlyMedia<HomeTimeline>,
        CanExcludeReplies<HomeTimeline>, CanExcludeReposts<HomeTimeline> {

        override fun toParams(): PageParams {
            return PageParams(
                PageType.HOME,
                withFiles = withFiles,
                includeRenotedMyRenotes = includeRenotedMyRenotes,
                includeLocalRenotes = includeLocalRenotes,
                includeMyRenotes = includeMyRenotes,
                excludeReplies = excludeReplies,
                excludeReposts = excludeReposts,
            )
        }

        override fun setOnlyMedia(isOnlyMedia: Boolean): HomeTimeline {
            return copy(
                withFiles = isOnlyMedia.takeIf {
                    it
                }
            )
        }

        override fun getOnlyMedia(): Boolean {
            return withFiles ?: false
        }

        override fun setExcludeReplies(isExcludeReplies: Boolean): HomeTimeline {
            return copy(
                excludeReplies = isExcludeReplies
            )
        }

        override fun getExcludeReplies(): Boolean {
            return excludeReplies ?: false
        }

        override fun setExcludeReposts(isExcludeReposts: Boolean): HomeTimeline {
            return copy(
                excludeReposts = isExcludeReposts
            )
        }

        override fun getExcludeReposts(): Boolean {
            return excludeReposts ?: false
        }
    }

    data class UserListTimeline(

        val listId: String,
        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null

    ) : Pageable(), UntilPaginate, SincePaginate, CanOnlyMedia<UserListTimeline> {

        override fun toParams(): PageParams {
            return PageParams(
                PageType.USER_LIST,
                listId = listId,
                withFiles = withFiles,
                includeLocalRenotes = includeLocalRenotes,
                includeMyRenotes = includeMyRenotes,
                includeRenotedMyRenotes = includeRenotedMyRenotes
            )
        }

        override fun setOnlyMedia(isOnlyMedia: Boolean): UserListTimeline {
            return copy(
                withFiles = isOnlyMedia.takeIf {
                    it
                }
            )
        }

        override fun getOnlyMedia(): Boolean {
            return withFiles ?: false
        }
    }

    data class ChannelTimeline(
        val channelId: String,
    ) : Pageable(), UntilPaginate, SincePaginate {
        override fun toParams(): PageParams {
            return PageParams(
                PageType.CHANNEL_TIMELINE,
                channelId = channelId
            )
        }
    }

    data class Mention(

        val following: Boolean?,
        val visibility: String? = null

    ) : Pageable(), UntilPaginate, SincePaginate {
        override fun toParams(): PageParams {
            return PageParams(
                PageType.MENTION,
                following = following,
                visibility = visibility
            )
        }
    }

    data class Show(

        val noteId: String

    ) : Pageable() {
        override fun toParams(): PageParams {
            return PageParams(
                PageType.DETAIL,
                noteId = noteId
            )
        }
    }

    data class SearchByTag(

        val tag: String, var reply: Boolean? = null,
        var renote: Boolean? = null,
        var withFiles: Boolean? = null,
        var poll: Boolean? = null

    ) : Pageable(), UntilPaginate, SincePaginate, CanOnlyMedia<SearchByTag> {
        override fun toParams(): PageParams {
            return PageParams(
                PageType.SEARCH_HASH,
                tag = tag,
                reply = reply,
                renote = renote,
                withFiles = withFiles,
                poll = poll
            )
        }

        override fun setOnlyMedia(isOnlyMedia: Boolean): SearchByTag {
            return copy(
                withFiles = isOnlyMedia.takeIf {
                    it
                }
            )
        }

        override fun getOnlyMedia(): Boolean {
            return withFiles ?: false
        }
    }

    data class Featured(

        val offset: Int?

    ) : Pageable() {
        override fun toParams(): PageParams {
            return PageParams(
                PageType.FEATURED,
                offset = offset
            )
        }
    }

    data class Notification(

        var following: Boolean? = null,
        var markAsRead: Boolean? = null

    ) : Pageable(), SincePaginate, UntilPaginate {
        override fun toParams(): PageParams {
            return PageParams(
                PageType.NOTIFICATION,
                following = following,
                markAsRead = markAsRead
            )
        }
    }

    data class UserTimeline(

        val userId: String,
        var includeReplies: Boolean? = true,
        var includeMyRenotes: Boolean? = true,
        var withFiles: Boolean? = null

    ) : Pageable(), SincePaginate, UntilPaginate, CanOnlyMedia<UserTimeline> {
        override fun toParams(): PageParams {
            return PageParams(
                PageType.USER,
                includeReplies = includeReplies,
                includeMyRenotes = includeMyRenotes,
                withFiles = withFiles,
                userId = userId
            )
        }

        override fun setOnlyMedia(isOnlyMedia: Boolean): UserTimeline {
            return copy(
                withFiles = isOnlyMedia.takeIf {
                    it
                }
            )
        }

        override fun getOnlyMedia(): Boolean {
            return withFiles ?: false
        }
    }

    data class Search(

        var query: String,
        var host: String? = null,
        var userId: String? = null

    ) : Pageable(), SincePaginate, UntilPaginate {
        override fun toParams(): PageParams {
            return PageParams(
                PageType.SEARCH,
                query = query,
                host = host,
                userId = userId
            )
        }
    }

    data class Antenna(

        val antennaId: String

    ) : Pageable(), UntilPaginate, SincePaginate {
        override fun toParams(): PageParams {
            return PageParams(
                PageType.ANTENNA,
                antennaId = antennaId
            )
        }
    }

    data class ClipNotes(
        val clipId: String
    ) : Pageable(), UntilPaginate, SincePaginate {
        override fun toParams(): PageParams {
            return PageParams(
                PageType.CLIP_NOTES,
                clipId = clipId
            )
        }
    }

    object Favorite : Pageable(), UntilPaginate, SincePaginate {
        override fun toParams(): PageParams {
            return PageParams(PageType.FAVORITE)
        }
    }

    sealed class Gallery : Pageable() {
        object Posts : Gallery(), UntilPaginate, SincePaginate {
            override fun toParams(): PageParams {
                return PageParams(type = PageType.GALLERY_POSTS)
            }
        }

        object Featured : Gallery() {
            override fun toParams(): PageParams {
                return PageParams(type = PageType.GALLERY_FEATURED)
            }
        }

        object Popular : Gallery() {
            override fun toParams(): PageParams {
                return PageParams(type = PageType.GALLERY_POPULAR)
            }
        }

        data class User(val userId: String) : Gallery(), UntilPaginate, SincePaginate {
            override fun toParams(): PageParams {
                return PageParams(type = PageType.USERS_GALLERY_POSTS, userId = userId)
            }
        }

        object MyPosts : Gallery(), UntilPaginate, SincePaginate {
            override fun toParams(): PageParams {
                return PageParams(type = PageType.MY_GALLERY_POSTS)
            }
        }

        object ILikedPosts : Gallery(), UntilPaginate, SincePaginate {
            override fun toParams(): PageParams {
                return PageParams(type = PageType.I_LIKED_GALLERY_POSTS)
            }
        }
    }

    sealed class Mastodon : Pageable() {
        data class PublicTimeline(
            val isOnlyMedia: Boolean? = null,
            val excludeReplies: Boolean? = null,
            val excludeReposts: Boolean? = null,
        ) : Mastodon(), CanOnlyMedia<PublicTimeline>, UntilPaginate, SincePaginate, CanExcludeReplies<PublicTimeline>, CanExcludeReposts<PublicTimeline> {
            override fun toParams(): PageParams {
                return PageParams(
                    type = PageType.MASTODON_PUBLIC_TIMELINE,
                    withFiles = isOnlyMedia,
                    excludeReplies = excludeReplies,
                    excludeReposts = excludeReposts
                )
            }

            override fun setOnlyMedia(isOnlyMedia: Boolean): PublicTimeline {
                return copy(
                    isOnlyMedia = isOnlyMedia
                )
            }

            override fun getOnlyMedia(): Boolean {
                return isOnlyMedia ?: false
            }

            override fun setExcludeReplies(isExcludeReplies: Boolean): PublicTimeline {
                return copy(
                    excludeReplies = isExcludeReplies
                )
            }

            override fun getExcludeReplies(): Boolean {
                return excludeReplies ?: false
            }

            override fun setExcludeReposts(isExcludeReposts: Boolean): PublicTimeline {
                return copy(
                    excludeReposts = isExcludeReposts
                )
            }

            override fun getExcludeReposts(): Boolean {
                return excludeReposts ?: false
            }
        }

        data class LocalTimeline(
            val isOnlyMedia: Boolean? = null,
            val excludeReplies: Boolean? = null,
            val excludeReposts: Boolean? = null,
        ) : Mastodon(), CanOnlyMedia<LocalTimeline>, UntilPaginate, SincePaginate, CanExcludeReplies<LocalTimeline>, CanExcludeReposts<LocalTimeline> {
            override fun toParams(): PageParams {
                return PageParams(
                    type = PageType.MASTODON_LOCAL_TIMELINE,
                    withFiles = isOnlyMedia,
                    excludeReplies = excludeReplies,
                    excludeReposts = excludeReposts
                )
            }

            override fun setOnlyMedia(isOnlyMedia: Boolean): LocalTimeline {
                return copy(
                    isOnlyMedia = isOnlyMedia
                )
            }

            override fun getOnlyMedia(): Boolean {
                return isOnlyMedia ?: false
            }

            override fun setExcludeReplies(isExcludeReplies: Boolean): LocalTimeline {
                return copy(
                    excludeReplies = isExcludeReplies
                )
            }

            override fun getExcludeReplies(): Boolean {
                return excludeReplies ?: false
            }

            override fun setExcludeReposts(isExcludeReposts: Boolean): LocalTimeline {
                return copy(
                    excludeReposts = isExcludeReposts
                )
            }

            override fun getExcludeReposts(): Boolean {
                return excludeReposts ?: false
            }
        }

        data class HashTagTimeline(val hashtag: String, val isOnlyMedia: Boolean? = null) :
            Mastodon(), CanOnlyMedia<HashTagTimeline>, SincePaginate, UntilPaginate {
            override fun toParams(): PageParams {
                return PageParams(
                    type = PageType.MASTODON_TAG_TIMELINE,
                    tag = hashtag,
                    withFiles = isOnlyMedia
                )
            }

            override fun setOnlyMedia(isOnlyMedia: Boolean): HashTagTimeline {
                return copy(
                    isOnlyMedia = isOnlyMedia
                )
            }

            override fun getOnlyMedia(): Boolean {
                return isOnlyMedia ?: false
            }
        }

        data class ListTimeline(val listId: String) : Mastodon(), SincePaginate, UntilPaginate {
            override fun toParams(): PageParams {
                return PageParams(
                    type = PageType.MASTODON_LIST_TIMELINE,
                    listId = listId,
                )
            }
        }

        data class HomeTimeline(
            val excludeReplies: Boolean? = null,
            val excludeReposts: Boolean? = null,
        ) : Mastodon(), SincePaginate, UntilPaginate, CanExcludeReposts<HomeTimeline>, CanExcludeReplies<HomeTimeline> {
            override fun toParams(): PageParams {
                return PageParams(
                    type = PageType.MASTODON_HOME_TIMELINE,
                    excludeReplies = excludeReplies,
                    excludeReposts = excludeReposts
                )
            }

            override fun getExcludeReposts(): Boolean {
                return excludeReposts ?: false
            }

            override fun getExcludeReplies(): Boolean {
                return excludeReplies ?: false
            }

            override fun setExcludeReplies(isExcludeReplies: Boolean): HomeTimeline {
                return copy(
                    excludeReplies = isExcludeReplies
                )
            }

            override fun setExcludeReposts(isExcludeReposts: Boolean): HomeTimeline {
                return copy(
                    excludeReposts = isExcludeReposts
                )
            }
        }

        data class UserTimeline(
            val userId: String,
            val isOnlyMedia: Boolean? = null,
            val excludeReplies: Boolean? = null,
            val excludeReblogs: Boolean? = null,
        ) : Mastodon(), CanOnlyMedia<UserTimeline>, SincePaginate, UntilPaginate {
            override fun toParams(): PageParams {
                return PageParams(
                    type = PageType.MASTODON_USER_TIMELINE,
                    withFiles = isOnlyMedia,
                    includeReplies = excludeReplies?.not(),
                    includeMyRenotes = excludeReblogs?.not(),
                    userId = userId
                )
            }

            override fun setOnlyMedia(isOnlyMedia: Boolean): UserTimeline {
                return copy(isOnlyMedia = isOnlyMedia)
            }

            override fun getOnlyMedia(): Boolean {
                return isOnlyMedia ?: false
            }
        }

        object BookmarkTimeline : Mastodon(), SincePaginate, UntilPaginate {
            override fun toParams(): PageParams {
                return PageParams(
                    type = PageType.MASTODON_BOOKMARK_TIMELINE
                )
            }
        }

        data class SearchTimeline(
            val query: String,
            val userId: String? = null,
        ) : Mastodon() {
            override fun toParams(): PageParams {
                return PageParams(
                    type = PageType.MASTODON_SEARCH_TIMELINE,
                    query = query,
                    userId = userId,
                )
            }
        }

        object TrendTimeline : Mastodon() {
            override fun toParams(): PageParams {
                return PageParams(

                )
            }
        }

    }

    object CalckeyRecommendedTimeline : Pageable(), UntilPaginate, SincePaginate {
        override fun toParams(): PageParams {
            return PageParams(
                type = PageType.CALCKEY_RECOMMENDED_TIMELINE,
            )
        }
    }


    abstract fun toParams(): PageParams


}

/**
 * リクエスト時にwithFiles(Misskey)の指定が可能なタイムライン種別であることを表すためのインターフェース
 * これを継承したPageableはメディアのみ、あるいはメディアを除外したリクエストを送信することができる。
 * 注意点がありMisskeyの場合デフォルトがundefined(null)になるがMastodonではfalseがデフォルトになる。
 * またMisskeyの場合trueを指定するとファイルが添付されたノートのみになり、
 * falseを指定するとファイルが添付されていないノートのみになる。
 */
interface CanOnlyMedia<T> {
    fun setOnlyMedia(isOnlyMedia: Boolean): T
    fun getOnlyMedia(): Boolean
}

interface CanExcludeReplies<T> {
    fun setExcludeReplies(isExcludeReplies: Boolean): T
    fun getExcludeReplies(): Boolean
}

interface CanExcludeReposts<T> {
    fun setExcludeReposts(isExcludeReposts: Boolean): T
    fun getExcludeReposts(): Boolean
}

interface CanExcludeIfExistsSensitiveMedia<T> {
    fun setExcludeIfExistsSensitiveMedia(isExcludeIfExistsSensitiveMedia: Boolean): T
    fun getExcludeIfExistsSensitiveMedia(): Boolean
}
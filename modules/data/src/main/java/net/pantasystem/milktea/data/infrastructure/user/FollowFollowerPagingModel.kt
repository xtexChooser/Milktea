package net.pantasystem.milktea.data.infrastructure.user

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.api.misskey.v10.MisskeyAPIV10
import net.pantasystem.milktea.api.misskey.v10.RequestFollowFollower
import net.pantasystem.milktea.api.misskey.v11.MisskeyAPIV11
import net.pantasystem.milktea.app_store.user.FollowFollowerPagingStore
import net.pantasystem.milktea.app_store.user.RequestType
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.toUser
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton


class FollowFollowerPagingStoreImpl(
    override val type: RequestType,
    val userDataSource: UserDataSource,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val getAccount: GetAccount,
    val loggerFactory: Logger.Factory,
    val noteDataSourceAdder: NoteDataSourceAdder,
) : FollowFollowerPagingStore {

    class Factory @Inject constructor(
        val userDataSource: UserDataSource,
        val misskeyAPIProvider: MisskeyAPIProvider,
        val loggerFactory: Logger.Factory,
        val getAccount: GetAccount,
        val noteDataSourceAdder: NoteDataSourceAdder
    ) : FollowFollowerPagingStore.Factory {
        override fun create(type: RequestType): FollowFollowerPagingStore {
            return FollowFollowerPagingStoreImpl(
                type,
                misskeyAPIProvider = misskeyAPIProvider,
                loggerFactory = loggerFactory,
                getAccount = getAccount,
                userDataSource = userDataSource,
                noteDataSourceAdder = noteDataSourceAdder
            )
        }
    }

    private val factory: PaginatorFactory = PaginatorFactory(
        getAccount = getAccount,
        logger = loggerFactory,
        misskeyAPIProvider = misskeyAPIProvider,
    )
    private val _state =
        MutableStateFlow<PageableState<List<User.Id>>>(PageableState.Loading.Init())
    override val state: StateFlow<PageableState<List<User.Id>>>
        get() = _state

    @OptIn(ExperimentalCoroutinesApi::class)
    override val users: Flow<List<User.Detail>>
        get() = state.map {
            it.content
        }.filter {
            it is StateContent.Exist
        }.flatMapLatest { stateContent ->
            val ids = (stateContent as StateContent.Exist).rawContent
            val accountId = ids.map { it.accountId }.distinct().first()
            userDataSource.observeIn(accountId, ids.map { it.id }).map { list ->
                list.mapNotNull { user ->
                    user as User.Detail?
                }
            }.map {
                val userMap = it.associateBy { it.id }
                ids.mapNotNull { userId ->
                    userMap[userId]
                }
            }
        }.catch {
            loggerFactory.create("FollowFollowerPagingModel").error("error", it)
        }

    private val idHolder = IdHolder()
    private val mutex = Mutex()

    override suspend fun loadPrevious() {

        mutex.withLock {
            runCancellableCatching {
                val s = _state.value
                _state.value = when (s) {
                    is PageableState.Loading.Init -> s
                    else -> PageableState.Loading.Previous(s.content)
                }
                val res = factory.create(type, idHolder).next()

                val account = getAccount.get(type.userId.accountId)
                val users = res.map {
                    it.pinnedNotes?.map { noteDTO ->
                        noteDataSourceAdder.addNoteDtoToDataSource(account, noteDTO)
                    }
                    it.toUser(account, true)
                }
                userDataSource.addAll(users)
                users.map { it.id }
            }.onFailure {
                _state.value = PageableState.Error(
                    _state.value.content,
                    it
                )
            }.onSuccess { responseUserIds ->
                val list = ((_state.value.content as? StateContent.Exist)?.rawContent
                    ?: emptyList())
                val newList = list.toMutableList().also { mutableList ->
                    mutableList.addAll(responseUserIds)
                }
                _state.value = PageableState.Fixed(
                    StateContent.Exist(newList)
                )
            }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            factory.create(type, idHolder).init()
            _state.value = PageableState.Fixed(StateContent.NotExist())
        }
    }
}

internal interface Paginator {
    companion object

    val idHolder: IdHolder
    suspend fun next(): List<UserDTO>
    suspend fun init()
}


@Singleton
internal class PaginatorFactory @Inject constructor(
    val misskeyAPIProvider: MisskeyAPIProvider,
    val getAccount: GetAccount,

    val logger: Logger.Factory,
) {
    suspend fun create(type: RequestType, idHolder: IdHolder): Paginator {
        val account = getAccount.get(type.userId.accountId)
        val api = misskeyAPIProvider.get(account)
        return if (api is MisskeyAPIV10) {
            V10Paginator(
                account,
                api,
                type,
                idHolder,
            )
        } else {
            DefaultPaginator(
                account,
                api as MisskeyAPIV11,
                type,
                logger.create("DefaultPaginator"),
                idHolder,
            )
        }

    }
}


internal class IdHolder(
    var nextId: String? = null
)

@Suppress("BlockingMethodInNonBlockingContext")
internal class DefaultPaginator(
    val account: Account,
    private val misskeyAPI: MisskeyAPIV11,
    val type: RequestType,
    private val logger: Logger?,
    override val idHolder: IdHolder,

    ) : Paginator {


    private val api =
        if (type is RequestType.Follower) misskeyAPI::followers else misskeyAPI::following

    override suspend fun next(): List<UserDTO> {
        logger?.debug("next: ${idHolder.nextId}")
        val res = api.invoke(
            RequestUser(
                account.token,
                userId = type.userId.id,
                untilId = idHolder.nextId
            )
        ).body()
            ?: return emptyList()
        idHolder.nextId = res.last().id
        require(idHolder.nextId != null)
        return res.mapNotNull {
            it.followee ?: it.follower
        }
    }

    override suspend fun init() {
        idHolder.nextId = null
    }
}


@Suppress("BlockingMethodInNonBlockingContext")
internal class V10Paginator(
    val account: Account,
    private val misskeyAPIV10: MisskeyAPIV10,
    val type: RequestType,
    override val idHolder: IdHolder,
) : Paginator {
    private val api =
        if (type is RequestType.Follower) misskeyAPIV10::followers else misskeyAPIV10::following

    override suspend fun next(): List<UserDTO> {
        val res = api.invoke(
            RequestFollowFollower(
                i = account.token,
                cursor = idHolder.nextId,
                userId = type.userId.id
            )
        ).body() ?: return emptyList()
        idHolder.nextId = res.next
        return res.users
    }

    override suspend fun init() {
        idHolder.nextId = null
    }
}

class FollowFollowerPagingModelImpl (
    val requestType: RequestType,
    val getAccount: GetAccount,
    val userDataSource: UserDataSource,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val mastodonAPIProvider: MastodonAPIProvider,
        ): StateLocker,
    PreviousLoader<FollowFollowerResponseItemType>,
    EntityConverter<FollowFollowerResponseItemType, UserIdAndNextId>,
    PaginationState<UserIdAndNextId>,
    IdGetter<String>
{
    override val mutex: Mutex = Mutex()

    private val _state = MutableStateFlow<PageableState<List<UserIdAndNextId>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<UserIdAndNextId>>> = _state
    override suspend fun convertAll(list: List<FollowFollowerResponseItemType>): List<UserIdAndNextId> {
        val account = getAccount.get(requestType.userId.accountId)
        val users = list.map {
            when(it) {
                is FollowFollowerResponseItemType.Default -> {
                    it.userDTO.toUser(account, true)
                }
                is FollowFollowerResponseItemType.Mastodon -> {
                    it.userDTO.toModel(account)
                }
                is FollowFollowerResponseItemType.V10 -> {
                    it.userDTO.toUser(account, true)
                }
            }
        }
        userDataSource.addAll(users)
        return list.map {
            it.toUserIdAndNextId(account.accountId)
        }
    }

    override suspend fun loadPrevious(): Result<List<FollowFollowerResponseItemType>> = runCancellableCatching{
        val account = getAccount.get(requestType.userId.accountId)
        when(account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                when(misskeyAPIProvider.get(account)) {
                    is MisskeyAPIV11 -> {
                        DefaultLoader(
                            account,
                            misskeyAPIProvider,
                            this@FollowFollowerPagingModelImpl
                        )
                    }
                    is MisskeyAPIV10 -> {
                        V10Loader(
                            account,
                            misskeyAPIProvider,
                            this@FollowFollowerPagingModelImpl
                        )
                    }
                    else -> throw IllegalStateException("not support follow follower list")
                }
            }
            Account.InstanceType.MASTODON -> {
                MastodonLoader(account, mastodonAPIProvider = mastodonAPIProvider, this@FollowFollowerPagingModelImpl)
            }
        }.loadPrevious().getOrThrow()
    }

    override fun getState(): PageableState<List<UserIdAndNextId>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<UserIdAndNextId>>) {
        _state.value = state
    }

    override suspend fun getSinceId(): String? {
        return null
    }

    override suspend fun getUntilId(): String? {
        return (_state.value.content as? StateContent.Exist)?.rawContent?.lastOrNull()?.nextId
    }

}

class V10Loader(
    val account: Account,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val idGetter: IdGetter<String>,
) : PreviousLoader<FollowFollowerResponseItemType> {
    override suspend fun loadPrevious(): Result<List<FollowFollowerResponseItemType>> {
        val api = misskeyAPIProvider.get(account) as MisskeyAPIV10
        TODO("Not yet implemented")
    }
}

class DefaultLoader(
    val account: Account,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val idGetter: IdGetter<String>,
) : PreviousLoader<FollowFollowerResponseItemType> {
    override suspend fun loadPrevious(): Result<List<FollowFollowerResponseItemType>> {
        val api = misskeyAPIProvider.get(account) as MisskeyAPIV11

        TODO("Not yet implemented")
    }
}

class MastodonLoader(
    val account: Account,
    val mastodonAPIProvider: MastodonAPIProvider,
    val idGetter: IdGetter<String>,
) : PreviousLoader<FollowFollowerResponseItemType> {
    override suspend fun loadPrevious(): Result<List<FollowFollowerResponseItemType>> {
        val api = mastodonAPIProvider.get(account)
        TODO("Not yet implemented")
    }
}

data class UserIdAndNextId(
    val userId: User.Id,
    val nextId: String?,
)

fun FollowFollowerResponseItemType.toUserIdAndNextId(accountId: Long): UserIdAndNextId {
    return UserIdAndNextId(
        userId = when(this) {
            is FollowFollowerResponseItemType.Default -> User.Id(accountId, userDTO.id)
            is FollowFollowerResponseItemType.Mastodon -> User.Id(accountId, userDTO.id)
            is FollowFollowerResponseItemType.V10 -> User.Id(accountId, userDTO.id)
        },
        nextId = nextId
    )
}

sealed interface FollowFollowerResponseItemType {
    val nextId: String?

    data class Default(val userDTO: UserDTO, override val nextId: String?) :
        FollowFollowerResponseItemType

    data class V10(val userDTO: UserDTO, override val nextId: String?) :
        FollowFollowerResponseItemType

    data class Mastodon(val userDTO: MastodonAccountDTO, override val nextId: String?) :
        FollowFollowerResponseItemType
}
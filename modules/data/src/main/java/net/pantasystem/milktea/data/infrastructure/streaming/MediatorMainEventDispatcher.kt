package net.pantasystem.milktea.data.infrastructure.streaming

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.api_streaming.ChannelBody
import net.pantasystem.milktea.api_streaming.channel.ChannelAPI
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.messaging.MessageDataSource
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.infrastructure.notification.impl.NotificationCacheAdder
import net.pantasystem.milktea.data.streaming.ChannelAPIWithAccountProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton

class MediatorMainEventDispatcher(val logger: Logger) {

    @Singleton
    class Factory @Inject constructor(
        val loggerFactory: Logger.Factory,
        val messageDataSource: MessageDataSource,
        val unreadNotificationDAO: UnreadNotificationDAO,
        val userDataSource: UserDataSource,
        val streamingMainMessageEventDispatcher: StreamingMainMessageEventDispatcher,
        val notificationCacheAdder: NotificationCacheAdder,
        val userDTOEntityConverter: UserDTOEntityConverter,
    ) {

        fun create(): MediatorMainEventDispatcher {
            return MediatorMainEventDispatcher(loggerFactory.create("MediatorMainEventDispatcher"))
                .attach(
                    streamingMainMessageEventDispatcher
                )
                .attach(
                    StreamingMainNotificationEventDispatcher(
                        unreadNotificationDAO,
                        notificationCacheAdder,
                    )
                )
                .attach(StreamingMainUserEventDispatcher(userDataSource, userDTOEntityConverter))
        }
    }

    private var dispatchers = mutableSetOf<StreamingMainEventDispatcher>()

    fun attach(dispatcher: StreamingMainEventDispatcher): MediatorMainEventDispatcher {
        synchronized(dispatchers) {
            dispatchers = dispatchers.toMutableSet().also {
                it.add(dispatcher)
            }
        }
        return this
    }


    suspend fun dispatch(account: Account, mainEvent: ChannelBody.Main) {
        val iterator = dispatchers.iterator()
        while (iterator.hasNext()) {
            val result = runCancellableCatching {
                iterator.next().dispatch(account, mainEvent)
            }.getOrElse {
                false
            }
            if (result) {
                return
            }
        }
    }


}

class ChannelAPIMainEventDispatcherAdapter @Inject constructor(
    private val channelAPIProvider: ChannelAPIWithAccountProvider,
    private val accountStore: AccountStore,
    loggerFactory: Logger.Factory
) {
    val logger = loggerFactory.create("MainEventDispatcher")

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(mainDispatcher: MediatorMainEventDispatcher) {
        accountStore.state.map { it.currentAccount }.filterNotNull().filter {
            it.instanceType == Account.InstanceType.MISSKEY || it.instanceType == Account.InstanceType.FIREFISH
        }.flatMapLatest { ac ->
            requireNotNull(channelAPIProvider.get(ac)).connect(ChannelAPI.Type.Main).map { body ->
                ac to body
            }
        }.mapNotNull {
            (it.second as? ChannelBody.Main)?.let { main ->
                it.first to main
            }
        }.catch { e ->
            logger.error("Dispatch時にエラー発生", e = e)
        }.collect {
            mainDispatcher.dispatch(it.first, it.second)
        }
    }
}
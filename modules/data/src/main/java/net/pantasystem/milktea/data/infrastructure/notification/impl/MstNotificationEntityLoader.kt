package net.pantasystem.milktea.data.infrastructure.notification.impl

import net.pantasystem.milktea.common.MastodonLinkHeaderDecoder
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.IdGetter
import net.pantasystem.milktea.common.paginator.PaginationState
import net.pantasystem.milktea.common.paginator.PreviousLoader
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.model.account.Account

class MstNotificationEntityLoader(
    val account: Account,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val idGetter: IdGetter<String>,
    val state: PaginationState<NotificationAndNextId>,
) : PreviousLoader<NotificationItem> {
    override suspend fun loadPrevious(): Result<List<NotificationItem>> = runCancellableCatching {
        val nextId = idGetter.getUntilId()
        val isEmpty = (state.getState().content as? StateContent.Exist)?.rawContent.isNullOrEmpty()
        if (nextId == null && !isEmpty) {
            return@runCancellableCatching emptyList()
        }
        val res = mastodonAPIProvider.get(account).getNotifications(
            maxId = nextId
        ).throwIfHasError()

        val body = res.body()

        val maxId = MastodonLinkHeaderDecoder(res.headers()["link"]).getMaxId()

        requireNotNull(body).map {
            NotificationItem.Mastodon(
                it,
                maxId
            )
        }
    }
}

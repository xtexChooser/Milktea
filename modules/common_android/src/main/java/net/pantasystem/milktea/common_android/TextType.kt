package net.pantasystem.milktea.common_android

import net.pantasystem.milktea.common_android.mfm.Root
import net.pantasystem.milktea.common_android.html.MastodonHTML
import net.pantasystem.milktea.common_android.html.MastodonHTMLParser
import net.pantasystem.milktea.common_android.mfm.MFMParser
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation

sealed interface TextType {
    data class Misskey(val root: Root?) : TextType
    data class Mastodon(
        val html: MastodonHTML,
        val mentions: List<Note.Type.Mastodon.Mention>,
        val tags: List<Note.Type.Mastodon.Tag>
    ) : TextType
}

fun getTextType(account: Account, note: NoteRelation, instanceEmojis: List<Emoji>): TextType? {
    return when (account.instanceType) {
        Account.InstanceType.MISSKEY -> {
            note.note.text?.let {
                TextType.Misskey(
                    MFMParser.parse(
                        note.note.text, (note.note.emojis ?: emptyList()) + instanceEmojis,
                        userHost = note.user
                            .host,
                        accountHost = account.getHost()
                    )
                )
            }
        }
        Account.InstanceType.MASTODON -> {
            note.note.text?.let {
                val option = note.note.type as? Note.Type.Mastodon
                TextType.Mastodon(
                    MastodonHTMLParser.parse(
                        it, note.note.emojis ?: emptyList(), userHost = note.user
                            .host,
                        accountHost = account.getHost()
                    ),
                    tags = option?.tags ?: emptyList(),
                    mentions = option?.mentions ?: emptyList()
                )
            }
        }
    }

}
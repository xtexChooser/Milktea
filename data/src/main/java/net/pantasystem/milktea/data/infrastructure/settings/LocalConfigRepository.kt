package net.pantasystem.milktea.data.infrastructure.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import net.pantasystem.milktea.model.setting.*
import java.util.regex.Pattern




class LocalConfigRepositoryImpl(
    private val sharedPreference: SharedPreferences
) : LocalConfigRepository {
    private val urlPattern =
        Pattern.compile("""(https)(://)([-_.!~*'()\[\]a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")

    override fun get(): Result<Config> {
        return runCatching {
            Config(
                isSimpleEditorEnabled = sharedPreference.getBoolean(
                    Keys.IsSimpleEditorEnabled.str(), DefaultConfig.config.isSimpleEditorEnabled,
                ),
                reactionPickerType = sharedPreference.getInt(Keys.ReactionPickerType.str(), 0)
                    .let {
                        when (it) {
                            0 -> {
                                ReactionPickerType.LIST
                            }
                            1 -> {
                                ReactionPickerType.SIMPLE
                            }
                            else -> {
                                DefaultConfig.config.reactionPickerType
                            }
                        }
                    },
                backgroundImagePath = sharedPreference.getString(
                    Keys.BackgroundImage.str(),
                    DefaultConfig.config.backgroundImagePath
                ),
                isClassicUI = sharedPreference.getBoolean(
                    Keys.ClassicUI.str(),
                    DefaultConfig.config.isClassicUI
                ),
                isUserNameDefault = sharedPreference.getBoolean(
                    Keys.IsUserNameDefault.str(),
                    DefaultConfig.config.isUserNameDefault
                ),
                isPostButtonAtTheBottom = sharedPreference.getBoolean(
                    Keys.IsPostButtonToBottom.str(),
                    DefaultConfig.config.isPostButtonAtTheBottom
                ),
                urlPreviewConfig = UrlPreviewConfig(
                    type = getSourceType(),
                ),
                noteExpandedHeightSize = sharedPreference.getInt(
                    Keys.NoteLimitHeight.str(),
                    DefaultConfig.config.noteExpandedHeightSize
                ),
                theme = Theme.from(sharedPreference.getInt(Keys.ThemeType.str(), 0))
            )
        }
    }

    override suspend fun save(config: Config): Result<Unit> {
        return runCatching {
            val old = get().getOrThrow().prefs()
            sharedPreference.edit {
                config.prefs().filter {
                    old[it.key] == it.value
                }.map {
                    when (val entry = it.value) {
                        is PrefType.BoolPref -> putBoolean(it.key, entry.value)
                        is PrefType.IntPref -> putInt(it.key, entry.value)
                        is PrefType.StrPref -> putString(it.key, entry.value)
                    }
                }
            }
        }
    }

    fun getSourceType(): UrlPreviewConfig.Type {
        val type = sharedPreference.getInt(
            UrlPreviewSourceSetting.URL_PREVIEW_SOURCE_TYPE_KEY,
            UrlPreviewSourceSetting.MISSKEY
        )
        if (type in UrlPreviewSourceSetting.MISSKEY..UrlPreviewSourceSetting.APP) {
            return if (type == UrlPreviewSourceSetting.SUMMALY && getSummalyUrl() == null) {
                return UrlPreviewConfig.Type.SummalyServer(getSummalyUrl()!!)
            } else if (type == UrlPreviewSourceSetting.APP) {
                UrlPreviewConfig.Type.InApp
            } else {
                UrlPreviewConfig.Type.Misskey
            }

        } else {
            return UrlPreviewConfig.Type.Misskey
        }
    }

    fun getSummalyUrl(): String? {
        return sharedPreference.getString(Keys.SummalyServerUrl.str(), null)
            ?.let { url ->
                if (urlPattern.matcher(url).find()) {
                    url
                } else {
                    null
                }
            }
    }


}


package net.pantasystem.milktea.data.infrastructure.settings

import net.pantasystem.milktea.model.setting.Config
import net.pantasystem.milktea.model.setting.ReactionPickerType
import net.pantasystem.milktea.model.setting.UrlPreviewConfig


fun Config.pref(key: Keys): PrefType? {
    return when (key) {
        Keys.BackgroundImage -> {
            PrefType.StrPref(backgroundImagePath)
        }
        Keys.ClassicUI -> {
            PrefType.BoolPref(isClassicUI)
        }
        Keys.IsPostButtonToBottom -> {
            PrefType.BoolPref(isPostButtonAtTheBottom)
        }
        Keys.IsSimpleEditorEnabled -> {
            PrefType.BoolPref(isSimpleEditorEnabled)
        }
        Keys.IsUserNameDefault -> {
            PrefType.BoolPref(isUserNameDefault)
        }
        Keys.NoteLimitHeight -> {
            PrefType.IntPref(noteExpandedHeightSize)
        }
        Keys.ReactionPickerType -> {
            PrefType.IntPref(
                when (reactionPickerType) {
                    ReactionPickerType.LIST -> 0
                    ReactionPickerType.SIMPLE -> 1
                }
            )
        }
        Keys.SummalyServerUrl -> {
            val type = urlPreviewConfig.type
            if (type is UrlPreviewConfig.Type.SummalyServer) {
                PrefType.StrPref(type.url)
            } else {
                null
            }
        }
        Keys.UrlPreviewSourceType -> {
            PrefType.IntPref(urlPreviewConfig.type.toInt())
        }
        Keys.ThemeType -> {
            PrefType.IntPref(theme.toInt())
        }
    }
}

fun Config.prefs(): Map<Keys, PrefType> {
    val map = mutableMapOf<Keys, PrefType>()
    Keys.allKeys.forEach { key ->
        pref(key)?.let {
            map[key] = it
        }
    }
    return map
}
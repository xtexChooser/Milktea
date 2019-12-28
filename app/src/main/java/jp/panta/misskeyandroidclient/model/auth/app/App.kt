package jp.panta.misskeyandroidclient.model.auth.app

data class App(
    val id: String,
    val name: String,
    val callbackUrl: String,
    val isAuthorized: Boolean,
    val permission: List<String>,
    val secret: String?
)
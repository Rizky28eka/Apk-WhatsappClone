package kuy.belajar.whatsappclone.model

/**
 * Created by Imam Fahrur Rofi on 19/10/20.
 */
data class MessageModel(
    var sender: String = "",
    var receiver: String = "",
    var message: String = "",
    var isSeen: String = "",
    var url: String = "",
    var messageID: String = ""
)
package co.bdozer.investopedia

data class Investopedia(
    val id:String, 
    val uri: String,
    val seqNo:Int,
    val title: String? = null,
    val text: String,
)
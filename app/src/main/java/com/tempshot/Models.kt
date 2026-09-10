package com.tempshot
const val HOUR = 3_600_000L
const val MINUTE = 60_000L
/** Stored alongside the legacy hour values; preserves existing user preferences. */
const val LEGACY_THREE_MINUTES = -3
const val FIFTEEN_MINUTES = -15
enum class PhotoState { TEMPORARY, KEPT, TRASH, UNAVAILABLE }
enum class CleanupMode { MANUAL, AUTOMATIC }
enum class Source(val label: String, val description: String) {
 CAMERA("拍照图片", "随手记录，不必一直占据相册"), SCREENSHOT("屏幕截图", "用过的信息，到期轻松放下"), SAVED("其他 APP 保存", "临时保存的图片，也有归期")
}
data class Photo(val id: String, val title: String, val source: Source, val added: Long, val expires: Long,
 val state: PhotoState = PhotoState.TEMPORARY, val uri: String = "", val folder: String = "", val restored: Boolean = false)
data class Rule(val enabled: Boolean = false, val since: Long = 0)
data class Library(val duration: Int = 24, val rules: List<Rule> = List(3) { Rule() }, val photos: List<Photo> = emptyList(),
 val seen: Map<String, Boolean> = emptyMap(), val initialized: Boolean = false, val lastScan: Long = 0, val error: String = "", val cleanupMode: CleanupMode = CleanupMode.MANUAL)
data class AppState(val language: String = "zh", val device: Library = Library(), val showNoticeOnLaunch: Boolean = true) { val library: Library get() = device }
object Rules {
 fun due(l: Library, now: Long) = l.photos.filter { it.state == PhotoState.TEMPORARY && it.expires <= now }
 fun keep(p: Photo, restored: Boolean = false) = p.copy(state=PhotoState.KEPT,restored=p.restored||restored)
 fun postpone(p: Photo, now: Long) = p.copy(expires=maxOf(p.expires,now)+24*HOUR)
 fun reenter(p: Photo, duration: Int, now: Long) = p.copy(state=PhotoState.TEMPORARY,restored=false,expires=now+durationMillis(duration))
 fun classify(path: String): Source? {
  val s=path.lowercase().split('/').filter { it.isNotBlank() }
  if(s.any { it in listOf("screenshots","screenshot","截屏","截图") }) return Source.SCREENSHOT
  if(s.size>=2 && s[0]=="dcim" && s[1]=="camera") return Source.CAMERA
  if(s.firstOrNull() in listOf("pictures","download","downloads")) return Source.SAVED
  return null
 }
}
fun durationMillis(duration: Int) = when(duration) {LEGACY_THREE_MINUTES,FIFTEEN_MINUTES->15*MINUTE;else->duration*HOUR}
fun durationLabel(duration: Int) = when(duration) {LEGACY_THREE_MINUTES,FIFTEEN_MINUTES->"15 分钟";12->"12 小时";24->"1 天";72->"3 天";else->"1 周"}
fun timeUntil(expires:Long,now:Long):String {
 val m=(expires-now+59_999)/60_000
 return when {
  m<60 -> "$m 分钟"
  m<1440 && m%60==0L -> "${m/60} 小时"
  m<1440 -> "${m/60} 小时 ${m%60} 分钟"
  m%1440==0L -> "${m/1440} 天"
  else -> "${m/1440} 天 ${m%1440/60} 小时"
 }
}
fun expirySummary(photos:List<Photo>,now:Long):String {
 if(photos.isEmpty()) return "暂无临时图片"
 val expired=photos.count {it.expires<=now}
 if(expired>0) return "$expired 张图片已到期"
 val next=photos.minOf {it.expires}
 return "最早 ${timeUntil(next,now)}后到期"
}
fun remaining(p: Photo, now: Long): String {
 if(p.state==PhotoState.KEPT) return if(p.restored) "已恢复并保留" else "已保留 · 不会自动清理"
 if(p.state==PhotoState.TRASH) return "已移入回收站"
 if(p.state==PhotoState.UNAVAILABLE) return "暂不可访问 · 已暂停清理"
 if(p.expires<=now) return "已到期 · 等待处理"
 return "${timeUntil(p.expires,now)}后到期"
}

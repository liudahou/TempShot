package com.tempshot

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

val LocalLanguage=staticCompositionLocalOf { "zh" }

/** App-owned copy only. User filenames and paths are rendered without translation. */
object Words {
 private val english=mapOf(
  "一刻相册" to "TempShot", "一刻相册 Logo" to "TempShot logo",
  "重要提示" to "Important notice", "我同意" to "I agree", "拒绝使用 APP" to "Decline and exit", "不再提示" to "Don't show again",
  "本APP不会请求网络访问权限。获得授权的照片仅在本机安全读取、分类和处理。" to "This app does not request network access. Authorized photos are read, classified, and processed securely on this device only.",
  "本APP会根据你设置的规则尝试处理到期图片并移入系统回收站。请在操作前仔细确认设置；因误操作、错误配置、系统或设备差异等造成的任何损失，由用户自行承担。" to "This app uses your rules to process expired photos and move them to the system trash. Review your settings carefully before taking action. You accept responsibility for any loss caused by mistakes, incorrect settings, or system and device differences.",
  "为保证应用正常运行，请在系统最近任务中锁定本APP，并允许其在后台运行。系统省电限制、强行停止或设备厂商的清理机制仍可能导致处理延迟。" to "To help the app work normally, lock it in the system's recent apps screen and allow background activity. Battery restrictions, force stop, or device-maker cleanup mechanisms may still delay processing.",
  "如有任何问题或意见，欢迎联系作者liudahou666@gmail.com😊" to "If you have any questions or feedback, feel free to contact the author at liudahou666@gmail.com 😊",
  "管理" to "Manage", "临时相册" to "Temporary album",
  "照片访问已授权" to "Full photo access granted", "仅部分照片已授权" to "Selected photos only", "照片访问未授权" to "Photo access not granted",
  "免确认清理：已授权 · 后台到期提醒" to "Media access granted · Expiry reminders",
  "免确认清理：未授权 · 后台到期提醒" to "Confirmation required · Expiry reminders",
  "让相册，轻一点。" to "A little less to keep.", "只留下重要的，临时的就交给一刻。" to "Keep what matters. Let the rest be temporary.",
  "正在照看的临时图片" to "Temporary photos in your care", "张照片" to "photos", "查看相册 ↗" to "View album ↗",
  "暂无临时图片" to "No temporary photos",
  "保留多久" to "Keep for", "三类图片共用" to "Shared across all sources",
  "15 分钟" to "15 min", "12 小时" to "12 h", "1 天" to "1 day", "3 天" to "3 days", "1 周" to "1 week",
  "调整后仅对新加入的图片生效，已有图片期限不变。" to "Changes apply to new photos only. Existing deadlines stay the same.",
  "模式切换" to "Cleanup mode", "到期后的处理方式" to "What happens when photos expire",
  "手动管理" to "Manual", "自动管理" to "Automatic", "到期后通知，确认后处理" to "Notify you, then wait for confirmation", "到期后自动移入回收站" to "Move to trash automatically when due",
  "自动管理将在下一次后台检查时尝试移入回收站；系统可能延后执行。" to "Automatic mode will try to move due photos to trash during the next background check; the system may delay it.",
  "手动管理不会自动移动图片，需由你确认后处理。" to "Manual mode never moves photos automatically; you confirm before handling them.",
  "已开启自动管理" to "Automatic mode enabled", "已切换为手动管理" to "Switched to manual mode", "自动管理需要完整照片访问和媒体管理授权" to "Automatic mode requires full photo access and media management permission",
  "自动纳入" to "Add automatically", "仅开启后新增的图片" to "New photos only",
  "拍照图片" to "Camera photos", "拍照" to "Camera", "屏幕截图" to "Screenshots", "其他 APP 保存" to "Saved from apps",
  "随手记录，不必一直占据相册" to "Everyday moments, without the clutter",
  "用过的信息，到期轻松放下" to "Let go of information once you are done",
  "临时保存的图片，也有归期" to "A deadline for your saved images",
  "自动纳入拍照图片" to "Automatically add camera photos", "自动纳入屏幕截图" to "Automatically add screenshots", "自动纳入其他 APP 保存" to "Automatically add saved images",
  "请先授予完整照片访问并刷新，建立历史图片基线" to "Grant full photo access and refresh before enabling this rule.",
  "已开启，仅纳入之后的新图片" to "Enabled. Only new photos will be added.",
  "已停止纳入，已有图片期限不变" to "Stopped adding photos. Existing deadlines stay the same.",
  "从回收站恢复的图片将保留，不再自动纳入。\n按目录识别来源，未知目录及收藏图片不自动纳入。" to "Restored photos are kept and excluded from automatic cleanup.\nSources are identified by folder. Favorites and unknown folders are excluded.",
  "尚未检查手机相册" to "Your photo library has not been checked yet", "正在检查…" to "Checking…", "立即检查相册" to "Check photo library",
  "此刻需要的，暂时留在这里。" to "Here for now, for as long as you need.", "检查中…" to "Checking…", "刷新" to "Refresh",
  "临时" to "Temporary", "即将到期" to "Due soon", "已保留" to "Kept", "回收站" to "Trash", "不可访问" to "Unavailable",
  "本 APP 跟踪的回收站图片" to "Trash tracked by TempShot", "按到期时间排序" to "Sorted by expiry",
  "这里空空的，也挺好。" to "A little breathing room.", "开启规则后，新的图片会出现在这里。" to "Enable a rule and new photos will appear here.",
  "短暂停留，也是美好的记录。" to "Some moments only need a short stay.", "处理到期图片" to "Review expired photos",
  "后台到期会通知你；系统限制可能导致执行延后。" to "We will notify you when photos expire. System restrictions may cause delays.",
  "图片详情" to "Photo details", "关闭" to "Close", "名称" to "Name", "来源" to "Source", "目录" to "Folder", "加入时间" to "Added",
  "♡ 保留这张图片" to "♡ Keep this photo", "立即移入回收站" to "Move to trash now",
  "已保留，不会再自动清理" to "Kept. This photo will not be cleaned up automatically.",
  "恢复并保留" to "Restore and keep", "恢复后不会重新纳入临时相册。" to "Restored photos will not be added again automatically.",
  "主动重新设为临时图片" to "Make temporary again", "已按当前保留时间重新计时" to "Restarted with your current retention period",
  "图片可能被移走、删除，或访问权限发生变化。不会对它执行清理。" to "This photo may have been moved or deleted, or access may have changed. Cleanup is paused.", "刷新相册" to "Refresh library",
  "授权管理" to "Permissions", "照片访问" to "Photo access", "免确认清理" to "Media management", "完整授权" to "Full access", "仅部分照片，自动纳入暂停" to "Selected photos only; automatic adding is paused",
  "未授权" to "Not granted", "已授权" to "Granted", "到期通知" to "Expiry notifications", "已开启" to "On", "未开启" to "Off", "设置" to "Settings", "完成" to "Done",
  "本 APP 不联网，不会上传照片；图片仅在本机处理和保留，以保护你的隐私。完整访问用于发现新增图片，媒体管理权限用于减少系统确认。" to "This app does not connect to the internet or upload photos. Your images are processed and kept only on this device to protect your privacy. Full access finds new photos; media management reduces confirmation prompts.",
  "本版本后台仅发送到期提醒，不会绕过系统后台限制。未授权媒体管理时，清理需要系统确认。" to "Background work sends reminders only. Cleanup requires system confirmation unless media management is granted.",
  "打开系统应用设置" to "Open system app settings", "移入回收站" to "Move to trash", "稍后处理" to "Later",
  "已恢复并保留" to "Restored and kept", "已保留 · 不会自动清理" to "Kept · No automatic cleanup", "已移入回收站" to "Moved to trash",
  "暂不可访问 · 已暂停清理" to "Unavailable · Cleanup paused", "已到期 · 等待处理" to "Expired · Awaiting review",
  "照片访问不足，自动纳入已暂停；请授权完整照片访问。" to "Automatic adding is paused. Please grant full photo access.",
  "相册权限已变化，请重新授权后刷新。" to "Photo access changed. Grant access again, then refresh.",
  "暂时无法读取相册，请稍后刷新；未更改清理状态。" to "Unable to read the library. Try refreshing later; cleanup status is unchanged.",
  "本地记录无法读取；未自动纳入历史图片。" to "Unable to read saved records. Existing photos have not been added automatically.",
  "图片到期提醒" to "Photo expiry reminders", "点击打开一刻相册，移入系统回收站。" to "Open TempShot to move expired photos to system trash.", "自动管理未完成，请打开 APP 检查授权和图片状态。" to "Automatic cleanup did not finish. Open TempShot to check permissions and photo status.",
  "已恢复并保留，不再自动纳入" to "Restored and kept. Excluded from automatic adding.", "已移入系统回收站" to "Moved to system trash",
  "系统操作已返回，请查看图片最新状态" to "System operation finished. Check the latest photo status.",
  "系统操作已返回，暂无法核对相册状态，请刷新" to "System operation finished. Refresh to verify the photo status.",
  "操作已取消，图片状态未更改" to "Canceled. Photo status is unchanged.",
  "此设备未提供媒体管理授权入口" to "Media management settings are unavailable on this device",
  "请在系统通知设置中管理提醒" to "Manage reminders in system notification settings",
  "暂无到期图片" to "No expired photos", "正在同步相册，请稍后重试" to "Syncing your library. Please try again shortly.",
  "请先授予完整照片访问权限" to "Please grant full photo access first",
  "无法核对图片状态，已停止操作" to "Unable to verify the photo status. Operation stopped.",
  "图片状态已变化，请重新查看" to "Photo status changed. Please review it again.",
  "系统暂不允许此操作，图片未标记为已清理" to "The system blocked this operation. Photos were not marked as cleaned up.",
  "回收站图片" to "Photo in trash", "预览暂不可用" to "Preview unavailable"
 )
 private val dynamic=listOf<Pair<Regex,(MatchResult)->String>>(
  Regex("最早 (\\d+) 分钟后到期") to { "Earliest expires in ${it.groupValues[1]} minutes" },
  Regex("最早 (\\d+) 小时后到期") to { "Earliest expires in ${it.groupValues[1]} hours" },
  Regex("最早 (\\d+) 小时 (\\d+) 分钟后到期") to { "Earliest expires in ${it.groupValues[1]}h ${it.groupValues[2]}m" },
  Regex("最早 (\\d+) 天后到期") to { "Earliest expires in ${it.groupValues[1]} days" },
  Regex("最早 (\\d+) 天 (\\d+) 小时后到期") to { "Earliest expires in ${it.groupValues[1]}d ${it.groupValues[2]}h" },
  Regex("(\\d+) 张图片") to { "${it.groupValues[1]} ${if(it.groupValues[1]=="1") "photo" else "photos"}" },
  Regex("(\\d+) 张临时图片已到期") to { "${it.groupValues[1]} temporary ${if(it.groupValues[1]=="1") "photo has" else "photos have"} expired" },
  Regex("(\\d+) 张图片已到期") to { "${it.groupValues[1]} ${if(it.groupValues[1]=="1") "photo has" else "photos have"} expired" },
  Regex("(\\d+) 天 (\\d+) 小时后到期") to { "Expires in ${it.groupValues[1]}d ${it.groupValues[2]}h" },
  Regex("(\\d+) 分钟后到期") to { "Expires in ${it.groupValues[1]} minutes" },
  Regex("(\\d+) 小时后到期") to { "Expires in ${it.groupValues[1]} hours" },
  Regex("(\\d+) 天后到期") to { "Expires in ${it.groupValues[1]} days" },
  Regex("(\\d+) 天 (\\d+) 小时后到期") to { "Expires in ${it.groupValues[1]}d ${it.groupValues[2]}h" },
  Regex("(\\d+) 小时 (\\d+) 分钟后到期") to { "Expires in ${it.groupValues[1]}h ${it.groupValues[2]}m" },
  Regex("最近检查：(.+)") to { "Last checked: ${it.groupValues[1]}" },
  Regex("新图片将保留 (.+)") to { "New photos will be kept for ${translate(it.groupValues[1],"en")}" },
  Regex("预计 (.+) 到期。后台到期提醒，打开 APP 后处理。") to { "Expires ${it.groupValues[1]}. Open the app to review after the reminder." },
  Regex("(\\d+) 张图片已到期。将请求移入系统回收站；每次最多处理 100 张。") to { "${it.groupValues[1]} photos have expired. Move them to system trash, up to 100 at a time." }
 )
 fun translate(text:String,language:String):String {
  if(language!="en")return text
  english[text]?.let{return it}
  dynamic.forEach { (pattern,format)->pattern.matchEntire(text)?.let{return format(it)} }
  return text
 }
}

@Composable fun UiText(text:String,modifier:Modifier=Modifier,color:Color=Color.Unspecified,fontSize:TextUnit=TextUnit.Unspecified,
 fontWeight:FontWeight?=null,letterSpacing:TextUnit=TextUnit.Unspecified,textAlign:TextAlign?=null,lineHeight:TextUnit=TextUnit.Unspecified,
 overflow:TextOverflow=TextOverflow.Clip,maxLines:Int=Int.MAX_VALUE) {
 Text(Words.translate(text,LocalLanguage.current),modifier=modifier,color=color,fontSize=fontSize,fontWeight=fontWeight,letterSpacing=letterSpacing,textAlign=textAlign,lineHeight=lineHeight,overflow=overflow,maxLines=maxLines)
}

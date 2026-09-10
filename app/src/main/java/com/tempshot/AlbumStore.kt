package com.tempshot
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
class AlbumStore internal constructor(context: Context) {
 private val prefs=context.getSharedPreferences("tempshot-v1",Context.MODE_PRIVATE)
 private val mutable=MutableStateFlow(load())
 val state=mutable.asStateFlow()
 // Rewrite the legacy envelope, retaining only device records and language.
 init { prefs.edit().putString("state",encode(mutable.value).toString()).apply() }
 @Synchronized fun update(block: (AppState)->AppState) { val next=block(mutable.value); prefs.edit().putString("state",encode(next).toString()).apply(); mutable.value=next }
 fun library(block:(Library)->Library)=update { it.copy(device=block(it.device)) }
 fun changePhoto(id:String,block:(Photo)->Photo)=library { it.copy(photos=it.photos.map { p->if(p.id==id) block(p) else p }) }
 private fun load():AppState=try { prefs.getString("state",null)?.let { raw-> val j=JSONObject(raw); AppState(if(j.optString("language")=="en") "en" else "zh",decodeLibrary(j.getJSONObject("device"))) } ?: AppState() } catch(_:Exception){ AppState(device=Library(error="本地记录无法读取；未自动纳入历史图片。")) }
 private fun encode(s:AppState)=JSONObject().put("language",s.language).put("device",encodeLibrary(s.device))
 private fun encodeLibrary(l:Library):JSONObject=JSONObject().put("duration",l.duration).put("initialized",l.initialized).put("lastScan",l.lastScan).put("error",l.error).put("cleanupMode",l.cleanupMode.name)
  .put("rules",JSONArray().also { a->l.rules.forEach { a.put(JSONObject().put("enabled",it.enabled).put("since",it.since)) } })
  .put("seen",JSONObject(l.seen)).put("photos",JSONArray().also { a->l.photos.forEach { p->a.put(JSONObject().put("id",p.id).put("title",p.title).put("source",p.source.ordinal).put("added",p.added).put("expires",p.expires).put("status",p.state.name).put("uri",p.uri).put("folder",p.folder).put("restored",p.restored)) } })
 private fun decodeLibrary(j:JSONObject):Library { val r=j.getJSONArray("rules");val p=j.getJSONArray("photos");val seen=j.optJSONObject("seen")?:JSONObject()
 val mode=runCatching { CleanupMode.valueOf(j.optString("cleanupMode",CleanupMode.MANUAL.name)) }.getOrDefault(CleanupMode.MANUAL)
 val duration=if(j.getInt("duration")==LEGACY_THREE_MINUTES) FIFTEEN_MINUTES else j.getInt("duration")
 return Library(duration,List(3){i->r.getJSONObject(i).let { Rule(it.getBoolean("enabled"),it.getLong("since")) }},List(p.length()){i->p.getJSONObject(i).let { Photo(it.getString("id"),it.getString("title"),Source.entries[it.getInt("source")],it.getLong("added"),it.getLong("expires"),PhotoState.valueOf(it.getString("status")),it.optString("uri"),it.optString("folder"),it.optBoolean("restored")) }},seen.keys().asSequence().associateWith {seen.getBoolean(it)},j.optBoolean("initialized"),j.optLong("lastScan"),j.optString("error"),mode) }
 companion object { @Volatile private var instance:AlbumStore?=null; fun get(context:Context):AlbumStore=instance?:synchronized(this){instance?:AlbumStore(context.applicationContext).also{instance=it}} }
}

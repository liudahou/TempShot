package com.tempshot
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class MigrationLanguageTest {
 @Test fun legacyDemoSelectionDoesNotDiscardDeviceRecords(){
  val context=ApplicationProvider.getApplicationContext<Context>()
  val prefs=context.getSharedPreferences("tempshot-v1",Context.MODE_PRIVATE)
  prefs.edit().clear().commit()
  val expected=sampleLibrary().copy(duration=72,seen=mapOf("a" to true),rules=listOf(Rule(true,1234),Rule(),Rule()))
  AlbumStore(context).update{AppState(device=expected)}
  val legacy=JSONObject(prefs.getString("state",null)!!).apply{remove("language");put("demo",true);put("samples","invalid-old-demo-data");put("read",true);put("manage",true)}
  prefs.edit().putString("state",legacy.toString()).commit()
  val migrated=AlbumStore(context).state.value
  assertEquals(expected,migrated.device)
  assertEquals("zh",migrated.language)
  val saved=JSONObject(prefs.getString("state",null)!!)
  assertFalse(saved.has("demo"));assertFalse(saved.has("samples"))
 }
 @Test fun englishPersistsWithoutChangingDeadlines(){
  val context=ApplicationProvider.getApplicationContext<Context>()
  val store=AlbumStore(context)
  val original=sampleLibrary()
  store.update{AppState(device=original)}
  store.update{it.copy(language="en")}
  val reloaded=AlbumStore(context).state.value
  assertEquals("en",reloaded.language);assertEquals(original,reloaded.device)
 }
 @Test fun defaultIsChineseAndEmpty(){assertEquals("zh",AppState().language);assertTrue(AppState().device.photos.isEmpty())}
 @Test fun legacyLibraryDefaultsToManualCleanup(){
  val context=ApplicationProvider.getApplicationContext<Context>();val prefs=context.getSharedPreferences("tempshot-v1",Context.MODE_PRIVATE)
  prefs.edit().clear().commit();AlbumStore(context).update{AppState(device=sampleLibrary())}
  val legacy=JSONObject(prefs.getString("state",null)!!).apply{getJSONObject("device").remove("cleanupMode")};prefs.edit().putString("state",legacy.toString()).commit()
  assertEquals(CleanupMode.MANUAL,AlbumStore(context).state.value.device.cleanupMode)
 }
 @Test fun savedThreeMinuteChoiceMigratesToFifteenMinutes(){
  val context=ApplicationProvider.getApplicationContext<Context>();val prefs=context.getSharedPreferences("tempshot-v1",Context.MODE_PRIVATE)
  prefs.edit().clear().commit();AlbumStore(context).update{AppState(device=sampleLibrary())}
  val legacy=JSONObject(prefs.getString("state",null)!!).apply{getJSONObject("device").put("duration",LEGACY_THREE_MINUTES)};prefs.edit().putString("state",legacy.toString()).commit()
  assertEquals(FIFTEEN_MINUTES,AlbumStore(context).state.value.device.duration)
 }
 @Test fun dynamicTextAndBackgroundMessagesAreTranslated(){
  assertEquals("Expires in 5h 3m",Words.translate("5 小时 3 分钟后到期","en"))
  assertEquals("New photos will be kept for 3 days",Words.translate("新图片将保留 3 天","en"))
  assertEquals("2 temporary photos have expired",Words.translate("2 张临时图片已到期","en"))
  assertEquals("Earliest expires in 3 minutes",Words.translate("最早 3 分钟后到期","en"))
  assertEquals("已恢复并保留",Words.translate("已恢复并保留","zh"))
  assertEquals("Restored and kept",Words.translate("已恢复并保留","en"))
 }
}

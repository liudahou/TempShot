package com.tempshot
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35],qualifiers="zh-rCN-w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NativeUiTest {
 @get:org.junit.Rule val compose=createAndroidComposeRule<ComponentActivity>()
 private lateinit var store:AlbumStore
 @Before fun launch(){
  store=AlbumStore.get(ApplicationProvider.getApplicationContext())
  store.update {AppState(device=sampleLibrary())}
  compose.setContent {TempShotApp(store,0,false,{},{},{},{},{photos,restore->store.library {l->l.copy(photos=l.photos.map {p->if(photos.any{it.id==p.id}) if(restore) Rules.keep(p,true) else p.copy(state=PhotoState.TRASH) else p})}},{},{})}
  compose.onNodeWithText("我同意").assertIsDisplayed()
  compose.onNodeWithText("我同意").performClick()
 }
 private fun shot(name:String){
  compose.waitForIdle()
  compose.runOnIdle {
   val wm=Class.forName("android.view.WindowManagerGlobal")
   val instance=wm.getMethod("getInstance").invoke(null)
   val field=wm.getDeclaredField("mViews").apply{isAccessible=true}
   val roots=field.get(instance) as List<*>
   val view=(roots.lastOrNull() as? android.view.View)?:compose.activity.window.decorView
   view.invalidate()
   val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
   view.draw(android.graphics.Canvas(bitmap))
   val file=File("build/reports/screenshots/$name.png");file.parentFile?.mkdirs()
   file.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
  }
 }
 @Test fun homeAndGalleryRender(){
  compose.onNodeWithText("让相册，轻一点。").assertIsDisplayed()
  compose.onNodeWithText("15 分钟").performClick()
  Assert.assertEquals(FIFTEEN_MINUTES,store.state.value.library.duration)
  compose.onNodeWithText("3 天").performClick()
  Assert.assertEquals(72,store.state.value.library.duration)
  shot("home")
  compose.onNodeWithText("查看相册 ↗").performClick()
  compose.onNodeWithText("山间的一刻").assertIsDisplayed()
  shot("gallery")
  compose.onNodeWithText("山间的一刻").performClick()
  compose.onNodeWithText("图片详情").assertIsDisplayed()
  shot("detail")
 }
 @Test fun restoreStaysKept(){
  compose.onNodeWithText("查看相册 ↗").performClick()
  compose.onNodeWithText("山间的一刻").performClick()
  compose.onNodeWithText("立即移入回收站").performScrollTo().performClick()
  compose.onNodeWithText("回收站",useUnmergedTree=true).performClick()
  compose.onNodeWithText("山间的一刻").performClick()
  compose.onNodeWithText("恢复并保留").performScrollTo().performClick()
  compose.onNodeWithText("已保留",useUnmergedTree=true).performClick()
  compose.onNodeWithText("已恢复并保留").assertIsDisplayed()
  Assert.assertTrue(Rules.due(store.state.value.library,System.currentTimeMillis()+168*HOUR).none{it.id=="sample-0"})
 }

 @Test fun languageSwitchUpdatesUiAndPersists(){
  compose.onNodeWithText("示例体验").assertDoesNotExist()
  compose.onNodeWithText("选择使用方式").assertDoesNotExist()
  compose.onNodeWithText("language").performClick()
  compose.onNodeWithText("English").performClick()
  compose.onNodeWithText("I agree").assertIsDisplayed()
  compose.onNodeWithText("I agree").performClick()
  compose.onNodeWithText("Notice").assertIsDisplayed()
  compose.onNodeWithText("A little less to keep.").assertIsDisplayed()
  Assert.assertEquals("en",AlbumStore(ApplicationProvider.getApplicationContext()).state.value.language)
  shot("home-en")
  compose.onNodeWithText("View album ↗").performClick()
  compose.onNodeWithText("山间的一刻").assertIsDisplayed()
  compose.onNodeWithText("Refresh").assertIsDisplayed()
  shot("gallery-en")
  compose.onNodeWithText("山间的一刻").performClick()
  compose.onNodeWithText("Photo details").assertIsDisplayed()
  compose.onNodeWithText("Name").assertIsDisplayed()
  compose.onAllNodesWithText("山间的一刻")[1].assertIsDisplayed()
  shot("detail-en")
  compose.onNodeWithText("Close").performClick()
  compose.onNodeWithText("language").performClick()
  compose.onNodeWithText("中文").performClick()
  compose.onNodeWithText("我同意").assertIsDisplayed()
  compose.onNodeWithText("我同意").performClick()
  compose.onNodeWithText("此刻需要的，暂时留在这里。").assertIsDisplayed()
  Assert.assertEquals("zh",store.state.value.language)
 }
 @Test fun importantNoticeButtonReopensNotice(){
  compose.onNodeWithText("重要提示").performClick()
  compose.onNodeWithText("拒绝使用 APP").assertIsDisplayed()
  compose.onNodeWithText("为保证应用正常运行，请在系统最近任务中锁定本APP，并允许其在后台运行。系统省电限制、强行停止或设备厂商的清理机制仍可能导致处理延迟。").assertIsDisplayed()
 }
 @Test fun dontShowAgainPersistsButLanguageSwitchStillShowsNotice(){
  compose.onNodeWithText("重要提示").performClick()
  compose.onNodeWithText("不再提示").performClick()
  compose.onNodeWithText("我同意").performClick()
  Assert.assertFalse(store.state.value.showNoticeOnLaunch)
  compose.onNodeWithText("language").performClick()
  compose.onNodeWithText("English").performClick()
  compose.onNodeWithText("I agree").assertIsDisplayed()
 }
 @Test fun newInstallHasOnlyEmptyDeviceLibrary(){
  compose.runOnIdle{store.update{AppState()}}
  compose.onNodeWithText("示例体验").assertDoesNotExist()
  compose.onNodeWithText("language").assertIsDisplayed()
  compose.onNodeWithText("查看相册 ↗").performClick()
  compose.onNodeWithText("0 张图片").assertIsDisplayed()
  compose.onNodeWithText("＋ 模拟新增").assertDoesNotExist()
  compose.onNodeWithText("重置示例").assertDoesNotExist()
 }
 @Test fun permissionsAreTranslated(){
  compose.onNodeWithText("language").performClick()
  compose.onNodeWithText("English").performClick()
  compose.onNodeWithText("Photo access not granted").performClick()
  compose.onNodeWithText("Permissions").assertIsDisplayed()
  compose.onNodeWithText("Media management").performScrollTo().assertIsDisplayed()
  compose.onNodeWithText("Done").performScrollTo().performClick()
 }
}


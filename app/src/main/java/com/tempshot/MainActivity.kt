package com.tempshot

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity:ComponentActivity() {
 private lateinit var store:AlbumStore
 private val permissionRevision=mutableIntStateOf(0)
 private val noticeRevision=mutableIntStateOf(0)
 private val busy=androidx.compose.runtime.mutableStateOf(false)
 private var pendingIds=emptyList<String>()
 private var restoring=false
 private var observerRegistered=false
 private var refreshJob:kotlinx.coroutines.Job?=null
 private val observer=object:android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
  override fun onChange(selfChange:Boolean){refreshJob?.cancel();refreshJob=lifecycleScope.launch{kotlinx.coroutines.delay(700);refresh()}}
 }
 private val permissions=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionRevision.intValue++;refresh() }
 private val mediaAction=registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
  val ids=pendingIds;pendingIds=emptyList();busy.value=false
  if(result.resultCode==Activity.RESULT_OK) {
   lifecycleScope.launch {
    val success=withContext(Dispatchers.IO){DeviceMedia.scan(this@MainActivity)}
    if(success) {
     val expected=if(restoring) PhotoState.KEPT else PhotoState.TRASH
     val done=store.state.value.device.photos.count { it.id in ids && it.state==expected }
     message(if(done==ids.size) if(restoring) "已恢复并保留，不再自动纳入" else "已移入系统回收站" else "系统操作已返回，请查看图片最新状态")
    } else message("系统操作已返回，暂无法核对相册状态，请刷新")
   }
  } else message("操作已取消，图片状态未更改")
 }
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState);enableEdgeToEdge();store=AlbumStore.get(this)
  pendingIds=savedInstanceState?.getStringArrayList("pending")?.toList()?:emptyList()
  restoring=savedInstanceState?.getBoolean("restoring")?:false
  setContent { TempShotApp(store,permissionRevision.intValue,busy.value,::refresh,::requestPhotos,::requestManager,::requestNotification,::operate,::message,::declineUse,intent.getBooleanExtra("show_due",false),noticeRevision.intValue) }
  DeviceMedia.schedule(this)
 }
 override fun onStart(){super.onStart();noticeRevision.intValue++;if(DeviceMedia.fullAccess(this)){contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,true,observer);observerRegistered=true}}
 override fun onStop(){if(observerRegistered){contentResolver.unregisterContentObserver(observer);observerRegistered=false};refreshJob?.cancel();super.onStop()}
 override fun onResume(){super.onResume();permissionRevision.intValue++;if(::store.isInitialized) refresh()}
 override fun onSaveInstanceState(outState:Bundle){outState.putStringArrayList("pending",ArrayList(pendingIds));outState.putBoolean("restoring",restoring);super.onSaveInstanceState(outState)}
 private fun message(text:String){Toast.makeText(this,Words.translate(text,store.state.value.language),Toast.LENGTH_SHORT).show()}
 private fun declineUse(){finishAndRemoveTask()}
 private fun refresh(){if(!::store.isInitialized || busy.value)return;busy.value=true;lifecycleScope.launch {try{withContext(Dispatchers.IO){DeviceMedia.scan(this@MainActivity)}}finally{busy.value=false;permissionRevision.intValue++}}}
 private fun requestPhotos(){
  val p=if(Build.VERSION.SDK_INT>=34) arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
   else if(Build.VERSION.SDK_INT>=33) arrayOf(Manifest.permission.READ_MEDIA_IMAGES) else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
  permissions.launch(p)
 }
 private fun requestManager(){try {startActivity(Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA,Uri.parse("package:$packageName")))}catch(_:Exception){message("此设备未提供媒体管理授权入口")}}
 private fun requestNotification(){if(Build.VERSION.SDK_INT>=33) permissions.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) else message("请在系统通知设置中管理提醒")}
 private fun operate(photos:List<Photo>,restore:Boolean){
  if(photos.isEmpty()){message("暂无到期图片");return}
  if(busy.value){message("正在同步相册，请稍后重试");return}
  if(!DeviceMedia.fullAccess(this)){message("请先授予完整照片访问权限");return}
  // Recheck the MediaStore before requesting a destructive operation. A restored or
  // inaccessible photo must never be trashed from a stale foreground snapshot.
  busy.value=true
  lifecycleScope.launch {
   val ok=withContext(Dispatchers.IO){DeviceMedia.scan(this@MainActivity)}
   if(!ok){busy.value=false;message("无法核对图片状态，已停止操作");return@launch}
   val requested=photos.map {it.id}.toSet()
   val valid=store.state.value.device.photos.filter { it.id in requested && if(restore) it.state==PhotoState.TRASH else it.state==PhotoState.TEMPORARY }.take(100)
   if(valid.isEmpty()){busy.value=false;message("图片状态已变化，请重新查看");return@launch}
   try {
    pendingIds=valid.map {it.id};restoring=restore
    val request=MediaStore.createTrashRequest(contentResolver,valid.map {Uri.parse(it.uri)},!restore)
    mediaAction.launch(IntentSenderRequest.Builder(request.intentSender).build())
   }catch(_:Exception){pendingIds=emptyList();busy.value=false;message("系统暂不允许此操作，图片未标记为已清理")}
  }
 }
}

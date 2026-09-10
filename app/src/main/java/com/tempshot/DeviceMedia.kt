package com.tempshot

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

data class MediaRow(val key:String,val uri:String,val title:String,val folder:String,val added:Long,val trashed:Boolean,val favorite:Boolean=false)
object Reconcile {
 fun apply(old:Library,rows:List<MediaRow>,now:Long):Library {
  val seen=old.seen.toMutableMap();val photos=old.photos.associateBy { it.id }.toMutableMap()
  rows.forEach { row ->
   val previous=photos[row.key]
   val restored=old.seen[row.key]==true && !row.trashed
   if(previous!=null) {
    photos[row.key]=when {
     row.trashed -> previous.copy(state=PhotoState.TRASH)
     restored || previous.state==PhotoState.TRASH -> Rules.keep(previous,true)
     previous.added!=row.added -> Rules.keep(previous).copy(added=row.added)
     previous.state==PhotoState.UNAVAILABLE || row.favorite -> Rules.keep(previous)
     else -> previous
    }.copy(title=row.title,folder=row.folder)
   } else if(old.initialized && !seen.containsKey(row.key) && !row.trashed && !row.favorite) {
    val source=Rules.classify(row.folder)
    if(source!=null) { val rule=old.rules[source.ordinal]
     if(rule.enabled && row.added>rule.since && row.added<=now) photos[row.key]=Photo(row.key,row.title,source,row.added,row.added+durationMillis(old.duration),uri=row.uri,folder=row.folder)
    }
   }
   seen[row.key]=row.trashed
  }
  val available=rows.map { it.key }.toSet()
  photos.replaceAll { key,p -> if(key !in available) p.copy(state=PhotoState.UNAVAILABLE) else p }
  return old.copy(photos=photos.values.toList(),seen=seen,initialized=true,lastScan=now,error="")
 }
}
object DeviceMedia {
 fun fullAccess(c:Context)=ContextCompat.checkSelfPermission(c,if(Build.VERSION.SDK_INT>=33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
 fun partialAccess(c:Context)=Build.VERSION.SDK_INT>=34 && ContextCompat.checkSelfPermission(c,Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)==PackageManager.PERMISSION_GRANTED
 fun manager(c:Context)=Build.VERSION.SDK_INT>=31 && MediaStore.canManageMedia(c)
 fun trashAutomatically(c:Context,photos:List<Photo>):Boolean {
  if(!fullAccess(c)||!manager(c)||photos.isEmpty()) return false
  return try {
   // MANAGE_MEDIA suppresses the system confirmation for this MediaStore request.
   MediaStore.createTrashRequest(c.contentResolver,photos.take(100).map { android.net.Uri.parse(it.uri) },true).send()
   true
  } catch(_:PendingIntent.CanceledException) { false } catch(_:Exception) { false }
 }
 /** MediaStore applies the trash request asynchronously; do not notify failure from a stale scan. */
 fun awaitAutomaticTrash(c:Context,ids:Set<String>):Boolean {
  repeat(20) {
   if(!scan(c)) return false
   val stillTemporary=AlbumStore.get(c).state.value.device.photos.any {it.id in ids&&it.state==PhotoState.TEMPORARY}
   if(!stillTemporary) return true
   try { Thread.sleep(250) } catch(_:InterruptedException) { Thread.currentThread().interrupt();return false }
  }
  return false
 }
 @Synchronized fun scan(c:Context):Boolean {
  val store=AlbumStore.get(c)
  if(!fullAccess(c)) { store.update { it.copy(device=it.device.copy(error="照片访问不足，自动纳入已暂停；请授权完整照片访问。")) };return false }
  return try {
   val rows=mutableListOf<MediaRow>()
   for(volume in MediaStore.getExternalVolumeNames(c)) {
    val base=MediaStore.Images.Media.getContentUri(volume)
    val fields=arrayOf(MediaStore.Images.Media._ID,MediaStore.Images.Media.DISPLAY_NAME,MediaStore.Images.Media.RELATIVE_PATH,MediaStore.Images.Media.DATE_ADDED,MediaStore.Images.Media.IS_TRASHED,MediaStore.Images.Media.IS_PENDING,MediaStore.Images.Media.IS_FAVORITE)
    val args=Bundle().apply { putInt(MediaStore.QUERY_ARG_MATCH_TRASHED,MediaStore.MATCH_INCLUDE) }
    val cursor=c.contentResolver.query(base,fields,args,null) ?: error("无法读取相册索引")
    cursor.use { while(it.moveToNext()) {
     if(it.getInt(5)!=0) continue
     val added=it.getLong(3)*1000;val id=it.getLong(0);val uri=ContentUris.withAppendedId(base,id).toString()
     rows+=MediaRow("$volume:$id",uri,it.getString(1)?:"未命名图片",it.getString(2)?:"",added,it.getInt(4)==1,it.getInt(6)==1)
    } }
   }
   store.update { it.copy(device=Reconcile.apply(it.device,rows,System.currentTimeMillis())) };true
  } catch(_:SecurityException) { store.update { it.copy(device=it.device.copy(error="相册权限已变化，请重新授权后刷新。")) };false
  } catch(_:Exception) { store.update { it.copy(device=it.device.copy(error="暂时无法读取相册，请稍后刷新；未更改清理状态。")) };false }
 }
 fun schedule(c:Context) {
  WorkManager.getInstance(c).enqueueUniquePeriodicWork("tempshot-scan",ExistingPeriodicWorkPolicy.KEEP,PeriodicWorkRequestBuilder<AlbumWorker>(15,TimeUnit.MINUTES).build())
 }
}
class AlbumWorker(c:Context,params:WorkerParameters):Worker(c,params) {
 override fun doWork():Result {
  val store=AlbumStore.get(applicationContext)
  if(!DeviceMedia.scan(applicationContext)) return Result.success()
  val due=Rules.due(store.state.value.device,System.currentTimeMillis())
  val manager=applicationContext.getSystemService(NotificationManager::class.java)
  if(due.isEmpty()) {manager.cancel(101);return Result.success()}
  if(store.state.value.device.cleanupMode==CleanupMode.AUTOMATIC && DeviceMedia.manager(applicationContext)) {
   val requested=due.take(100)
   if(DeviceMedia.trashAutomatically(applicationContext,requested) && DeviceMedia.awaitAutomaticTrash(applicationContext,requested.map {it.id}.toSet())) {
    if(Rules.due(store.state.value.device,System.currentTimeMillis()).isEmpty()) {manager.cancel(101);return Result.success()}
   }
  }
  manager.createNotificationChannel(NotificationChannel("expiry",Words.translate("图片到期提醒",store.state.value.language),NotificationManager.IMPORTANCE_DEFAULT))
  if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(applicationContext,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return Result.success()
  val intent=PendingIntent.getActivity(applicationContext,0,Intent(applicationContext,MainActivity::class.java).putExtra("show_due",true),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  val automatic=store.state.value.device.cleanupMode==CleanupMode.AUTOMATIC
  val message=if(automatic) "自动管理未完成，请打开 APP 检查授权和图片状态。" else "点击打开一刻相册，移入系统回收站。"
  val notification=NotificationCompat.Builder(applicationContext,"expiry").setSmallIcon(R.drawable.ic_notification)
   .setContentTitle(Words.translate("${due.size} 张临时图片已到期",store.state.value.language)).setContentText(Words.translate(message,store.state.value.language)).setContentIntent(intent).setAutoCancel(true).setOnlyAlertOnce(true).build()
  manager.notify(101,notification)
  return Result.success()
 }
}

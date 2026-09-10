package com.tempshot

import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as DrawSize
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val thumbnails=object:LruCache<String,Bitmap>(16*1024*1024){override fun sizeOf(key:String,value:Bitmap)=value.byteCount}
@Composable fun PhotoArt(photo:Photo,modifier:Modifier=Modifier,large:Boolean=false) {
 val context=LocalContext.current
 val key=photo.uri+":"+photo.state+":"+large
 val image by produceState<Bitmap?>(null,key) { value=withContext(Dispatchers.IO){ thumbnails.get(key) ?: runCatching {context.contentResolver.loadThumbnail(Uri.parse(photo.uri),Size(if(large)1000 else 400,if(large)1000 else 400),null)}.getOrNull()?.also { thumbnails.put(key,it) } } }
 if(image!=null) Image(image!!.asImageBitmap(),photo.title,modifier,contentScale=if(large)ContentScale.Fit else ContentScale.Crop)
 else Box(modifier.background(androidx.compose.ui.graphics.Color(0xFFEAEAF3)),contentAlignment=Alignment.Center){UiText(if(photo.state==PhotoState.TRASH) "回收站图片" else "预览暂不可用",fontSize=12.sp,color=Muted)}
}

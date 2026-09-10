package com.tempshot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

val Ink=Color(0xFF262D43)
val Muted=Color(0xFF8791A6)
val Violet=Color(0xFF786BDB)
val Paper=Color(0xFFF7F8FC)
val Pale=Color(0xFFEEEBFB)
private val Hairline=Color(0xFFEAECF3)
@Composable private fun dateText(t:Long)=SimpleDateFormat(if(LocalLanguage.current=="en") "MMM d, HH:mm" else "MM月dd日 HH:mm",if(LocalLanguage.current=="en") Locale.ENGLISH else Locale.CHINA).format(Date(t))

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun TempShotApp(store:AlbumStore,permissionRevision:Int,busy:Boolean,onRefresh:()->Unit,
 onRead:()->Unit,onManage:()->Unit,onNotify:()->Unit,onTrash:(List<Photo>,Boolean)->Unit,onMessage:(String)->Unit,showDue:Boolean=false) {
 val state by store.state.collectAsState()
 val l=state.library
 val tr:(String)->String={Words.translate(it,state.language)}
 var page by rememberSaveable { mutableIntStateOf(if(showDue)1 else 0) }
 var filter by rememberSaveable { mutableStateOf(if(showDue)"soon" else "temporary") }
 var sheet by rememberSaveable { mutableStateOf("") }
 var selected by rememberSaveable { mutableStateOf<String?>(null) }
 var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
 val context=LocalContext.current
 val read=remember(permissionRevision) {DeviceMedia.fullAccess(context)}
 val partial=remember(permissionRevision){DeviceMedia.partialAccess(context)}
 val manage=remember(permissionRevision){DeviceMedia.manager(context)}
 val notified=remember(permissionRevision){NotificationManagerCompat.from(context).areNotificationsEnabled()}
 val temporary=l.photos.filter {it.state==PhotoState.TEMPORARY}
 val selectedPhoto=l.photos.find {it.id==selected}
 fun openGallery(){page=1;filter="temporary"}
 fun processDue(){val due=Rules.due(l,now);if(due.isEmpty()){onMessage("暂无到期图片");return}
  if(!read){sheet="permissions";return}
  sheet="due"
 }
 LaunchedEffect(Unit){while(true){now=System.currentTimeMillis();delay(15_000)}}
 LaunchedEffect(Unit){onRefresh()}
 BackHandler(page!=0 || selected!=null || sheet.isNotEmpty()){when{sheet.isNotEmpty()->sheet="";selected!=null->selected=null;else->page=0}}
 CompositionLocalProvider(LocalLanguage provides state.language) {
 MaterialTheme(colorScheme=lightColorScheme(primary=Violet,onPrimary=Color.White,secondary=Violet,background=Paper,surface=Color.White,onSurface=Ink,onBackground=Ink,surfaceVariant=Pale)) {
  Scaffold(containerColor=Paper,topBar={
   Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=24.dp,vertical=16.dp),verticalAlignment=Alignment.CenterVertically){
    Image(painterResource(R.drawable.brand_logo),tr("一刻相册 Logo"),Modifier.size(43.dp))
    Column(Modifier.padding(start=10.dp)){UiText("一刻相册",fontSize=19.sp,fontWeight=FontWeight.Bold,color=Ink);UiText("TEMPSHOT",fontSize=9.sp,letterSpacing=2.sp,color=Muted)}
    Spacer(Modifier.weight(1f))
    Surface(onClick={sheet="language"},color=Pale,shape=RoundedCornerShape(9.dp)){
     UiText("language",Modifier.padding(horizontal=11.dp,vertical=8.dp),fontSize=10.sp,color=Muted)
    }
   }
  },bottomBar={
   Surface(color=Color.White,shadowElevation=5.dp){Row(Modifier.fillMaxWidth().navigationBarsPadding().height(72.dp)){
    listOf("管理","临时相册").forEachIndexed { i,label ->
     Column(Modifier.weight(1f).fillMaxHeight().clickable{page=i}.semantics{contentDescription=tr(label);this.selected=page==i}.padding(top=10.dp),horizontalAlignment=Alignment.CenterHorizontally){
      Box {Mark(if(i==0)"clock" else "album",if(page==i)Violet else Muted,Modifier.size(26.dp));if(i==1 && temporary.isNotEmpty())UiText("${temporary.size}",Modifier.align(Alignment.TopEnd).offset(x=12.dp,y=(-4).dp).background(Pale,CircleShape).padding(horizontal=4.dp),fontSize=8.sp,color=Violet)}
      UiText(label,Modifier.padding(top=5.dp),fontSize=11.sp,color=if(page==i)Violet else Muted)
     }
    }
   }}
  }){padding->
   BoxWithConstraints(Modifier.padding(padding).fillMaxSize()){
    val wide=maxWidth>600.dp
    Box(Modifier.fillMaxSize(),contentAlignment=Alignment.TopCenter){
     val body=Modifier.widthIn(max=if(wide)600.dp else 480.dp).fillMaxWidth()
     if(page==0) Column(body.verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(bottom=24.dp)){
      Surface(onClick={sheet="permissions"},shape=RoundedCornerShape(16.dp),color=Color.White,border=BorderStroke(1.dp,Hairline)){
       Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){
        Box(Modifier.size(35.dp).background(Pale,RoundedCornerShape(11.dp)),contentAlignment=Alignment.Center){Mark("shield",Violet,Modifier.size(20.dp))}
        Column(Modifier.weight(1f).padding(horizontal=10.dp)){
         UiText(if(read)"照片访问已授权" else if(partial)"仅部分照片已授权" else "照片访问未授权",fontSize=12.sp,fontWeight=FontWeight.SemiBold)
         UiText("免确认清理：${if(manage)"已授权" else "未授权"} · 后台到期提醒",fontSize=10.sp,color=Muted,modifier=Modifier.padding(top=4.dp))
        };UiText("›",fontSize=23.sp,color=Muted)
       }
      }
      Spacer(Modifier.height(25.dp));UiText("让相册，轻一点。",fontSize=27.sp,fontWeight=FontWeight.Bold,letterSpacing=(-0.6).sp)
      UiText("只留下重要的，临时的就交给一刻。",fontSize=12.sp,color=Muted,modifier=Modifier.padding(top=8.dp,bottom=22.dp))
      Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp)).background(Brush.linearGradient(listOf(Color(0xFF647DE8),Color(0xFF8168E4)))).padding(23.dp)){
       Mark("clock",Color.White.copy(alpha=.10f),Modifier.align(Alignment.TopEnd).offset(x=24.dp,y=(-22).dp).size(143.dp))
       Column {UiText("正在照看的临时图片",fontSize=12.sp,color=Color(0xFFE7E7FF));Row(verticalAlignment=Alignment.Bottom,modifier=Modifier.padding(vertical=13.dp)){UiText(temporary.size.toString().padStart(2,'0'),fontSize=42.sp,fontWeight=FontWeight.SemiBold,color=Color.White);UiText("张照片",fontSize=12.sp,color=Color(0xFFE5E4FF),modifier=Modifier.padding(start=10.dp,bottom=8.dp))}
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){UiText(expirySummary(temporary,now),fontSize=11.sp,color=Color(0xFFE7E7FF),maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f).padding(end=10.dp));UiText("查看相册 ↗",fontSize=11.sp,color=Color.White,maxLines=1,textAlign=androidx.compose.ui.text.style.TextAlign.End,modifier=Modifier.width(92.dp).clickable{openGallery()}.padding(7.dp))}
       }
      }
      SectionHeading("保留多久","三类图片共用")
      Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {listOf(FIFTEEN_MINUTES,12,24,72,168).forEach { duration->
       Surface(onClick={store.library{it.copy(duration=duration)};onMessage("新图片将保留 ${durationLabel(duration)}")},modifier=Modifier.weight(1f),shape=RoundedCornerShape(12.dp),color=if(l.duration==duration)Pale else Color.White,border=BorderStroke(1.dp,if(l.duration==duration)Color(0xFFCDC5F3) else Hairline)){
        UiText(durationLabel(duration),Modifier.padding(vertical=14.dp).fillMaxWidth(),textAlign=androidx.compose.ui.text.style.TextAlign.Center,fontSize=12.sp,color=if(l.duration==duration)Violet else Muted,fontWeight=if(l.duration==duration)FontWeight.SemiBold else FontWeight.Normal)
       }
      }}
      UiText("调整后仅对新加入的图片生效，已有图片期限不变。",fontSize=10.sp,color=Muted,modifier=Modifier.padding(top=10.dp),lineHeight=17.sp)
      SectionHeading("模式切换","到期后的处理方式")
      Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
       listOf(CleanupMode.MANUAL to "手动管理",CleanupMode.AUTOMATIC to "自动管理").forEach {(mode,label)->
        val selected=l.cleanupMode==mode;val available=mode==CleanupMode.MANUAL||(read&&manage)
        Surface(onClick={if(available){store.library{it.copy(cleanupMode=mode)};onMessage(if(mode==CleanupMode.AUTOMATIC)"已开启自动管理" else "已切换为手动管理")}else{onMessage("自动管理需要完整照片访问和媒体管理授权");sheet="permissions"}},modifier=Modifier.weight(1f),shape=RoundedCornerShape(12.dp),color=if(selected)Pale else Color.White,border=BorderStroke(1.dp,if(selected)Color(0xFFCDC5F3) else Hairline)){
         Column(Modifier.padding(13.dp)){UiText(label,fontSize=12.sp,color=if(available)Violet else Muted,fontWeight=if(selected)FontWeight.SemiBold else FontWeight.Normal);UiText(if(mode==CleanupMode.MANUAL)"到期后通知，确认后处理" else "到期后自动移入回收站",fontSize=9.sp,lineHeight=14.sp,color=if(available)Muted else Muted.copy(alpha=.65f),modifier=Modifier.padding(top=5.dp))}
        }
       }
      }
      UiText(if(l.cleanupMode==CleanupMode.AUTOMATIC)"自动管理将在下一次后台检查时尝试移入回收站；系统可能延后执行。" else "手动管理不会自动移动图片，需由你确认后处理。",fontSize=10.sp,color=Muted,modifier=Modifier.padding(top=10.dp),lineHeight=17.sp)
      SectionHeading("自动纳入","仅开启后新增的图片")
      Surface(shape=RoundedCornerShape(19.dp),color=Color.White,border=BorderStroke(1.dp,Hairline)){
       Column(Modifier.padding(horizontal=15.dp)){Source.entries.forEachIndexed { i,s->
        Row(Modifier.fillMaxWidth().padding(vertical=13.dp),verticalAlignment=Alignment.CenterVertically){
         Box(Modifier.size(39.dp).background(listOf(Color(0xFFEEF2FF),Color(0xFFF3EFFF),Color(0xFFEAF6F7))[i],RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){Mark(listOf("camera","screenshot","save")[i],listOf(Color(0xFF8090D4),Color(0xFFA087D6),Color(0xFF6FA8AE))[i],Modifier.size(23.dp))}
         Column(Modifier.weight(1f).padding(start=12.dp)){UiText(s.label,fontSize=13.sp,fontWeight=FontWeight.Medium);UiText(s.description,fontSize=10.sp,color=Muted,modifier=Modifier.padding(top=5.dp),maxLines=2)}
         Switch(checked=l.rules[i].enabled,onCheckedChange={enabled->
          if(enabled && (!read||!l.initialized)){onMessage("请先授予完整照片访问并刷新，建立历史图片基线");sheet="permissions"}
          else{store.library {it.copy(rules=it.rules.mapIndexed{j,r->if(i==j)Rule(enabled,System.currentTimeMillis()) else r})};onMessage(if(enabled)"已开启，仅纳入之后的新图片" else "已停止纳入，已有图片期限不变")}
         },modifier=Modifier.semantics{contentDescription=tr("自动纳入${s.label}")},colors=SwitchDefaults.colors(checkedTrackColor=Violet,checkedThumbColor=Color.White,uncheckedTrackColor=Color(0xFFE5E7EF),uncheckedBorderColor=Color.Transparent))
        };if(i<2)HorizontalDivider(color=Hairline)
       }}
      }
      Row(Modifier.padding(top=18.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){UiText("♧",color=Violet,fontSize=15.sp);UiText("从回收站恢复的图片将保留，不再自动纳入。\n按目录识别来源，未知目录及收藏图片不自动纳入。",fontSize=10.sp,lineHeight=18.sp,color=Muted)}
      run{UiText(if(l.lastScan==0L)"尚未检查手机相册" else "最近检查：${dateText(l.lastScan)}",fontSize=10.sp,color=Muted,modifier=Modifier.padding(top=14.dp));if(l.error.isNotEmpty())UiText(l.error,color=Color(0xFFB68166),fontSize=11.sp,modifier=Modifier.padding(top=8.dp));TextButton(onClick=onRefresh,enabled=!busy){UiText(if(busy)"正在检查…" else "立即检查相册",fontSize=12.sp)}}
     } else {
      val shown=l.photos.filter {p-> when(filter){"soon"->p.state==PhotoState.TEMPORARY&&p.expires-now<=6*HOUR;"kept"->p.state==PhotoState.KEPT;"trash"->p.state==PhotoState.TRASH;"unavailable"->p.state==PhotoState.UNAVAILABLE;else->p.state==PhotoState.TEMPORARY} }.sortedBy {it.expires}
      LazyVerticalGrid(columns=GridCells.Fixed(2),modifier=body,contentPadding=PaddingValues(start=24.dp,end=24.dp,bottom=24.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
       item(span={GridItemSpan(2)}){Column{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){UiText("临时相册",fontSize=27.sp,fontWeight=FontWeight.Bold);UiText("此刻需要的，暂时留在这里。",fontSize=12.sp,color=Muted,modifier=Modifier.padding(top=8.dp))};TextButton(onClick={onRefresh()},enabled=!busy){UiText(if(busy)"检查中…" else "刷新",fontSize=11.sp)}}
        Row(Modifier.padding(top=20.dp).horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)){
         (listOf("temporary" to "临时","soon" to "即将到期","kept" to "已保留","trash" to "回收站")+if(l.photos.any{it.state==PhotoState.UNAVAILABLE})listOf("unavailable" to "不可访问") else emptyList()).forEach{(id,title)->
          Surface(onClick={filter=id},shape=RoundedCornerShape(10.dp),color=if(filter==id)Pale else Color(0xFFEBEDF4)){UiText(title,Modifier.padding(horizontal=13.dp,vertical=10.dp),fontSize=11.sp,color=if(filter==id)Violet else Muted)}
         }
        }
        Row(Modifier.fillMaxWidth().padding(top=20.dp,bottom=1.dp),horizontalArrangement=Arrangement.SpaceBetween){UiText("${shown.size} 张图片",fontSize=10.sp,color=Muted);UiText(if(filter=="trash")"本 APP 跟踪的回收站图片" else "按到期时间排序",fontSize=10.sp,color=Muted)}
       }}
       items(shown,key={it.id}){p->Surface(onClick={selected=p.id},shape=RoundedCornerShape(16.dp),color=Color.White,border=BorderStroke(1.dp,Hairline)){
        Column{Box{PhotoArt(p,Modifier.fillMaxWidth().aspectRatio(1.06f));UiText(p.source.label.removeSuffix("图片"),Modifier.padding(9.dp).background(Color.White.copy(alpha=.9f),RoundedCornerShape(6.dp)).padding(horizontal=7.dp,vertical=4.dp),fontSize=9.sp,color=Muted)}
         Column(Modifier.padding(12.dp)){Text(p.title,fontSize=12.sp,maxLines=1,overflow=TextOverflow.Ellipsis);UiText(remaining(p,now),fontSize=10.sp,maxLines=2,lineHeight=16.sp,color=if(p.state==PhotoState.TEMPORARY&&p.expires-now<=6*HOUR)Color(0xFFBE8C6B) else Muted,modifier=Modifier.padding(top=7.dp))}
        }
       }}
       if(shown.isEmpty())item(span={GridItemSpan(2)}){Column(Modifier.fillMaxWidth().padding(vertical=52.dp),horizontalAlignment=Alignment.CenterHorizontally){Mark("clock",Color(0xFFB4ACD9),Modifier.size(45.dp));UiText("这里空空的，也挺好。",fontSize=14.sp,color=Muted,modifier=Modifier.padding(top=17.dp));UiText("开启规则后，新的图片会出现在这里。",fontSize=11.sp,color=Muted,modifier=Modifier.padding(top=9.dp))}}
       item(span={GridItemSpan(2)}){Column(horizontalAlignment=Alignment.CenterHorizontally){UiText("短暂停留，也是美好的记录。",fontSize=10.sp,color=Muted,modifier=Modifier.padding(vertical=16.dp));Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){OutlinedButton(onClick={processDue()},modifier=Modifier.weight(1f),enabled=!busy){UiText("处理到期图片",fontSize=11.sp)}};UiText("后台到期会通知你；系统限制可能导致执行延后。",fontSize=10.sp,color=Muted,modifier=Modifier.padding(top=10.dp))}}
      }
     }
    }
   }
  }
  if(selectedPhoto!=null){val p=selectedPhoto
   ModalBottomSheet(onDismissRequest={selected=null},sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),containerColor=Paper){Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(bottom=28.dp)){
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){UiText("图片详情",fontSize=19.sp,fontWeight=FontWeight.SemiBold);TextButton(onClick={selected=null}){UiText("关闭")}}
    PhotoArt(p,Modifier.fillMaxWidth().height(255.dp).clip(RoundedCornerShape(18.dp)),true)
    DetailRow("名称",p.title);DetailRow("来源",tr(p.source.label));DetailRow("目录",p.folder);DetailRow("加入时间",dateText(p.added))
    Surface(shape=RoundedCornerShape(14.dp),color=Pale,modifier=Modifier.fillMaxWidth().padding(vertical=12.dp)){UiText(remaining(p,now),Modifier.padding(17.dp),color=Violet,fontSize=14.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)}
    when(p.state){
     PhotoState.TEMPORARY->{UiText("预计 ${dateText(p.expires)} 到期。后台到期提醒，打开 APP 后处理。",fontSize=11.sp,color=Muted,lineHeight=18.sp)
      WideButton("♡ 保留这张图片"){store.changePhoto(p.id){Rules.keep(it)};selected=null;onMessage("已保留，不会再自动清理")}
      WideButton("立即移入回收站",danger=true,enabled=!busy){selected=null;onTrash(listOf(p),false)}
     }
     PhotoState.TRASH->{WideButton("恢复并保留",enabled=!busy){selected=null;onTrash(listOf(p),true)};UiText("恢复后不会重新纳入临时相册。",fontSize=11.sp,color=Muted,modifier=Modifier.padding(top=9.dp))}
     PhotoState.KEPT->{WideButton("主动重新设为临时图片",secondary=true){store.changePhoto(p.id){Rules.reenter(it,l.duration,now)};selected=null;onMessage("已按当前保留时间重新计时")}}
     PhotoState.UNAVAILABLE->{UiText("图片可能被移走、删除，或访问权限发生变化。不会对它执行清理。",fontSize=12.sp,color=Muted);WideButton("刷新相册",secondary=true){selected=null;onRefresh()}}
    }
   }}
  }
  if(sheet.isNotEmpty()){
   when(sheet){
    "language"->AlertDialog(onDismissRequest={sheet=""},title={UiText("language")},text={Column{listOf("zh" to "中文","en" to "English").forEach{(code,label)->Row(Modifier.fillMaxWidth().clickable{store.update{it.copy(language=code)};sheet=""}.padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){RadioButton(selected=state.language==code,onClick={store.update{it.copy(language=code)};sheet=""});UiText(label,Modifier.padding(start=8.dp))}}}},confirmButton={TextButton(onClick={sheet=""}){UiText("关闭")}})
    "permissions"->ModalBottomSheet(onDismissRequest={sheet=""},containerColor=Paper){Column(Modifier.padding(horizontal=24.dp).padding(bottom=30.dp).verticalScroll(rememberScrollState())){
     UiText("授权管理",fontSize=21.sp,fontWeight=FontWeight.SemiBold)
     UiText("本 APP 不联网，不会上传照片；图片仅在本机处理和保留，以保护你的隐私。完整访问用于发现新增图片，媒体管理权限用于减少系统确认。",fontSize=12.sp,color=Muted,lineHeight=20.sp,modifier=Modifier.padding(vertical=14.dp))
     PermissionRow("照片访问",if(read)"完整授权" else if(partial)"仅部分照片，自动纳入暂停" else "未授权",onRead)
     PermissionRow("免确认清理",if(manage)"已授权" else "未授权",onManage)
     run{PermissionRow("到期通知",if(notified)"已开启" else "未开启",onNotify);UiText("本版本后台仅发送到期提醒，不会绕过系统后台限制。未授权媒体管理时，清理需要系统确认。",fontSize=11.sp,color=Muted,lineHeight=19.sp,modifier=Modifier.padding(top=16.dp));TextButton(onClick={context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:${context.packageName}")))}){UiText("打开系统应用设置",fontSize=12.sp)}}
     WideButton("完成"){sheet=""}
    }}
    "due"->AlertDialog(onDismissRequest={sheet=""},title={UiText("处理到期图片")},text={UiText("${Rules.due(l,now).size} 张图片已到期。将请求移入系统回收站；每次最多处理 100 张。")},confirmButton={TextButton(onClick={sheet="";onTrash(Rules.due(l,System.currentTimeMillis()),false)},enabled=!busy){UiText("移入回收站")}},dismissButton={TextButton(onClick={sheet=""}){UiText("稍后处理")}})
   }
  }
 }
}
}
@Composable private fun SectionHeading(title:String,sub:String){Row(Modifier.fillMaxWidth().padding(top=25.dp,bottom=13.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){UiText(title,fontSize=14.sp,fontWeight=FontWeight.SemiBold);UiText(sub,fontSize=10.sp,color=Muted)}}
@Composable private fun DetailRow(label:String,value:String){Row(Modifier.fillMaxWidth().padding(top=12.dp),horizontalArrangement=Arrangement.spacedBy(20.dp)){UiText(label,fontSize=11.sp,color=Muted,modifier=Modifier.width(58.dp));Text(value,fontSize=11.sp,color=Ink,modifier=Modifier.weight(1f),maxLines=3,overflow=TextOverflow.Ellipsis)}}
@Composable private fun PermissionRow(title:String,value:String,onClick:()->Unit){Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){UiText(title,fontSize=14.sp);UiText(value,fontSize=11.sp,color=Muted,modifier=Modifier.padding(top=5.dp))};TextButton(onClick=onClick){UiText("设置",fontSize=12.sp)}};HorizontalDivider(color=Hairline)}
@Composable private fun WideButton(text:String,secondary:Boolean=false,danger:Boolean=false,enabled:Boolean=true,onClick:()->Unit){Button(onClick=onClick,enabled=enabled,shape=RoundedCornerShape(13.dp),colors=ButtonDefaults.buttonColors(containerColor=if(danger)Color(0xFFFDEDE8) else if(secondary)Pale else Violet,contentColor=if(danger)Color(0xFFB87E68) else if(secondary)Violet else Color.White),modifier=Modifier.fillMaxWidth().padding(top=9.dp).heightIn(min=48.dp)){UiText(text,fontSize=13.sp)}}

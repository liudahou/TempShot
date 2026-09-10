package com.tempshot
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
@Composable fun Mark(kind:String,color:Color,modifier:Modifier=Modifier){Canvas(modifier){scale(size.width/24f,size.height/24f,pivot=Offset.Zero){
 val stroke=Stroke(1.6f,cap=StrokeCap.Round)
 fun line(x:Float,y:Float,a:Float,b:Float)=drawLine(color,Offset(x,y),Offset(a,b),1.6f,StrokeCap.Round)
 when(kind){
 "clock"->{drawCircle(color,9.3f,Offset(12f,12f),style=stroke);line(12f,6f,12f,12f);line(12f,12f,16f,14f)}
 "shield"->{val p=Path();p.moveTo(12f,2f);p.lineTo(20f,6f);p.lineTo(19f,14f);p.quadraticTo(17f,19f,12f,22f);p.quadraticTo(7f,19f,5f,14f);p.lineTo(4f,6f);p.close();drawPath(p,color,style=stroke);line(8f,12f,11f,15f);line(11f,15f,16f,9f)}
 "camera"->{drawRoundRect(color,Offset(2f,6f),Size(20f,14f),androidx.compose.ui.geometry.CornerRadius(3f),style=stroke);drawCircle(color,4f,Offset(12f,13f),style=stroke);line(7f,6f,8f,3f);line(8f,3f,16f,3f);line(16f,3f,17f,6f)}
 "screenshot"->{line(7f,2f,2f,2f);line(2f,2f,2f,7f);line(17f,2f,22f,2f);line(22f,2f,22f,7f);line(2f,17f,2f,22f);line(2f,22f,7f,22f);line(17f,22f,22f,22f);line(22f,22f,22f,17f);drawRect(color,Offset(6f,6f),Size(12f,12f),style=stroke)}
 "save"->{line(12f,2f,12f,15f);line(7f,10f,12f,15f);line(12f,15f,17f,10f);line(3f,16f,3f,21f);line(3f,21f,21f,21f);line(21f,21f,21f,16f)}
 else->{drawRoundRect(color,Offset(3f,3f),Size(18f,18f),androidx.compose.ui.geometry.CornerRadius(3f),style=stroke);drawCircle(color,1.5f,Offset(8f,8f));line(4f,18f,10f,12f);line(10f,12f,14f,16f);line(14f,16f,17f,13f);line(17f,13f,21f,17f)}
 }
}}}

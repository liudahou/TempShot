package com.tempshot
// Test data only; not packaged into the app.
fun sampleLibrary(now:Long=System.currentTimeMillis())=Library(initialized=true,photos=listOf(
 Photo("sample-0","山间的一刻",Source.CAMERA,now-19*HOUR,now+5*HOUR,uri="content://media/external/images/media/10",folder="DCIM/Camera/")
))

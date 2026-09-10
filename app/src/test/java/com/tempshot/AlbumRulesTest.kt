package com.tempshot
import org.junit.Assert.*
import org.junit.Test
class AlbumRulesTest {
 @Test fun expirySummaryUsesActualEarliestDeadline(){val p=sampleLibrary(now).photos.first().copy(expires=now+3*MINUTE);assertEquals("最早 3 分钟后到期",expirySummary(listOf(p),now))}
 @Test fun expirySummaryReportsExpiredInsteadOfSixHourWindow(){val p=sampleLibrary(now).photos.first().copy(expires=now-1);assertEquals("1 张图片已到期",expirySummary(listOf(p),now))}
 @Test fun cleanupModeDefaultsToManual(){assertEquals(CleanupMode.MANUAL,Library().cleanupMode)}
 @Test fun fifteenMinuteRetentionUsesMinutes(){assertEquals(15*MINUTE,durationMillis(FIFTEEN_MINUTES));assertEquals("15 分钟",durationLabel(FIFTEEN_MINUTES))}
 @Test fun legacyThreeMinuteRetentionMigratesToFifteenMinutes(){assertEquals(15*MINUTE,durationMillis(LEGACY_THREE_MINUTES));assertEquals("15 分钟",durationLabel(LEGACY_THREE_MINUTES))}
 @Test fun reenterWithFifteenMinuteRetentionUsesMinutes(){val p=sampleLibrary(now).photos.first();assertEquals(now+15*MINUTE,Rules.reenter(p,FIFTEEN_MINUTES,now).expires)}
 private val now=1_800_000_000_000L
 private fun row(key:String="new",added:Long=now,trash:Boolean=false,folder:String="DCIM/Camera/",favorite:Boolean=false)=MediaRow(key,"content://media/external/images/media/1",key,folder,added,trash,favorite)
 private fun enabled()=Library(rules=List(3){Rule(true,now-HOUR)},initialized=true)
 @Test fun firstScanOnlyCreatesBaseline(){val l=Reconcile.apply(Library(rules=List(3){Rule(true,now-HOUR)}),listOf(row()),now);assertTrue(l.photos.isEmpty());assertTrue(l.initialized);assertTrue(l.seen.containsKey("new"))}
 @Test fun newPhotosUseSharedDuration(){val l=Reconcile.apply(enabled().copy(duration=72),listOf(row()),now);assertEquals(now+72*HOUR,l.photos.single().expires)}
 @Test fun changingDurationDoesNotRewriteExistingDeadline(){val l=Reconcile.apply(enabled(),listOf(row()),now);val edited=Reconcile.apply(l.copy(duration=12),listOf(row()),now+HOUR);assertEquals(l.photos.single().expires,edited.photos.single().expires)}
 @Test fun disabledSourceCannotEnroll(){val l=Reconcile.apply(enabled().copy(rules=List(3){Rule()}),listOf(row()),now);assertTrue(l.photos.isEmpty())}
 @Test fun oldPhotosCannotEnrollAfterToggle(){val l=Reconcile.apply(enabled(),listOf(row(added=now-2*HOUR)),now);assertTrue(l.photos.isEmpty())}
 @Test fun knownTrashedPhotoRestoresToKept(){val l=Reconcile.apply(enabled(),listOf(row()),now);val trashed=Reconcile.apply(l,listOf(row(trash=true)),now);val restored=Reconcile.apply(trashed,listOf(row()),now+48*HOUR);assertEquals(PhotoState.KEPT,restored.photos.single().state);assertTrue(restored.photos.single().restored);assertTrue(Rules.due(restored,now+99*HOUR).isEmpty())}
 @Test fun untrackedRestoredPhotoIsNotEnrolled(){val l=enabled().copy(seen=mapOf("new" to true));assertTrue(Reconcile.apply(l,listOf(row()),now).photos.isEmpty())}
 @Test fun missingThenReappearingPhotoIsConservativelyKept(){val l=Reconcile.apply(enabled(),listOf(row()),now);val missing=Reconcile.apply(l,emptyList(),now);assertEquals(PhotoState.UNAVAILABLE,missing.photos.single().state);assertEquals(PhotoState.KEPT,Reconcile.apply(missing,listOf(row()),now).photos.single().state)}
 @Test fun favoritesAndUnknownFoldersNeverAutoEnroll(){assertTrue(Reconcile.apply(enabled(),listOf(row(favorite=true)),now).photos.isEmpty());assertTrue(Reconcile.apply(enabled(),listOf(row(folder="Bluetooth/")),now).photos.isEmpty())}
 @Test fun screenshotClassificationPrecedesSavedImages(){assertEquals(Source.SCREENSHOT,Rules.classify("Pictures/Screenshots/"));assertEquals(Source.CAMERA,Rules.classify("DCIM/Camera/"));assertEquals(Source.SAVED,Rules.classify("Pictures/WeChat/"));assertNull(Rules.classify("AppPrivate/"))}
 @Test fun keptPicturesStayExcludedUntilExplicitReentry(){val p=sampleLibrary(now).photos.first();val kept=Rules.keep(p,true);assertTrue(Rules.due(Library(photos=listOf(kept)),now+48*HOUR).isEmpty());val reentered=Rules.reenter(kept,12,now);assertEquals(PhotoState.TEMPORARY,reentered.state);assertEquals(now+12*HOUR,reentered.expires);assertFalse(reentered.restored)}
 @Test fun postponeExpiredUsesCurrentTime(){val p=sampleLibrary(now).photos.first().copy(expires=now-HOUR);assertEquals(now+24*HOUR,Rules.postpone(p,now).expires)}
}

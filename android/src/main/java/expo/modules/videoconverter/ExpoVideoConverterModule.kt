package expo.modules.videoconverter

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.Promise
import expo.modules.kotlin.activityresult.AppContextActivityResultLauncher
import expo.modules.kotlin.exception.CodedException
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Future;

import android.net.Uri

import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;
import com.otaliastudios.transcoder.source.ClipDataSource;
import com.otaliastudios.transcoder.source.DataSource;
import com.otaliastudios.transcoder.source.UriDataSource;
import com.otaliastudios.transcoder.strategy.DefaultAudioStrategy;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy;
import com.otaliastudios.transcoder.strategy.TrackStrategy;

class ExpoVideoConverterModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("ExpoVideoConverter")

    AsyncFunction("convert") { options: Options, promise: Promise ->
      convert(options, promise)
    }
  }

  private fun convert(options: Options, promise: Promise) {
    val E_CREATE_FILE_ERROR: String = "E_CREATE_FILE_ERROR";

    try {
      val context = requireNotNull(appContext.reactContext)
      val mTranscodeOutputFile: File = File(options.outputPath)
      val fileIsCreated: Boolean = mTranscodeOutputFile.createNewFile()
      
      if(!fileIsCreated){
        throw IOException(E_CREATE_FILE_ERROR)
      }

      val mTranscodeOutputFilePath: String = mTranscodeOutputFile.getAbsolutePath()
      val source: DataSource = UriDataSource(context, Uri.fromFile(File(options.sourcePath)))
      val finalSource: DataSource =
        if (options.trimEnd != 0.toDouble()) {
          ClipDataSource(source, (options.trimStart  * 1000 * 1000).toLong(), (options.trimEnd * 1000 * 1000).toLong())
        } else {
          source
        }

      val mTranscodeVideoStrategy: TrackStrategy = DefaultVideoStrategy
        .exact(options.width, options.height)
        .frameRate(options.frameRate.toInt())
        .bitRate(options.videoBitrate)
        .keyFrameInterval(1.toFloat())
        .build()

      val mTranscodeAudioStrategy: TrackStrategy = DefaultAudioStrategy.builder()
        .sampleRate(44100)
        .bitRate(options.audioBitrate)
        .channels(DefaultAudioStrategy.CHANNELS_AS_INPUT)
        .build()

      val listeners = object : TranscoderListener {
        override fun onTranscodeProgress(progress: Double) {}

        override fun onTranscodeCompleted(successCode: Int) {
          val map = HashMap<String, String>()
          map.put("uri", mTranscodeOutputFilePath)
          promise.resolve(map)
        }

        override fun onTranscodeCanceled() {
          val map = HashMap<String, Boolean>()
          map.put("isCancelled", true)
          promise.resolve(map)
        }

        override fun onTranscodeFailed(e: Throwable) {
          promise.resolve(null)
        }
      }

      val transcoder: Future<Void> = Transcoder.into(mTranscodeOutputFilePath)
        .addDataSource(finalSource)
        .setListener(listeners)
        .setVideoTrackStrategy(mTranscodeVideoStrategy)
        .setAudioTrackStrategy(mTranscodeAudioStrategy)
        .transcode()
    }
    catch(e: Exception) {
      promise.resolve(null)
    }
  }
}

class Options : Record {
  @Field
  val id: Int = 0

  @Field
  val sourcePath: String = ""

  @Field
  val outputPath: String = ""

  @Field
  val width: Int = 0

  @Field
  val height: Int = 0

  @Field
  val frameRate: Int = 0

  @Field
  val videoBitrate: Long = 0

  @Field
  val audioBitrate: Long = 0

  @Field
  val audioChannels: Int = 0

  @Field
  val trimStart: Double = 0.0

  @Field
  val trimEnd: Double = 0.0

}

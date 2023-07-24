import ExpoModulesCore
import Foundation
import AVFoundation
import NextLevelSessionExporter

public class ExpoVideoConverterModule: Module {
  private var transcodersRunning: NSMutableArray = NSMutableArray()
  private let E_CREATE_FILE_ERROR: String = "E_CREATE_FILE_ERROR"
  private let E_CONVERSION_ERROR: String = "E_CONVERSION_ERROR"
  private let E_CONVERSION_CANCELLED: String = "E_CONVERSION_CANCELLED"

  public func definition() -> ModuleDefinition {
    Name("ExpoVideoConverter")

    AsyncFunction("convert") { (config: Config, promise: Promise) in
      do {
        let mediaAssetSourceUrl = try URL(fileURLWithPath: config.sourcePath)
        let mediaAssetOutputUrl = try URL(fileURLWithPath: config.outputPath)
        let mediaSourceAsset: AVAsset = AVAsset(url: mediaAssetSourceUrl)
        
        let clipStart = CMTimeMakeWithSeconds(config.trimStart, preferredTimescale: 600)
        let clipEnd = CMTimeMakeWithSeconds(config.trimEnd, preferredTimescale: 600)
        
        let exporter = NextLevelSessionExporter(withAsset: mediaSourceAsset)
        exporter.outputURL = mediaAssetOutputUrl
      
        let compressionDict: [String: Any] = [
          AVVideoAverageBitRateKey: config.videoBitrate,
          AVVideoProfileLevelKey: AVVideoProfileLevelH264HighAutoLevel as String,
          AVVideoAverageNonDroppableFrameRateKey: config.frameRate,
        ]

        exporter.videoOutputConfiguration = [
          AVVideoCodecKey: AVVideoCodecType.h264,
          AVVideoWidthKey: config.width,
          AVVideoHeightKey: config.height,
          AVVideoScalingModeKey: AVVideoScalingModeResizeAspectFill,
          AVVideoCompressionPropertiesKey: compressionDict
        ]
      
        exporter.audioOutputConfiguration = [
          AVFormatIDKey: kAudioFormatMPEG4AAC,
          AVEncoderBitRateKey: config.audioBitrate,
          AVSampleRateKey: NSNumber(value: Float(44100)),
          AVNumberOfChannelsKey: config.audioChannels
        ]
        
        if(clipEnd != CMTime.zero){
          exporter.timeRange = CMTimeRangeMake(start: clipStart, duration: clipEnd)
        }

        exporter.outputFileType = AVFileType.mp4

        exporter.export(progressHandler: { (progress) in
          print(progress)
        }, completionHandler: { result in
          switch result {
            case .success(let status):
              switch status {
              case .completed:
                print("NextLevelSessionExporter, export completed, \(exporter.outputURL?.description ?? "")")
                let map: NSDictionary = ["uri": config.outputPath]
                promise.resolve(map)
                break
              default:
                let map: NSDictionary = ["isCancelled": true]
                promise.resolve(map)
                print("NextLevelSessionExporter, did not complete")
                break
          }
          break
          case .failure(let error):
            print("NextLevelSessionExporter, failed to export \(error)")
            let map: NSDictionary = ["error": true]
            promise.resolve(map)
            break
          }
        })
      } catch {
        let map: NSDictionary = ["error": true]
        promise.resolve(map)
      }
    }
  }
}

struct Config : Record {
  @Field
  var id: Int = 0

  @Field
  var sourcePath: String = ""

  @Field
  var outputPath: String = ""

  @Field
  var width: Double = 0

  @Field
  var height: Double = 0

  @Field
  var frameRate: Double = 0

  @Field
  var videoBitrate: Double = 0.0

  @Field
  var audioBitrate: Double = 0.0

  @Field
  var audioChannels: Int = 0

  @Field
  var trimStart: Double = 0.0

  @Field
  var trimEnd: Double = 0.0
}

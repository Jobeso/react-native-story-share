//
//  RNStoryShare.swift
//  RNStoryShare
//
//  Created by Johannes Sorg on 21.11.18.
//  Copyright © 2018 Facebook. All rights reserved.
//

import Foundation
import UIKit

@objc(RNStoryShare)
class RNStoryShare: NSObject{
    let domain: String = "RNStoryShare"
    let FILE_PATH: String = "file_path"
    let BASE64: String = "base64"

    let UNKNOWN_ERROR: String = "An unknown error occured in RNStoryShare"

    let instagramScheme = URL(string: "instagram-stories://share")
    let snapchatScheme = URL(string: "snapchat://")
    
    @objc
    func constantsToExport() -> [String: Any]! {
        return [
            "FILE_PATH": FILE_PATH,
            "BASE64": BASE64
        ]
    }

    @objc
    func share(_ config: NSDictionary,
               resolver resolve: RCTPromiseResolveBlock,
               rejecter reject: RCTPromiseRejectBlock) -> Void {
        
        do{
            if (config["backgroundAsset"] == nil){
                throw NSError(domain: domain, code: 400, userInfo: nil)
            }

            let backgroundAsset = RCTConvert.nsurl(config["backgroundAsset"])
            let stickerAsset = RCTConvert.nsurl(config["stickerAsset"])
            let type: String = RCTConvert.nsString(config["type"] ?? FILE_PATH)
            let attributionLink: String = RCTConvert.nsString(config["attributionLink"]) ?? ""
            
            if(type == BASE64){
                do {
                    let decodedData = try Data(contentsOf: backgroundAsset!,
                                               options: NSData.ReadingOptions(rawValue: 0))
                    
                    if(stickerAsset != nil){
                        let decodedStickerData = try Data(contentsOf: stickerAsset!,
                                                          options: NSData.ReadingOptions(rawValue: 0))

                        shareToInstagram(UIImage(data: decodedData)!.pngData()! as NSData,
                                         stickerData: UIImage(data: decodedStickerData)!.pngData()! as NSData,
                                         attributionLink: attributionLink,
                                         resolve: resolve,
                                         reject: reject)
                    }else{
                        shareToInstagram(UIImage(data: decodedData)!.pngData()! as NSData,
                                         stickerData: nil,
                                         attributionLink: attributionLink,
                                         resolve: resolve,
                                         reject: reject)
                    }
                }catch {
                    reject(domain, "Type is base64 but assets can't be converted to data", error)
                }
            }else{
                // TODO add support for non base64 images
                // shareToInstagram(UIImage(data: decodedData)!.pngData()! as NSData,
                //                  stickerData: UIImage(data: decodedStickerData)!.pngData()! as NSData,
                //                  attributionLink: attributionLink,
                //                  resolve: resolve,
                //                  reject: reject)
            }
        }catch{
            reject(domain, "Parameter Missing: 'backgroundAsset'", error)
        }
    }
    
    @objc
    func isInstagramAvailable(_ resolve: RCTPromiseResolveBlock,
                              rejecter reject: RCTPromiseRejectBlock) -> Void {
        resolve(UIApplication.shared.canOpenURL(instagramScheme!))
    }
    
    @objc
    func isSnapchatAvailable(_ resolve: RCTPromiseResolveBlock,
                              rejecter reject: RCTPromiseRejectBlock) -> Void {
        resolve(UIApplication.shared.canOpenURL(snapchatScheme!))
    }
    
    func shareToInstagram(_ backgroundData: NSData,
                          stickerData: NSData? = nil,
                          attributionLink: String,
                          resolve: RCTPromiseResolveBlock,
                          reject: RCTPromiseRejectBlock){
        do{
            if(UIApplication.shared.canOpenURL(instagramScheme!)){
                
                if(stickerData != nil){
                    let pasteboardItems = [[
                        "com.instagram.sharedSticker.backgroundImage": backgroundData,
                        "com.instagram.sharedSticker.stickerImage": stickerData!,
                        "com.instagram.sharedSticker.contentURL": attributionLink]]

                    UIPasteboard.general.items = pasteboardItems
                    UIApplication.shared.openURL(instagramScheme!)

                    resolve("ok")
                }else{
                    let pasteboardItems = [[
                        "com.instagram.sharedSticker.backgroundImage": backgroundData,
                        "com.instagram.sharedSticker.contentURL": attributionLink]]
                    
                    UIPasteboard.general.items = pasteboardItems
                    UIApplication.shared.openURL(instagramScheme!)

                    resolve("ok")
                }

            }else {
                throw NSError(domain: domain, code: 400, userInfo: nil)
            }
        }catch {
            reject(domain, "Instagram not available", error)
        }
    }

}

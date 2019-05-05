//
//  RNStoryShare.swift
//  RNStoryShare
//
//  Created by Johannes Sorg on 21.11.18.
//  Copyright © 2018 Facebook. All rights reserved.
//

import Foundation
import UIKit
import SCSDKCreativeKit

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
    func isInstagramAvailable(_ resolve: RCTPromiseResolveBlock,
                              rejecter reject: RCTPromiseRejectBlock) -> Void {
        resolve(UIApplication.shared.canOpenURL(instagramScheme!))
    }
    
    @objc
    func isSnapchatAvailable(_ resolve: RCTPromiseResolveBlock,
                             rejecter reject: RCTPromiseRejectBlock) -> Void {
        resolve(UIApplication.shared.canOpenURL(snapchatScheme!))
    }
    
    func _shareToInstagram(_ backgroundData: NSData? = nil,
                           stickerData: NSData? = nil,
                           attributionLink: String,
                           resolve: RCTPromiseResolveBlock,
                           reject: RCTPromiseRejectBlock){
        do{
            if(UIApplication.shared.canOpenURL(instagramScheme!)){
                
                if(backgroundData != nil && stickerData != nil){
                    let pasteboardItems = [[
                        "com.instagram.sharedSticker.backgroundImage": backgroundData!,
                        "com.instagram.sharedSticker.stickerImage": stickerData!,
                        "com.instagram.sharedSticker.contentURL": attributionLink]]
                    
                    UIPasteboard.general.items = pasteboardItems
                    UIApplication.shared.openURL(instagramScheme!)
                    
                    resolve("ok")
                }else if(stickerData != nil){
                    let pasteboardItems = [[
                        "com.instagram.sharedSticker.stickerImage": stickerData!,
                        "com.instagram.sharedSticker.contentURL": attributionLink]]
                    
                    UIPasteboard.general.items = pasteboardItems
                    UIApplication.shared.openURL(instagramScheme!)
                    
                    resolve("ok")
                }else{
                    let pasteboardItems = [[
                        "com.instagram.sharedSticker.backgroundImage": backgroundData!,
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


    @objc
    func shareToInstagram(_ config: NSDictionary,
               resolver resolve: RCTPromiseResolveBlock,
               rejecter reject: RCTPromiseRejectBlock) -> Void {
        
        do{
            if (config["backgroundAsset"] == nil && config["stickerAsset"] == nil){
                throw NSError(domain: domain, code: 400, userInfo: nil)
            }

            let backgroundAsset = RCTConvert.nsurl(config["backgroundAsset"])
            let stickerAsset = RCTConvert.nsurl(config["stickerAsset"])
            let type: String = RCTConvert.nsString(config["type"] ?? FILE_PATH)
            let attributionLink: String = RCTConvert.nsString(config["attributionLink"]) ?? ""
            
            if(type == BASE64){
                do {
                    if(backgroundAsset != nil && stickerAsset != nil){
                        let decodedData = try Data(contentsOf: backgroundAsset!,
                                                   options: NSData.ReadingOptions(rawValue: 0))

                        let decodedStickerData = try Data(contentsOf: stickerAsset!,
                                                          options: NSData.ReadingOptions(rawValue: 0))
                        
                        _shareToInstagram(UIImage(data: decodedData)!.pngData()! as NSData,
                                          stickerData: UIImage(data: decodedStickerData)!.pngData()! as NSData,
                                          attributionLink: attributionLink,
                                          resolve: resolve,
                                          reject: reject)
                    }else if(stickerAsset != nil){
                        let decodedStickerData = try Data(contentsOf: stickerAsset!,
                                                          options: NSData.ReadingOptions(rawValue: 0))

                        _shareToInstagram(nil,
                                         stickerData: UIImage(data: decodedStickerData)!.pngData()! as NSData,
                                         attributionLink: attributionLink,
                                         resolve: resolve,
                                         reject: reject)
                    }else{
                        let decodedData = try Data(contentsOf: backgroundAsset!,
                                                   options: NSData.ReadingOptions(rawValue: 0))

                        _shareToInstagram(UIImage(data: decodedData)!.pngData()! as NSData,
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
    

    
    func _shareToSnapchat(_ snap: SCSDKSnapContent,
                              stickerAsset: URL? = nil,
                              attributionLink: String,
                              resolve: RCTPromiseResolveBlock,
                              reject: RCTPromiseRejectBlock)
    {
        do {
            if(attributionLink != ""){
                snap.attachmentUrl = attributionLink
            }
            
            if(stickerAsset != nil){
                let data = try Data(contentsOf: stickerAsset!,
                                    options: NSData.ReadingOptions(rawValue: 0))
                
                let stickerImage = UIImage(data: data)
                let sticker = SCSDKSnapSticker(stickerImage: stickerImage!)
                snap.sticker = sticker
            }

            let snapAPI = SCSDKSnapAPI(content: snap)
            snapAPI.startSnapping {(error: Error?) in
                print("Sharing on SnapChat.")
            }
        } catch {
            reject(domain, "Type is base64 but assets can't be converted to data", error)
        }
    }
    
    @objc
    func shareToSnapchat(_ config: NSDictionary,
                          resolver resolve: RCTPromiseResolveBlock,
                          rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            if (config["backgroundAsset"] == nil && config["stickerAsset"] == nil){
                throw NSError(domain: domain, code: 400, userInfo: nil)
            }

            let backgroundAsset = RCTConvert.nsurl(config["backgroundAsset"])
            let stickerAsset = RCTConvert.nsurl(config["stickerAsset"])
            let attributionLink: String = RCTConvert.nsString(config["attributionLink"]) ?? ""
            
            if(backgroundAsset != nil) {
                let data = try Data(contentsOf: backgroundAsset!,
                                    options: NSData.ReadingOptions(rawValue: 0))
                
                let snapImage = UIImage(data: data)
                let photo = SCSDKSnapPhoto(image: snapImage!)
                let snap = SCSDKPhotoSnapContent(snapPhoto: photo)

                _shareToSnapchat(snap,stickerAsset: stickerAsset, attributionLink: attributionLink, resolve: resolve, reject: reject)
            }else{
                let snap = SCSDKNoSnapContent()
                _shareToSnapchat(snap,stickerAsset: stickerAsset, attributionLink: attributionLink, resolve: resolve, reject: reject)
            }
        } catch {
            reject(domain, "Parameter Missing: 'backgroundAsset'", error)
        }
    }
}

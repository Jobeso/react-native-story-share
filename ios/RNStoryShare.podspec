
Pod::Spec.new do |s|
  s.name         = "RNStoryShare"
  s.version      = "1.0.0"
  s.summary      = "RNStoryShare"
  s.description  = "Share your images to instagram stories."
  s.homepage     = "https://github.com/Jobeso/react-native-story-share"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/author/RNStoryShare.git", :tag => "master" }
  s.source_files  = "RNStoryShare/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  s.dependency "SnapSDK/SCSDKCreativeKit"

end

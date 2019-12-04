require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "RNStoryShare"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  RNStoryShare
                   DESC
  s.homepage     = "https://github.com/author/RNStoryShare"
  s.license      = package["license"]
  s.author       = { "author" => package["author"]["email"], "name" => package["author"]["name"] }
  s.platform     = :ios, "10.0"
  s.source       = { :git => "https://github.com/author/RNStoryShare.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m}"
  s.requires_arc = true

  s.dependency "React"
  s.dependency "SnapSDK/SCSDKCreativeKit"
end

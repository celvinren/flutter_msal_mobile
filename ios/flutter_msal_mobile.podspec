#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint flutter_msal_mobile.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'flutter_msal_mobile'
  s.version          = '1.0.0'
  s.summary          = 'Flutter plugin project for MSAL ios.'
  s.description      = <<-DESC
Flutter plugin project for MSAL ios.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  # https://learn.microsoft.com/en-us/entra/msal/objc/
  # Supported platforms: iOS 14.0+
  s.platform = :ios, '14.0'
  s.dependency 'MSAL', '~> 1.4.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'
end

#import <Cordova/CDV.h>
#import "BrightcovePlayerSDK.h"
#import "BCPlayerPluginController.h"

@interface BCPlayerPlugin : CDVPlugin <BrightcovePluginViewControllerDelegate>

@property BCPlayerPluginController *bCPlayerPluginController;

- (void)init:(CDVInvokedUrlCommand*)command;
- (void)load:(CDVInvokedUrlCommand*)command;
- (void)play:(CDVInvokedUrlCommand*)command;
- (void)pause:(CDVInvokedUrlCommand*)command;
- (void)seek:(CDVInvokedUrlCommand*)command;
- (void)reposition:(CDVInvokedUrlCommand*)command;
- (void)show:(CDVInvokedUrlCommand*)command;
- (void)hide:(CDVInvokedUrlCommand*)command;
- (void)rate:(CDVInvokedUrlCommand*)command;
@end


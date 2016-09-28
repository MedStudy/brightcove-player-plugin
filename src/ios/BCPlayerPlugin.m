#import "BCPlayerPlugin.h"

extern id bcPluginGlobal;

@implementation BCPlayerPlugin

#pragma mark - Cordova Initialization

-(CDVPlugin*) initWithWebView:(UIWebView*)theWebView
{
    self = (BCPlayerPlugin*)[super initWithWebView:theWebView];
    bcPluginGlobal = self;
    _backgroundMode = false;
    return self;
}

#pragma mark - Cordova Events

- (void) handlePlayEvent
{
    NSLog(@"Play video");

    [self.commandDelegate evalJs:[NSString stringWithFormat:@"cordova.fireWindowEvent('brightcovePlayer.play', {})"]];
}

- (void) handleLoadedEvent:(NSString*)duration
{
    NSLog(@"loaded video");
    [self.commandDelegate evalJs:[NSString stringWithFormat:@"cordova.fireWindowEvent('brightcovePlayer.loaded', {'duration': %@})", duration]];
}

- (void) handleProgressEvent:(NSString*)currentTime
{

    [self.commandDelegate evalJs:[NSString stringWithFormat:@"cordova.fireWindowEvent('brightcovePlayer.progress', {'currentTime': %@})", currentTime]];
}

- (void) handleRateEvent:(NSString*)rate
{

    [self.commandDelegate evalJs:[NSString stringWithFormat:@"cordova.fireWindowEvent('brightcovePlayer.rate', {'rate': %@})", rate]];
}

- (void) handlePauseEvent
{
    if(_backgroundMode){
        if(self.bCPlayerPluginController != nil){
            [self.bCPlayerPluginController play];
        }
        _backgroundMode = false;
    }

    [self.commandDelegate evalJs:[NSString stringWithFormat:@"cordova.fireWindowEvent('brightcovePlayer.pause', {})"]];
}

- (void) handleBackgroundMode
{
    _backgroundMode = true;
}

- (void) handleExitBackgroundMode
{
    _backgroundMode = false;
}

- (void) handleEndedEvent
{
    NSLog(@"ended video");

    [self.commandDelegate evalJs:[NSString stringWithFormat:@"cordova.fireWindowEvent('brightcovePlayer.end', {})"]];
}

- (void) handleErrorEvent
{
    NSLog(@"error video");

    [self.commandDelegate evalJs:[NSString stringWithFormat:@"cordova.fireWindowEvent('brightcovePlayer.error', {})"]];
}

- (void) handleLoadErrorEvent
{
    NSLog(@"load error video");

    [self.commandDelegate evalJs:[NSString stringWithFormat:@"cordova.fireWindowEvent('brightcovePlayer.error', {})"]];
}

#pragma mark - Cordova Methods

- (void)init:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;

    NSString *token = [command argumentAtIndex:0 withDefault:@"" andClass:[NSString class]];
    if (token != nil && [token length]) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Inited"];
    } else{
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Empty Brightcove token!"];
    }

    self.bCPlayerPluginController = [[BCPlayerPluginController alloc] initWithToken:self.webView.superview Token:token];
    self.bCPlayerPluginController.delegate = self;

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)reposition:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;

    NSString *xString = [command argumentAtIndex:0 withDefault:@"" andClass:[NSString class]];
    double x = 0;
    if (xString != nil && [xString length]) {
        x= [xString doubleValue];
    }

    NSString *yString = [command argumentAtIndex:1 withDefault:@"" andClass:[NSString class]];
    double y = 0;
    if (yString != nil && [yString length]) {
        y =[yString doubleValue];
    }

    NSString *widthString = [command argumentAtIndex:2 withDefault:@"" andClass:[NSString class]];
    double width = 0;
    if (widthString != nil && [widthString length]) {
        width =[widthString doubleValue];
    }

    NSString *heightString = [command argumentAtIndex:3 withDefault:@"" andClass:[NSString class]];
    double height = 0;
    if (heightString != nil && [heightString length]) {
        height =[heightString doubleValue];
    }

    CGRect viewRect = CGRectMake(x, y, width, height);

    [self.bCPlayerPluginController reposition:viewRect];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:nil];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)load:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;

    NSString *refId = [command argumentAtIndex:0 withDefault:@"" andClass:[NSString class]];
    if (refId != nil && [refId length]) {
        [self.bCPlayerPluginController load:refId];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"ok"];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)seek:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;

    NSString *positionString = [command argumentAtIndex:0 withDefault:@"" andClass:[NSString class]];
    double position = 0;
    if (positionString != nil && [positionString length]) {
        position =[positionString doubleValue];
    }

    if (positionString != nil && [positionString length]) {
        [self.bCPlayerPluginController seek:position];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"ok"];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)rate:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
    CDVPluginResult* pluginResult = nil;

    NSString *rateString = [command argumentAtIndex:0 withDefault:@"" andClass:[NSString class]];
    float rate = 0;
    if (rateString != nil && [rateString length]) {
        rate =[rateString floatValue];
    }

    if (rateString != nil && [rateString length]) {
        [self.bCPlayerPluginController rate:rate];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"ok"];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)play:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
    CDVPluginResult* pluginResult = nil;

    [self.bCPlayerPluginController play];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"ok"];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)pause:(CDVInvokedUrlCommand*)command
{
    [self.commandDelegate runInBackground:^{
        CDVPluginResult* pluginResult = nil;

    [self.bCPlayerPluginController pause];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"ok"];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)hide:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;

    [self.bCPlayerPluginController hide];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"ok"];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)show:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;

    [self.bCPlayerPluginController show];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"ok"];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

#pragma mark - Helper Methods

@end

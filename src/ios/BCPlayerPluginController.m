#import "BCPlayerPluginController.h"

@implementation BCPlayerPluginController

#pragma mark - Initialization

#pragma mark - Events

#pragma mark - Methods

-(id)initWithToken:(UIView*)pluginView Token:(NSString*)token
{
    CGRect viewRect = CGRectMake(0, 0, 300, 300);

    self = [super init];
    self.token = token;
    self.pluginView = pluginView;

    _parentView = [[UIView alloc] initWithFrame:viewRect];
    _parentView.backgroundColor = [UIColor clearColor];
    [pluginView addSubview:_parentView];
    _parentView.hidden = YES;

    _avpvc = [[AVPlayerViewController alloc] init];
    self.avpvc.view.frame = viewRect;
    self.avpvc.view.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
    [self.avpvc didMoveToParentViewController:self];
    _avpvc.view.backgroundColor = [UIColor clearColor];
    self.avpvc.view.hidden = YES;
    [_pluginView addSubview:_avpvc.view];

    self.playbackController = [[BCOVPlayerSDKManager sharedManager] createPlaybackController];
    self.playbackController.delegate = self;

    self.playbackController.autoAdvance = NO;
    self.playbackController.autoPlay = YES;
    //self.playbackController.allowsBackgroundAudioPlayback = YES;
    [self.playbackController setAllowsExternalPlayback:YES];

    viewRect.origin.x = 0;
    viewRect.origin.y = 0;
    self.playbackController.view.frame = viewRect;
    [_parentView addSubview: self.playbackController.view];

    _playRate = 1;
    return self;
}

-(void)reposition:(CGRect)viewRect
{
    _parentView.frame = viewRect;
    self.avpvc.view.frame = viewRect;
    [_pluginView setNeedsDisplay];
    self.avpvc.view.hidden = NO;
    _parentView.hidden = NO;
}

-(void)show
{
    self.avpvc.view.hidden = NO;
    _parentView.hidden = NO;
}

-(void)hide
{
    self.avpvc.view.hidden = YES;
    _parentView.hidden = YES;
}

-(NSString*)getDuration
{
    return self.duration;
}

-(void)rate:(float)rate
{
    self.avpvc.player.rate = rate;
}

-(NSString*)getCurrentTime
{
    return self.currentTime;
}

-(void)load:(NSString*)urlString
{
    self.duration = @"0";
    self.isLoaded = false;
    
    NSURL *url = [NSURL URLWithString:urlString];
    BCOVSource *vSource = [[BCOVSource alloc] initWithURL:url];
    BCOVVideo *video = [[BCOVVideo alloc] initWithSource:vSource cuePoints:nil properties:nil];

    if (video)
    {
        NSMutableArray *videoArray = [NSMutableArray arrayWithCapacity:1];
        [videoArray addObject:video];
        [self.playbackController setVideos:videoArray];
    }
    else
    {
        NSLog(@"BrightcovePluginViewController Debug - Error creating video object");
        [_delegate handleLoadErrorEvent];
    }
}

-(void)seek:(double)position
{
    [self.avpvc.player.currentItem seekToTime:CMTimeMakeWithSeconds(position, 60000)];
}

-(void)pause
{
    [self.avpvc.player pause];
}

-(void)play
{
    [self.avpvc.player play];
}

#pragma mark BCOVPlaybackControllerDelegate Methods

- (void)playbackController:(id<BCOVPlaybackController>)controller didAdvanceToPlaybackSession:(id<BCOVPlaybackSession>)session
{
    NSLog(@"ViewController Debug - Advanced to new session.");
    self.avpvc.player = session.player;
}

- (void)playbackController:(id<BCOVPlaybackController>)controller playbackSession:(id<BCOVPlaybackSession>)session didReceiveLifecycleEvent:(BCOVPlaybackSessionLifecycleEvent *)lifecycleEvent
{
    NSString *type = lifecycleEvent.eventType;

    if ([type isEqualToString:kBCOVPlaybackSessionLifecycleEventPlay]){
        NSLog(@"BrightcovePluginViewController Debug - kBCOVPlaybackSessionLifecycleEventPlay");
        [_delegate handlePlayEvent];
    }
    else if ([type isEqualToString:kBCOVPlaybackSessionLifecycleEventPause]){
        NSLog(@"BrightcovePluginViewController Debug - kBCOVPlaybackSessionLifecycleEventPause");
        [_delegate handlePauseEvent];
    }
    else if ([type isEqualToString:kBCOVPlaybackSessionLifecycleEventEnd]){
        NSLog(@"BrightcovePluginViewController Debug - kBCOVPlaybackSessionLifecycleEventEnd");
        [_delegate handleEndedEvent];
    }
    else if ([type isEqualToString:kBCOVPlaybackSessionLifecycleEventError] || [type isEqualToString:kBCOVPlaybackSessionLifecycleEventFail]){
        NSLog(@"BrightcovePluginViewController Debug - kBCOVPlaybackSessionLifecycleEventError");
        [_delegate handleErrorEvent];
    }
    else if ([type isEqualToString:kBCOVPlaybackSessionErrorDomain]){
        NSLog(@"BrightcovePluginViewController Debug - kBCOVPlaybackSessionErrorDomain");
        [_delegate handleLoadErrorEvent];
    }
}

- (void)playbackController:(id<BCOVPlaybackController>)controller playbackSession:(id<BCOVPlaybackSession>)session didProgressTo:(NSTimeInterval)progress
{
    self.currentTime = [NSString stringWithFormat:@"%f", (float)progress];
    if(_playRate != self.avpvc.player.rate){
        _playRate = self.avpvc.player.rate;
        [_delegate handleRateEvent:[NSString stringWithFormat:@"%f", (float)_playRate]];
    }
    [_delegate handleProgressEvent:self.currentTime];
}

- (void)playbackController:(id<BCOVPlaybackController>)controller playbackSession:(id<BCOVPlaybackSession>)session didChangeDuration:(NSTimeInterval)duration
{
    if(!self.isLoaded){
        self.isLoaded = true;
        self.duration = [NSString stringWithFormat:@"%f", (float)duration];
        NSLog(@"Duration: %@", self.duration);

        [_delegate handleLoadedEvent:self.duration];
    }
}

#pragma mark - Helper Methods

@end

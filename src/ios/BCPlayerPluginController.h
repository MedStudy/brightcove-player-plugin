#import <UIKit/UIKit.h>
#import <AVKit/AVKit.h>
#import "BrightcovePlayerSDK.h"

@protocol BrightcovePluginViewControllerDelegate <NSObject>

@optional
- (void)playerShown;
- (void)seekingVideo;
- (void)seekedVideo;
- (void)bufferingVideo;
- (void)playerHidden:(NSString *)position;
- (void)handlePlayEvent;
- (void)handlePauseEvent;
- (void)handleProgressEvent:(NSString *)position;
- (void)handleErrorEvent;
- (void)handleEndedEvent;
- (void)handleLoadedEvent:(NSString *)duration;
- (void)handleLoadErrorEvent;
@end

@interface BCPlayerPluginController : UIViewController <BCOVPlaybackSessionConsumer, BCOVPlaybackControllerDelegate>

@property NSString *token;
@property id<BCOVPlaybackController> playbackController;
@property (nonatomic, strong) AVPlayerViewController *avpvc;
@property (nonatomic, strong) id delegate;
@property UIView *parentView;
@property UIView *pluginView;
@property NSString *duration;
@property NSString *currentTime;
@property BOOL isLoaded;

-(BCPlayerPluginController*)initWithToken:(UIView*)pluginView Token:(NSString*)token;
-(void)load:(NSString*)refId;
-(void)reposition:(CGRect)viewRect;
-(void)play;
-(void)pause;
-(void)seek:(double)position;
-(void)rate:(float)rate;
-(void)hide;
-(void)show;
@end

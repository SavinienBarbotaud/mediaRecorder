
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNMediarecorderSpec.h"

@interface Mediarecorder : NSObject <NativeMediarecorderSpec>
#else
#import <React/RCTBridgeModule.h>

@interface Mediarecorder : NSObject <RCTBridgeModule>
#endif

@end

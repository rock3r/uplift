#ifndef __RS_HSL_RSH__
#define __RS_HSL_RSH__

/*
 * This function was adapted from those at
 * http://dystopiancode.blogspot.co.uk/2012/06/hsv-rgb-conversion-algorithms-in-c.html
 * and from StylingAndroid's repo: https://github.com/StylingAndroid/ColourWheel
 */
static float4 hsv2Argb(float3 hsv, float alpha) {
    float c = hsv[2] * hsv[1];
    float x = c * (1.0f - fabs(fmod(hsv[0] / 60.0f, 2) - 1.0f));
    float m = hsv[2] - hsv[1];

   	float4 argb;
   	if (hsv[0] >= 0.0f && hsv[0] < 60.0f) {
   	    argb.r = c + m;
   	    argb.g = x + m;
   	    argb.b = m;
   	} else if (hsv[0] >= 60.0f && hsv[0] < 120.0f) {
   	    argb.r = x + m;
   	    argb.g = c + m;
   	    argb.b = m;
   	} else if (hsv[0] >= 120.0f && hsv[0] < 180.0f) {
   	    argb.r = m;
   	    argb.g = c + m;
   	    argb.b = x + m;
   	} else if (hsv[0] >= 180.0f && hsv[0] < 240.0f) {
   	    argb.r = m;
   	    argb.g = x + m;
   	    argb.b = c + m;
   	} else if (hsv[0] >= 240.0f && hsv[0] < 300.0f) {
   	    argb.r = x + m;
   	    argb.g = m;
   	    argb.b = c + m;
   	} else if (hsv[0] >= 300.0f && hsv[0] < 360.0f) {
   	    argb.r = c + m;
   	    argb.g = m;
   	    argb.b = x + m;
   	} else {
   	    argb.r = m;
   	    argb.g = m;
   	    argb.b = m;
   	}
    argb.a = alpha;
    return argb;
}

#endif // #ifndef __RS_HSL_RSH__

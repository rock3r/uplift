#pragma version(1)
#pragma rs java_package_name(me.seebrock3r.elevationtester.widget.colorwheel)

#include "hsv.rsh"

/*
 * This file was adapted from StylingAndroid's repo: https://github.com/StylingAndroid/ColourWheel
 */

const static uchar4 transparent = {0, 0, 0, 0};

float centerX;
float centerY;
float radius;
float brightness = 1.0f;

void colorWheel(rs_script script, rs_allocation allocation, float brightness_value) {
    centerX = rsAllocationGetDimX(allocation) / 2.0f;
    centerY = rsAllocationGetDimY(allocation) / 2.0f;
    radius = min(centerX, centerY);
    brightness = brightness_value;
    rsForEach(script, allocation, allocation);
}

uchar4 RS_KERNEL root(uchar4 in, int32_t x, int32_t y) {
    uchar4 out;
    float xOffset = x - centerX;
    float yOffset = y - centerY;
    float centerOffset = hypot(xOffset, yOffset);

    if (centerOffset <= radius) {
        float centerAngle = fmod(degrees(atan2(yOffset, xOffset)) + 360.0f, 360.0f);
        float3 colorHsv;
        colorHsv.x = centerAngle;
        colorHsv.y = centerOffset / radius;
        colorHsv.z = brightness;
        out = rsPackColorTo8888(skiaHsvToArgb(colorHsv));
    } else {
        out = transparent;
    }
    return out;
}

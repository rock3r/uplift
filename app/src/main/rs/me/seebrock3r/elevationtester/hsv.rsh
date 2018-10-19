#ifndef __RS_HSL_RSH__
#define __RS_HSL_RSH__

/*
 * This function was adapted from Skia's own SkColor::SkHSVToColor
 * See https://github.com/google/skia/blob/master/src/core/SkColor.cpp
 */
float tolerance = 1.0f / (1 << 12);

static bool nearlyZero(float value) {
    return fabs(value) < tolerance;
}

static float4 skiaHsvToArgb(float3 hsv) {
    float4 argb;
    argb.a = 1.0f;
    
    float s = clamp(hsv.y, 0.0f, 1.0f);
    float v = clamp(hsv.z, 0.0f, 1.0f);

    if (nearlyZero(s)) {
        // The color has no saturation and thus is a shade of gray
        argb.rgb = v;
        return argb;
    }

    float h = clamp(hsv.x, 0.0f, 360.0f);
    float hx = h / 60.0f;

    uchar sector = floor(hx);
    float sectorOffset = fract(hx);

    float p = (1.0f - s) * v;
    float q = (1.0f - (s * sectorOffset)) * v;
    float t = (1.0f - (s * (1.0f - sectorOffset))) * v;

    switch (sector) {
        case 0:  argb.r = v; argb.g = t; argb.b = p; break;
        case 1:  argb.r = q; argb.g = v; argb.b = p; break;
        case 2:  argb.r = p; argb.g = v; argb.b = t; break;
        case 3:  argb.r = p; argb.g = q; argb.b = v; break;
        case 4:  argb.r = t; argb.g = p; argb.b = v; break;
        default: argb.r = v; argb.g = p; argb.b = q; break;
    }
    return argb;
}

#endif // #ifndef __RS_HSL_RSH__

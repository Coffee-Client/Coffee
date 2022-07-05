#version 330

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform float Progress;
uniform vec2 Direction;
uniform float Range;

out vec4 fragColor;

uint hash( uint x ) {
    x += ( x << 10u );
    x ^= ( x >>  6u );
    x += ( x <<  3u );
    x ^= ( x >> 11u );
    x += ( x << 15u );
    return x;
}



// Compound versions of the hashing algorithm I whipped together.
uint hash( uvec2 v ) { return hash( v.x ^ hash(v.y)                         ); }
uint hash( uvec3 v ) { return hash( v.x ^ hash(v.y) ^ hash(v.z)             ); }
uint hash( uvec4 v ) { return hash( v.x ^ hash(v.y) ^ hash(v.z) ^ hash(v.w) ); }



// Construct a float with half-open range [0:1] using low 23 bits.
// All zeroes yields 0.0, all ones yields the next smallest representable value below 1.0.
float floatConstruct( uint m ) {
    const uint ieeeMantissa = 0x007FFFFFu; // binary32 mantissa bitmask
    const uint ieeeOne      = 0x3F800000u; // 1.0 in IEEE binary32

    m &= ieeeMantissa;                     // Keep only mantissa bits (fractional part)
    m |= ieeeOne;                          // Add fractional part to 1.0

    float  f = uintBitsToFloat( m );       // Range [1:2]
    return f - 1.0;                        // Range [0:1]
}



// Pseudo-random value in half-open range [0:1].
float random( float x ) { return floatConstruct(hash(floatBitsToUint(x))); }
float random( vec2  v ) { return floatConstruct(hash(floatBitsToUint(v))); }
float random( vec3  v ) { return floatConstruct(hash(floatBitsToUint(v))); }
float random( vec4  v ) { return floatConstruct(hash(floatBitsToUint(v))); }
void main() {
    float range2 = Range/2.;
    int added = 1;
    vec2 mappedCoord = texCoord/oneTexel;
    float randOffset = (random(mappedCoord)-.5)*Progress*(Range*4);
    vec2 randOffsetV = vec2(randOffset)*Direction;
    vec4 color = texture(DiffuseSampler, (mappedCoord+randOffsetV)*oneTexel);
    vec4 colorCpy = color;
    for(float r = -range2;r<range2;r+=1.) {
        vec2 offset = vec2(r,r)*Direction*oneTexel;
        vec2 newPixel = texCoord+offset;
        vec4 colorThere = texture(DiffuseSampler, newPixel);
        added += 1;
        color += colorThere;
    }
    vec4 avg = mix(colorCpy, color/added, Progress);
    vec4 iceColor = vec4(0.87,0.97,0.98,1);
    float weightA = 0.9;
    float delta = 1-weightA;
    weightA = weightA+delta*(1-Progress);
    float weightB = 1-weightA;
    fragColor = avg*weightA+iceColor*weightB;
}
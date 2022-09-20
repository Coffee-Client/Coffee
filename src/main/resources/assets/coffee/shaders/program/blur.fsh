#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform float radius;
uniform float progress;

out vec4 fragColor;

void main() {
    vec4 blurred = vec4(0.0);
    float progRadius = floor(radius * progress);
    for (float r = -progRadius; r <= progRadius; r += 1.0) {
        vec4 smple = texture(DiffuseSampler, texCoord + oneTexel * r * BlurDir);
        // Accumulate smoothed blur
        blurred = blurred + smple;
    }
    fragColor = blurred / (progRadius * 2.0 + 1.0);
}
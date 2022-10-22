#version 330

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

void main() {
    vec4 current = texture(DiffuseSampler, texCoord);

    if (current.a != 0) {
        fragColor = vec4(0.219, 0.290, 0.309, 0.3);
        return;
    }

    bool seenSelect = false;
    bool seenNonSelect = false;
    for(int x = -1; x <= 1; x++) {
        for(int y = -1; y <= 1; y++) {
            vec2 offset = vec2(x, y);
            vec2 coord = texCoord + offset * oneTexel;
            vec4 t = texture(DiffuseSampler, coord);
            if (t.a == 1) seenSelect = true;
            else if (t.a == 0) seenNonSelect = true;
        }
    }
    if (seenSelect && seenNonSelect) fragColor = vec4(1);
    else discard;
}
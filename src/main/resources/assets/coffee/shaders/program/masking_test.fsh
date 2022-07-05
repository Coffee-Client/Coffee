#version 330

uniform sampler2D DiffuseSampler;
uniform sampler2D Mask;
uniform sampler2D Masking;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

void main() {
    vec4 maskedColor = texture(Mask, texCoord);
//    fragColor = maskedColor;
    if (maskedColor == vec4(1)) { // positive, copy all the stuff from the masking buffer over
        fragColor = texture(Masking, texCoord);
    } else { // negative, copy main buffer
        fragColor = texture(DiffuseSampler, texCoord);
    }
}
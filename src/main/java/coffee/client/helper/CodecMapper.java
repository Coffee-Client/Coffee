package coffee.client.helper;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Util;

public class CodecMapper {
    public static <T> JsonSerializer<T> createSerializer(Codec<T> v) {
        return (t, type, jsonSerializationContext) -> {
            DataResult<JsonElement> jsonElementDataResult = v.encodeStart(JsonOps.INSTANCE, t);
            return Util.getResult(jsonElementDataResult, RuntimeException::new);
        };
    }
    public static <T>JsonDeserializer<T> createDeserializer(Codec<T> v) {
        return (jsonElement, type, jsonDeserializationContext) -> {
            return Util.getResult(v.parse(JsonOps.INSTANCE, jsonElement), RuntimeException::new);
        };
    }
}

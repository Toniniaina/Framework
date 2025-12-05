package framework.utilitaire;

import java.util.HashMap;
import java.util.Map;

public class ConverterRegistry {

    private final Map<ConversionKey, Converter<?, ?>> converters = new HashMap<>();

    public void registerConverter(Class<?> sourceType, Class<?> targetType, Converter<?, ?> converter) {
        if (sourceType == null || targetType == null || converter == null) {
            throw new IllegalArgumentException("sourceType, targetType and converter must not be null");
        }
        converters.put(new ConversionKey(sourceType, targetType), converter);
    }

    @SuppressWarnings("unchecked")
    public <S, T> Converter<S, T> getConverter(Class<S> sourceType, Class<T> targetType) {
        Converter<?, ?> converter = converters.get(new ConversionKey(sourceType, targetType));
        return (Converter<S, T>) converter;
    }

    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        return converters.containsKey(new ConversionKey(sourceType, targetType));
    }
}

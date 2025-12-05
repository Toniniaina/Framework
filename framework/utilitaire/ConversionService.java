package framework.utilitaire;

public class ConversionService {

    private static final ConversionService INSTANCE = new ConversionService();

    public static ConversionService getInstance() {
        return INSTANCE;
    }

    public static void registerConverter(Class<?> sourceType, Class<?> targetType, Converter<?, ?> converter) {
        INSTANCE.registry.registerConverter(sourceType, targetType, converter);
    }

    private final ConverterRegistry registry = new ConverterRegistry();

    public <T> T convert(Object source, Class<T> targetType) {
        if (source == null) return null;
        Class<?> sourceType = source.getClass();

        if (targetType.isAssignableFrom(sourceType)) {
            @SuppressWarnings("unchecked")
            T casted = (T) source;
            return casted;
        }

        @SuppressWarnings("unchecked")
        Converter<Object, T> converter = (Converter<Object, T>) registry.getConverter(sourceType, targetType);
        if (converter != null) {
            return converter.convert(source);
        }

        // Essayer une factory statique sur le type cible (ex: fromId(String))
        T viaFactory = convertViaStaticFactory(source, targetType);
        if (viaFactory != null) {
            return viaFactory;
        }

        return convertPrimitive(source, targetType);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertViaStaticFactory(Object source, Class<T> targetType) {
        if (source == null) return null;
        String raw = source.toString();
        try {
            // Convention simple: méthode statique fromId(String)
            java.lang.reflect.Method m = targetType.getDeclaredMethod("fromId", String.class);
            if (!m.isAccessible()) m.setAccessible(true);
            Object res = m.invoke(null, raw);
            return (T) res;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Throwable t) {
            throw new IllegalArgumentException("Error invoking static factory on " + targetType.getName() + ": " + t.getMessage(), t);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convertPrimitive(Object source, Class<T> targetType) {
        if (source == null) return null;
        String raw = source.toString();

        if (targetType == String.class) return (T) raw;
        if (targetType == Integer.class || targetType == int.class) return (T) Integer.valueOf(raw);
        if (targetType == Long.class || targetType == long.class) return (T) Long.valueOf(raw);
        if (targetType == Double.class || targetType == double.class) return (T) Double.valueOf(raw);
        if (targetType == Boolean.class || targetType == boolean.class) return (T) Boolean.valueOf(raw);

        throw new IllegalArgumentException("No converter found for " + source.getClass().getName() + " -> " + targetType.getName());
    }
}

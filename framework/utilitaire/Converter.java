package framework.utilitaire;

public interface Converter<S, T> {
    T convert(S source);
}

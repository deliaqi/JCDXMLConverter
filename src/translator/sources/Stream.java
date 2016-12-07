package translator.sources;

/**
 * Represents a stream source that a parser can read from.
 * The resource may be an online file or streaming service.
 * This will be implemented depending on the platform.
 * For example, the java.io.InputStream interface can be used instead of Stream
 * when implementing in the Java platform.
 */
public interface Stream extends Source {
    
}

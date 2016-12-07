package translator.sources;

/**
 * Represents a source from which a parser can take its input.
 * Subinterfaces (File, Stream) may be more specific as to where the information
 * comes from (a file on disk, a stream from a network, etc).
 * This is meant to be implemented in the chosen platform.
 */
public interface Source {
    
}

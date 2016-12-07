package translator.sources.impl;

import java.io.InputStream;
import translator.sources.Stream;

public class StreamImpl implements Stream {
    
    private InputStream inputStream;
    
    public StreamImpl(InputStream in) {
        this.inputStream = in;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

}

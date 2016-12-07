
package translator.sources.impl;

import java.io.File;
import java.net.URI;

public class FileImpl extends File implements translator.sources.File {
    
    public FileImpl(String parent, String child) {
        super(parent, child);
    }
    
    public FileImpl(String pathname) {
        super(pathname);
    }
    
    public FileImpl(File parent, String child) {
        super(parent, child);
    }
    
    public FileImpl(URI uri) {
        super(uri);
    }
    
}

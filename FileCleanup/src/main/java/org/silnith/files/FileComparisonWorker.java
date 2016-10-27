package org.silnith.files;

import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class FileComparisonWorker extends SwingWorker<Boolean, Integer> {
    
    private final Path first;
    
    private final Path second;
    
    private final JProgressBar progressBar;
    
    public FileComparisonWorker(final Path first, final Path second, final JProgressBar progressBar) {
        super();
        this.first = first;
        this.second = second;
        this.progressBar = progressBar;
    }
    
    @Override
    protected Boolean doInBackground() throws IOException {
        try (final InputStream firstInputStream = new BufferedInputStream(Files.newInputStream(first, StandardOpenOption.READ), 65536);
                final InputStream secondInputStream = new BufferedInputStream(Files.newInputStream(second, StandardOpenOption.READ), 65536);) {
            int firstByte;
            int secondByte;
            int count = 0;
            do {
                firstByte = firstInputStream.read();
                secondByte = secondInputStream.read();
                count++ ;
                publish(count);
                if (firstByte != secondByte) {
                    return false;
                }
            } while (firstByte != -1);
            
            return true;
        }
    }
    
    @Override
    protected void process(final List<Integer> chunks) {
        assert !chunks.isEmpty();
        assert EventQueue.isDispatchThread();
        
        final int lastIndex = chunks.size() - 1;
        final Integer lastValue = chunks.get(lastIndex);
        progressBar.setValue(lastValue);
    }
    
    @Override
    protected void done() {
        assert EventQueue.isDispatchThread();
    }
    
}

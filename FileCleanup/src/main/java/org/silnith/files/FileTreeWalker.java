package org.silnith.files;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class FileTreeWalker implements Callable<Map<Long, Set<Path>>> {
    
    private final File file;
    
    private final ConcurrentMap<Long, Set<Path>> accumulator;
    
    public FileTreeWalker(final File file) {
        this(file, new ConcurrentHashMap<Long, Set<Path>>());
    }
    
    public FileTreeWalker(final File file, final ConcurrentMap<Long, Set<Path>> accumulator) {
        super();
        this.file = file;
        this.accumulator = accumulator;
    }
    
    @Override
    public Map<Long, Set<Path>> call() throws Exception {
        assert !EventQueue.isDispatchThread();
        
        final Path path = file.toPath();
        System.out.println(path);
        
        try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);) {
            for (final Path path2 : directoryStream) {
                try {
                    if (Files.isRegularFile(path2, LinkOption.NOFOLLOW_LINKS)) {
                        final long size = Files.size(path2);
                        
                        final Set<Path> filesOfSize;
                        {
                            final Set<Path> newList = new HashSet<>();
                            final Set<Path> previousList = accumulator.putIfAbsent(size, newList);
                            if (previousList == null) {
                                filesOfSize = newList;
                            } else {
                                filesOfSize = previousList;
                            }
                        }
                        
                        assert filesOfSize != null;
                        
                        filesOfSize.add(path2);
                    } else if (Files.isDirectory(path2, LinkOption.NOFOLLOW_LINKS)) {
                        final FileTreeWalker fileTreeWalker = new FileTreeWalker(path2.toFile(), accumulator);
                        fileTreeWalker.call();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return accumulator;
    }
    
}

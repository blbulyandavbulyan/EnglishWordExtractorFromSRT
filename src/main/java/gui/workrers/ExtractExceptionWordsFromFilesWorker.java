package gui.workrers;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class ExtractExceptionWordsFromFilesWorker extends SwingWorker<Set<String>, String> {
    private final File[] exceptionWordsFiles;
    private final Predicate<Character> isValidChar;

    public ExtractExceptionWordsFromFilesWorker(File[] exceptionWordsFiles, Predicate<Character> isValidChar) {
        this.exceptionWordsFiles = exceptionWordsFiles;
        this.isValidChar = isValidChar;
    }

    @Override
    protected Set<String> doInBackground() throws Exception {
        long maxReadBytes = Arrays.stream(exceptionWordsFiles).mapToLong(File::length).sum();
        long currentReadBytes = 1;
        StringBuilder wordBuilder = new StringBuilder();
        Set<String> words = new HashSet<>();
        for (File exceptionWordsFile : exceptionWordsFiles) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(exceptionWordsFile));
            long fileLength = exceptionWordsFile.length();
            for (int c = bufferedInputStream.read(); c != -1; c = bufferedInputStream.read(), currentReadBytes++){
                if(isValidChar.test((char)c)){
                    wordBuilder.append((char) c);
                }
                else if(wordBuilder.length() > 0){
                    String word = wordBuilder.toString().toLowerCase();
                    words.add(word);
                    wordBuilder.setLength(0);
                }
                setProgress((int)Math.round((double) (currentReadBytes*100)/maxReadBytes));
            }
            bufferedInputStream.close();
        }
        return words;
    }
}

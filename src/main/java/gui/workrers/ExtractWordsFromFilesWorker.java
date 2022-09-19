package gui.workrers;

import wordprocessing.wordinfo.MinimalWordInfo;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class ExtractWordsFromFilesWorker extends SwingWorker<MinimalWordInfo[], Object> {
    private final File[] filesForExtractWords;
    private Set<String> exceptionWords;
    private final Predicate<Character> isValidChar;

    public ExtractWordsFromFilesWorker(File[] filesForExtractWords, Set<String> exceptionWords, Predicate<Character> isValidChar) {
        this.filesForExtractWords = filesForExtractWords;
        this.exceptionWords = exceptionWords;
        this.isValidChar = isValidChar;
    }

    @Override
    protected MinimalWordInfo[] doInBackground() throws Exception {
        long maxReadBytes = Arrays.stream(filesForExtractWords).mapToLong(File::length).sum();
        long currentReadBytes = 1;
        Map<String, Integer> words = new HashMap<>();
        StringBuilder wordBuilder = new StringBuilder();
        for (File fileForExtractWords : filesForExtractWords) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileForExtractWords));
            for (int c = bufferedInputStream.read(); c != -1; c = bufferedInputStream.read()){
                if(isValidChar.test((char)c)){
                    wordBuilder.append((char) c);
                }
                else if(wordBuilder.length() > 0){
                    String word = wordBuilder.toString().toLowerCase();
                    if(exceptionWords == null || !exceptionWords.contains(word)){
                        if(words.containsKey(word))words.replace(word, words.get(word) + 1);
                        else words.put(word, 1);
                    }
                    wordBuilder.setLength(0);
                }
                setProgress((int)Math.round((double) (currentReadBytes*100)/maxReadBytes));
            }
            bufferedInputStream.close();
        }
        return words.entrySet().stream().map(entry-> new MinimalWordInfo(entry.getKey(), entry.getValue())).toArray(MinimalWordInfo[]::new);
    }
    public void setExceptionWords(Set<String> exceptionWords) {
        this.exceptionWords = exceptionWords;
    }
}

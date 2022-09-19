package wordprocessing.wordextractor;

import gui.interfaces.ProgressBarInterface;
import wordprocessing.wordinfo.MinimalWordInfo;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;

public class WordExtractor {
    private final File fileForExtractWords;
    private final Predicate<Character> isValidChar;
    private final Set<String> exceptionWords;
    private final ProgressBarInterface progressBarInterface;
    public WordExtractor(File fileForExtractWords, Predicate<Character> isValidChar, Set<String> exceptionWords, ProgressBarInterface progressBarInterface){
        this.fileForExtractWords = fileForExtractWords;
        this.isValidChar = isValidChar;
        this.exceptionWords = exceptionWords;
        this.progressBarInterface = progressBarInterface;
    }
    public MinimalWordInfo[] getWordsAndCountRepeats() throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileForExtractWords));
        StringBuilder wordBuilder = new StringBuilder();
        Map<String, Integer> words = new HashMap<>();
        if(progressBarInterface != null) {
            progressBarInterface.setMinimum(0);
            progressBarInterface.setMaximum((int)fileForExtractWords.length());
            progressBarInterface.setValue(0);
        }
        for (int c = bufferedInputStream.read(), readByte = 1; c != -1; c = bufferedInputStream.read(), readByte++){
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
            if(progressBarInterface != null)progressBarInterface.setValue(readByte);
        }
        bufferedInputStream.close();
        return words.entrySet().stream().map(entry-> new MinimalWordInfo(entry.getKey(), entry.getValue())).toArray(MinimalWordInfo[]::new);
    }
    public Set<String> getWords() throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileForExtractWords));
        StringBuilder wordBuilder = new StringBuilder();
        Set<String> words = new HashSet<>();
        if(progressBarInterface != null) {
            progressBarInterface.setMinimum(0);
            progressBarInterface.setMaximum((int)fileForExtractWords.length());
            progressBarInterface.setValue(0);
        }
        for (int c = bufferedInputStream.read(), readByte = 1; c != -1; c = bufferedInputStream.read(), readByte++){
            if(isValidChar.test((char)c)){
                wordBuilder.append((char) c);
            }
            else if(wordBuilder.length() > 0){
                String word = wordBuilder.toString().toLowerCase();
                if(exceptionWords == null || !exceptionWords.contains(word))words.add(word);

                wordBuilder.setLength(0);
            }
            if(progressBarInterface != null)progressBarInterface.setValue(readByte);
        }
        bufferedInputStream.close();
        return words;
    }
    public static void main(String[] args) throws IOException {

    }
}

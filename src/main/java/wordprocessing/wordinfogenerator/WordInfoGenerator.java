package wordprocessing.wordinfogenerator;

import translategetter.Translate.PartOfSpeech;
import translategetter.TranslateProvider;
import wordprocessing.wordinfo.MinimalWordInfo;
import wordprocessing.wordinfo.WordInfo;

import java.util.*;

public class WordInfoGenerator {

    private HashMap<String, String> localizedPartOfSpeechesNames = new HashMap<>();
    private final TranslateProvider  translateProvider;
    public WordInfoGenerator(TranslateProvider translateProvider, String partOfSpeechRbPrefix, ResourceBundle rb){
        this.translateProvider = translateProvider;
        for (PartOfSpeech partOfSpeech : PartOfSpeech.values()) {
            localizedPartOfSpeechesNames.put(partOfSpeech.name(), rb.getString(String.format("%s.%s", partOfSpeechRbPrefix, partOfSpeech.name())));
        }

    }
    public WordInfo[] generateWordInfos(MinimalWordInfo[] minimalWordInfos){
        WordInfo[] wordInfos = new WordInfo[minimalWordInfos.length];
        for (int i = 0; i < wordInfos.length; i++) {
           // wordInfos[i] = new WordInfo(minimalWordInfos[i].getWord())
        }
        return wordInfos;
    }
}

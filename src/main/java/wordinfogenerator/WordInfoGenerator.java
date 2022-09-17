package wordinfogenerator;

import translategetter.Translate.PartOfSpeech;
import translategetter.TranslateProvider;
import wordextractor.MinimalWordInfo;

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
    public WordInfo[] generateWordInfos(Collection<MinimalWordInfo> minimalWordInfos){
        WordInfo[] wordInfos = new WordInfo[minimalWordInfos.size()];
        for (MinimalWordInfo minimalWordInfo : minimalWordInfos) {
            //translateProvider.
        }
        return wordInfos;
    }
}

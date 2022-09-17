package wordinfogenerator;

import org.json.JSONObject;
import translategetter.Translate.PartOfSpeech;
import translategetter.TranslateProvider;

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
    public List<WordInfo> generateWordInfos(Collection<MinimalWordInfo> minimalWordInfos){
        List<WordInfo> wordInfos = new LinkedList<>();
        for (MinimalWordInfo minimalWordInfo : minimalWordInfos) {

        }
        return wordInfos;
    }
}

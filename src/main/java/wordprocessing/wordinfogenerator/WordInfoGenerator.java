package wordprocessing.wordinfogenerator;

import gui.interfaces.ProgressBarInterface;
import translategetter.Translate;
import translategetter.Translate.PartOfSpeech;
import translategetter.TranslateProvider;
import wordprocessing.wordinfo.MinimalWordInfo;
import wordprocessing.wordinfo.WordInfo;

import java.util.*;
import java.util.function.Function;

public class WordInfoGenerator {

    private HashMap<String, String> localizedPartOfSpeechesNames = new HashMap<>();
    private final TranslateProvider  translateProvider;
    private final ProgressBarInterface progressBarInterface;

    public WordInfoGenerator(TranslateProvider translateProvider, String partOfSpeechRbPrefix, ResourceBundle rb, ProgressBarInterface progressBarInterface){
        this.translateProvider = translateProvider;
        this.progressBarInterface = progressBarInterface;

    }
    public WordInfo[] generateWordInfos(MinimalWordInfo[] minimalWordInfos){
        WordInfo[] wordInfos = new WordInfo[minimalWordInfos.length];
        if(progressBarInterface != null){
            progressBarInterface.setMinimum(0);
            progressBarInterface.setMaximum(wordInfos.length - 1);
        }
        for (int i = 0; i < wordInfos.length; i++) {
            Translate[] translates = translateProvider.getWordTranslates(minimalWordInfos[i].getWord(), "en", "ru");
            wordInfos[i] = new WordInfo(minimalWordInfos[i], Arrays.stream(translates).map(Translate::translate).toArray(String[]::new), Arrays.stream(translates).map(translate -> localizedPartOfSpeechesNames.get(translate.partOfSpeech().name())).toArray(String[]::new));
            if(progressBarInterface != null)progressBarInterface.setValue(i);
        }
        return wordInfos;
    }
}

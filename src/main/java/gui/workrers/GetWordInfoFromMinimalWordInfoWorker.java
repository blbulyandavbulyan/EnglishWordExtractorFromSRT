package gui.workrers;

import translategetter.Translate;
import translategetter.TranslateProvider;
import wordprocessing.wordinfo.MinimalWordInfo;
import wordprocessing.wordinfo.WordInfo;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;

public class GetWordInfoFromMinimalWordInfoWorker extends SwingWorker<WordInfo[], Object> {
    private MinimalWordInfo[] minimalWordInfos;
    private final TranslateProvider translateProvider;
    private final HashMap<Translate.PartOfSpeech, String> localizedPartOfSpeechNames;

    public GetWordInfoFromMinimalWordInfoWorker(MinimalWordInfo[] minimalWordInfos, TranslateProvider translateProvider, HashMap<Translate.PartOfSpeech, String> localizedPartOfSpeechNames) {
        this.minimalWordInfos = minimalWordInfos;
        this.translateProvider = translateProvider;
        this.localizedPartOfSpeechNames = localizedPartOfSpeechNames;
    }

    @Override
    protected WordInfo[] doInBackground() {
        WordInfo[] wordInfos = new WordInfo[minimalWordInfos.length];
        for (int i = 0; i < wordInfos.length; i++) {
            Translate[] translates = translateProvider.getWordTranslates(minimalWordInfos[i].getWord(), "en", "ru");
            wordInfos[i] = new WordInfo(minimalWordInfos[i], Arrays.stream(translates).map(Translate::translate).toArray(String[]::new), Arrays.stream(translates).map(translate -> localizedPartOfSpeechNames.get(translate.partOfSpeech())).toArray(String[]::new));
            setProgress((int)Math.round((double) ((i+1)*100)/wordInfos.length));
        }
        return wordInfos;
    }
    public void setMinimalWordInfos(MinimalWordInfo[] minimalWordInfos) {
        this.minimalWordInfos = minimalWordInfos;
    }
}

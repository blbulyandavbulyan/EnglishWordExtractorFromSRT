package translategetter;

import java.util.List;

public interface TranslateProvider {
    List<String> getWordTranslates(String word, String wordLanguage, String translateLanguage);
    String translatePhrase(String phrase, String phraseLanguage, String translateLanguage);
}

package translategetter;

import translategetter.exceptions.TranslatorException;

import java.util.Set;

public interface TranslateProvider {
    Translate[] getWordTranslates(String word, String wordLanguage, String translateLanguage) throws TranslatorException;
    String translatePhrase(String phrase, String phraseLanguage, String translateLanguage) throws TranslatorException;
    Set<String> getLanguages();
}

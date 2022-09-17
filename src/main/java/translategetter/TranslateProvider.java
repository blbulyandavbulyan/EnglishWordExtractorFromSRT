package translategetter;

import translategetter.exceptions.TranslatorException;
import wordinfogenerator.WordInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface TranslateProvider {
    WordInfo[] getWordTranslates(String word, String wordLanguage, String translateLanguage) throws TranslatorException;
    String translatePhrase(String phrase, String phraseLanguage, String translateLanguage) throws TranslatorException;
}

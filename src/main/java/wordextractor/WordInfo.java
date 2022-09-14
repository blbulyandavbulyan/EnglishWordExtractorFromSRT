package wordextractor;

public class WordInfo {
    private final long countOfRepetitions;
    private final String translate;
    private final String partOfSpeech;

    public WordInfo(long countOfRepetitions, String translate, String partOfSpeech) {
        this.countOfRepetitions = countOfRepetitions;
        this.translate = translate;
        this.partOfSpeech = partOfSpeech;
    }

    public long getCountOfRepetitions() {
        return countOfRepetitions;
    }

    public String getTranslate() {
        return translate;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }
}

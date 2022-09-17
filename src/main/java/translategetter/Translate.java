package translategetter;

public class Translate {
    public enum PartOfSpeech {
        ADJ,
        ADV,
        CONJ,
        DET,
        MODAL,
        NOUN,
        PREP,
        PRON,
        VERB,
        OTHER
    }
    private final PartOfSpeech partOfSpeech;
    private final String translate;

    public Translate(PartOfSpeech partOfSpeech, String translate) {
        this.partOfSpeech = partOfSpeech;
        this.translate = translate;
    }

    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    public String getTranslate() {
        return translate;
    }
}

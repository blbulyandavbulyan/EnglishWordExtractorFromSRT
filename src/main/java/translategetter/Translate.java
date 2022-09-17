package translategetter;

public record Translate(translategetter.Translate.PartOfSpeech partOfSpeech, String translate, double confidence) {
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

    @Override
    public String toString() {
        return translate;
    }
}

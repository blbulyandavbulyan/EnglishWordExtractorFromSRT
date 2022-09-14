package translategetter.exceptions;

public class DetectedAndGivenLanguageNotEqualException extends TranslatorException{
    private final String detectedLanguage;
    private final String givenLanguage;
    public DetectedAndGivenLanguageNotEqualException(String detectedLanguage, String givenLanguage){
        this.detectedLanguage = detectedLanguage;
        this.givenLanguage = givenLanguage;
    }

    public String getDetectedLanguage() {
        return detectedLanguage;
    }

    public String getGivenLanguage() {
        return givenLanguage;
    }
}

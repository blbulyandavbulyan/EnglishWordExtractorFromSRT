package translategetter.exceptions;

public class InvalidStatusCode extends TranslatorException{
    private final int statusCode;
    private final Integer translatorErrorCode;
    private final String errorDescription;

    public InvalidStatusCode(int statusCode, Integer translatorErrorCode, String errorDescription) {
        this.statusCode = statusCode;
        this.translatorErrorCode = translatorErrorCode;
        this.errorDescription = errorDescription;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Integer getTranslatorErrorCode() {
        return translatorErrorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}

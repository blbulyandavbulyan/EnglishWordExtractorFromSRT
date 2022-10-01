package programsettings.exceptions.exceptionwordsfileprocessing;

import java.io.File;

public class ExceptionWordsFileNotFoundException extends ExceptionWordsFileProcessingException {


    private final String errorMessage;
    public ExceptionWordsFileNotFoundException(File providedFile) {
        super(providedFile);
        errorMessage = "File %s not found".formatted(providedFile.getName());
    }
    @Override
    public String getMessage() {
        return errorMessage;
    }
}

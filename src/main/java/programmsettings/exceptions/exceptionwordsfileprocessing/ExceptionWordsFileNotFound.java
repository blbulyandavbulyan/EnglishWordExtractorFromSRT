package programmsettings.exceptions.exceptionwordsfileprocessing;

import java.io.File;

public class ExceptionWordsFileNotFound extends ExceptionWordsFileProcessingException {


    private final String errorMessage;
    public ExceptionWordsFileNotFound(File providedFile) {
        super(providedFile);
        errorMessage = "File %s not found".formatted(providedFile.getName());
    }
    @Override
    public String getMessage() {
        return errorMessage;
    }
}

package programmsettings.exceptions.exceptionwordsfileprocessing;

import java.io.File;

public class ExceptionWordsFileIsEmptyException extends ExceptionWordsFileProcessingException{
    public ExceptionWordsFileIsEmptyException(File providedFile) {
        super(providedFile);
    }
}

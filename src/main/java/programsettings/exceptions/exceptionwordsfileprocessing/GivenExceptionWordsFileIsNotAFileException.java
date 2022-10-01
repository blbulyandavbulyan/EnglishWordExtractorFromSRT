package programsettings.exceptions.exceptionwordsfileprocessing;

import java.io.File;

public class GivenExceptionWordsFileIsNotAFileException extends ExceptionWordsFileProcessingException {
    public GivenExceptionWordsFileIsNotAFileException(File providedFile) {
        super(providedFile);
    }
}

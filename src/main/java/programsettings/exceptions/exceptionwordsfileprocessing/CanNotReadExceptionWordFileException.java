package programsettings.exceptions.exceptionwordsfileprocessing;

import java.io.File;

public class CanNotReadExceptionWordFileException extends ExceptionWordsFileProcessingException{

    public CanNotReadExceptionWordFileException(File providedFile) {
        super(providedFile);
    }
}

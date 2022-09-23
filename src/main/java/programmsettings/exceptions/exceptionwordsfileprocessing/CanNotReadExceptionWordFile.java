package programmsettings.exceptions.exceptionwordsfileprocessing;

import java.io.File;

public class CanNotReadExceptionWordFile extends ExceptionWordsFileProcessingException{

    public CanNotReadExceptionWordFile(File providedFile) {
        super(providedFile);
    }
}

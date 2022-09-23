package programmsettings.exceptions.exceptionwordsfileprocessing;

import programmsettings.exceptions.MainSettingsException;

import java.io.File;

public class ExceptionWordsFileProcessingException extends MainSettingsException {
    protected final File providedFile;

    public ExceptionWordsFileProcessingException(File providedFile) {
        this.providedFile = providedFile;
    }

    public File getProvidedFile() {
        return providedFile;
    }
}

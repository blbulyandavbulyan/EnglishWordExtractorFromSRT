package programsettings.exceptions.exceptionwordsfileprocessing;

import programsettings.exceptions.MainSettingsException;

import java.io.File;

public class ExceptionWordsFileProcessingException extends MainSettingsException {
    protected final File providedFile;

    public ExceptionWordsFileProcessingException(File providedFile) {
        super();
        this.providedFile = providedFile;
    }

    public File getProvidedFile() {
        return providedFile;
    }
}

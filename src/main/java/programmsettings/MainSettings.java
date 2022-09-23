package programmsettings;

import programmsettings.exceptions.exceptionwordsfileprocessing.*;

import java.io.File;
import java.net.ConnectException;

public class MainSettings {
    private File[] onStartExceptionWordsFiles;
    public MainSettings(File propertySettingsFile){

    }
    public MainSettings(File[] onStartExceptionWordsFiles) {
        this.onStartExceptionWordsFiles = onStartExceptionWordsFiles;
        selfCheck();
    }
    public void selfCheck(){
        //check exception words files array
        if(onStartExceptionWordsFiles != null){
            if(onStartExceptionWordsFiles.length > 0){
                for (File exceptionWordFile : onStartExceptionWordsFiles) {
                    if(!exceptionWordFile.isFile())throw new GivenExceptionWordsFileIsNotAFileException(exceptionWordFile);
                    else if(!exceptionWordFile.exists())throw new ExceptionWordsFileNotFound(exceptionWordFile);
                    else if(!exceptionWordFile.canRead())throw new CanNotReadExceptionWordFile(exceptionWordFile);
                    else if(exceptionWordFile.length() == 0)throw new ExceptionWordsFileIsEmptyException(exceptionWordFile);
                }
            }
            else throw new ExceptionFilesArrayIsNotNullAndEmpty();
        }
    }
    public File[] getOnStartExceptionWordsFiles() {
        return onStartExceptionWordsFiles;
    }
    public void setOnStartExceptionWordsFiles(File[] onStartExceptionWordsFiles) {
        this.onStartExceptionWordsFiles = onStartExceptionWordsFiles;
    }
}

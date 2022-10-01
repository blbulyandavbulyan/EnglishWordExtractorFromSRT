package programsettings;

import programsettings.exceptions.exceptionwordsfileprocessing.*;

import java.io.File;

public class MainSettings {
    private File[] exceptionWordsFiles;
//    public MainSettings(File propertySettingsFile){
//
//    }
    public MainSettings(File[] exceptionWordsFiles) {
        this.exceptionWordsFiles = exceptionWordsFiles;
        selfCheck();
    }
    public void selfCheck(){
        //check exception words files array
        if(exceptionWordsFiles != null){
            if(exceptionWordsFiles.length > 0){
                for (File exceptionWordFile : exceptionWordsFiles) {
                    if(!exceptionWordFile.isFile())throw new GivenExceptionWordsFileIsNotAFileException(exceptionWordFile);
                    else if(!exceptionWordFile.exists())throw new ExceptionWordsFileNotFoundException(exceptionWordFile);
                    else if(!exceptionWordFile.canRead())throw new CanNotReadExceptionWordFileException(exceptionWordFile);
                    else if(exceptionWordFile.length() == 0)throw new ExceptionWordsFileIsEmptyException(exceptionWordFile);
                }
            }
            else throw new ExceptionFilesArrayIsNotNullAndEmptyException();
        }
    }
    public File[] getExceptionWordsFiles() {
        return exceptionWordsFiles;
    }
    public void setExceptionWordsFiles(File[] exceptionWordsFiles) {
        this.exceptionWordsFiles = exceptionWordsFiles;
    }
}

package programsettings;

import programsettings.exceptions.exceptionwordsfileprocessing.*;

import java.io.File;
import java.util.Set;

public class MainSettings {
    private final Set<File> exceptionWordsFiles;
//    public MainSettings(File propertySettingsFile){
//
//    }
    public MainSettings(Set<File> exceptionWordsFiles) {
        this.exceptionWordsFiles = exceptionWordsFiles;
        selfCheck();
    }
    public void selfCheck(){
        //check exception words files array
        if(exceptionWordsFiles != null){
            if(exceptionWordsFiles.size() > 0){
                for (File exceptionWordFile : exceptionWordsFiles) {
                    if(!exceptionWordFile.exists())throw new ExceptionWordsFileNotFoundException(exceptionWordFile);
                    else if(!exceptionWordFile.isFile())throw new GivenExceptionWordsFileIsNotAFileException(exceptionWordFile);
                    else if(!exceptionWordFile.canRead())throw new CanNotReadExceptionWordFileException(exceptionWordFile);
                    else if(exceptionWordFile.length() == 0)throw new ExceptionWordsFileIsEmptyException(exceptionWordFile);
                }
            }
            else throw new ExceptionFilesArrayIsNotNullAndEmptyException();
        }
    }
    public Set<File> getExceptionWordsFiles() {
        return exceptionWordsFiles;
    }
//    public void setExceptionWordsFiles(Set<File> exceptionWordsFiles) {
//        this.exceptionWordsFiles = exceptionWordsFiles;
//    }
}

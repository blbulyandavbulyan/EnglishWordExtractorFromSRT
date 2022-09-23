package wordprocessing.wordinfo;

import gui.jtablereflection.annotations.ReflectionTable;
import gui.jtablereflection.annotations.ReflectionTableColumn;

@ReflectionTable
public class MinimalWordInfo {
    @ReflectionTableColumn(columnNamePropertiesKey = "minimalWordInfo.columnNames.word")
    protected final String word;
    @ReflectionTableColumn(columnNamePropertiesKey = "minimalWordInfo.columnNames.countOfRepeats")
    protected final Long countOfRepeats;
    public MinimalWordInfo(String word, long countOfRepeats) {
        this.word = word;
        this.countOfRepeats = countOfRepeats;
    }
    public MinimalWordInfo(MinimalWordInfo minimalWordInfo){
        this(minimalWordInfo.word, minimalWordInfo.countOfRepeats);
    }
    public String getWord() {
        return word;
    }

    public long getCountOfRepeats() {
        return countOfRepeats;
    }
}

package wordinfogenerator;

import jtablereflection.annotations.ReflectionTable;
import jtablereflection.annotations.ReflectionTableColumn;

@ReflectionTable
public class WordInfo extends MinimalWordInfo{
    @ReflectionTableColumn(columnNamePropertiesKey = "wordInfo.columnNames.translate")
    protected final String translate;
    @ReflectionTableColumn(columnNamePropertiesKey = "wordInfo.columnNames.partOfSpeech")
    protected final String partOfSpeech;

    public WordInfo(String word, long countOfRepeats, String translate, String partOfSpeech) {
        super(word, countOfRepeats);
        this.translate = translate;
        this.partOfSpeech = partOfSpeech;
    }

    public String getTranslate() {
        return translate;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }
}

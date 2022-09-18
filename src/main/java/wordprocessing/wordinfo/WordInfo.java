package wordprocessing.wordinfo;

import gui.jtablereflection.annotations.ReflectionTable;
import gui.jtablereflection.annotations.ReflectionTableColumn;
import wordprocessing.wordinfogenerator.exceptions.PartsOfSpeechIsNullException;
import wordprocessing.wordinfogenerator.exceptions.TranslatesIsNullException;
import wordprocessing.wordinfogenerator.exceptions.TranslatesLengthIsNotEqualToPartsOfSpeechLength;

@ReflectionTable(
        hierarchicalColumnOrder = ReflectionTable.HierarchicalColumnOrder.FROM_CHILD_TO_PARENT
)
public class WordInfo extends MinimalWordInfo {
    @ReflectionTableColumn(columnNamePropertiesKey = "wordInfo.columnNames.translate")
    protected final String[] translates;
    @ReflectionTableColumn(
            columnNamePropertiesKey = "wordInfo.columnNames.partOfSpeech",
            anotherColumnEditorControlThisColumn = true,
            anotherControlColumnName = "translates"
    )
    protected final String[] partsOfSpeech;

    public WordInfo(String word, long countOfRepeats, String[] translates, String[] partsOfSpeech) {
        super(word, countOfRepeats);
        if(translates == null)
            throw new TranslatesIsNullException();
        if(partsOfSpeech == null)
            throw new PartsOfSpeechIsNullException();
        if(translates.length != partsOfSpeech.length)
            throw new TranslatesLengthIsNotEqualToPartsOfSpeechLength();
        this.translates = translates;
        this.partsOfSpeech = partsOfSpeech;
    }
    //public WordInfo(MinimalWordInfo minimalWordInfo, String[] translates, )
    public String[] getTranslates() {
        return translates;
    }

    public String[] getPartsOfSpeech() {
        return partsOfSpeech;
    }
}

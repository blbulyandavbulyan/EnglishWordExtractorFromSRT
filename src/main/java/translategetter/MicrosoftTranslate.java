package translategetter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import translategetter.exceptions.DetectedAndGivenLanguageNotEqualException;
import translategetter.exceptions.InvalidStatusCode;
import translategetter.exceptions.TranslatorException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class MicrosoftTranslate implements TranslateProvider{
    public static void main(String[] args) throws IOException, InterruptedException {
        MicrosoftTranslate microsoftTranslate = new MicrosoftTranslate("739f0d0146msh7cc406a9f7c0d10p1f79c6jsn56e503cd2739");
        for (var translatedWord : microsoftTranslate.getWordTranslates("end", "en", "ru")) {
            System.out.println(translatedWord);
        }
    }
    private final String apiKey;
    public MicrosoftTranslate(String apiKey){
        this.apiKey = apiKey;
    }
    private static final HashMap<String, Translate.PartOfSpeech> partOfSpeechMapper = new HashMap<>();
    static{
        for (Translate.PartOfSpeech partOfSpeech : Translate.PartOfSpeech.values()) {
            partOfSpeechMapper.put(partOfSpeech.name(), partOfSpeech);
        }
    }

    @Override
    public Translate[] getWordTranslates(String word, String wordLanguage, String translateLanguage) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://microsoft-translator-text.p.rapidapi.com/Dictionary/Lookup?from=" + wordLanguage+ "&api-version=3.0&to="+translateLanguage))
                .header("content-type", "application/json")
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", "microsoft-translator-text.p.rapidapi.com")
                .method("POST", HttpRequest.BodyPublishers.ofString("[\n    {\n        \"Text\": \"" + word + "\"\n    }\n]"))
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            String responseString = response.body();
            if(response.statusCode() != 200){
                try{
                    JSONObject errorObject = new JSONObject(responseString).getJSONObject("error");
                    throw new InvalidStatusCode(response.statusCode(), errorObject.getInt("code"), errorObject.getString("message"));
                }
                catch (JSONException e){
                    throw new InvalidStatusCode(response.statusCode(), null, null);
                }
            }
            System.out.println(responseString);
            JSONArray translations = new JSONArray(responseString).getJSONObject(0).getJSONArray("translations");
            Translate[] translates = new Translate[translations.length()];
            for (int i = 0; i < translations.length(); i++){
                JSONObject translateJSON = translations.getJSONObject(i);
                translates[i] = new Translate(
                        convertPartOfSpeechNameToPartOfSpeech(translateJSON.getString("posTag")),
                        translateJSON.getString("normalizedTarget"),
                        translateJSON.getDouble("confidence")
                );
            }
            return translates;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String translatePhrase(String phrase, String phraseLanguage, String translateLanguage) throws TranslatorException {
        return translatePhrase(phrase, phraseLanguage, translateLanguage, false);
    }

    @Override
    public Set<String> getLanguages() {
        return null;
    }

    public String translatePhrase(String phrase, String phraseLanguage, String translateLanguage, boolean needCheckDetectedLanguage) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://microsoft-translator-text.p.rapidapi.com/translate?api-version=3.0&to%5B0%5D=" + translateLanguage +"&suggestedFrom=" + phraseLanguage +"&textType=plain&profanityAction=NoAction"))
                .header("content-type", "application/json")
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", "microsoft-translator-text.p.rapidapi.com")
                .method("POST", HttpRequest.BodyPublishers.ofString("[\n    {\n        \"Text\": \""+ phrase +"\"\n    }\n]"))
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200){
                try{
                    JSONObject errorObject = new JSONObject(response.body()).getJSONObject("error");
                    throw new InvalidStatusCode(response.statusCode(), errorObject.getInt("code"), errorObject.getString("message"));
                }
                catch (JSONException e){
                    throw new InvalidStatusCode(response.statusCode(), null, null);
                }
            }
            System.out.println(response.body());
            JSONArray jsonArray = new JSONArray(response.body());
            String detectedLanguage = jsonArray.getJSONObject(0).getJSONObject("detectedLanguage").getString("language");
            if(!detectedLanguage.equals(phraseLanguage) && needCheckDetectedLanguage)
                throw new DetectedAndGivenLanguageNotEqualException(detectedLanguage, phraseLanguage);
            return new JSONArray(response.body()).getJSONObject(0).getJSONArray("translations").getJSONObject(0).getString("text");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
    private static Translate.PartOfSpeech convertPartOfSpeechNameToPartOfSpeech(String partOfSpeechName){
        return partOfSpeechMapper.get(partOfSpeechName);
    }
}

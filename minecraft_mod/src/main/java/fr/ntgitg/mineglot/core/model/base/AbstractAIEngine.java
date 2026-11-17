package fr.ntgitg.mineglot.core.model.base;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.ntgitg.mineglot.core.validation.ValidationService;
import fr.ntgitg.mineglot.utils.log.ModLogger;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.net.ConnectException;
import java.util.regex.Pattern;

/**
 * Translation engine backed by a LOCAL Argos Translate server (argos_server.py).
 * No API key, no OpenAI/Claude. Class name and public method are kept so the rest
 * of the mod (TranslationCacheApiFlow) does not need to change.
 */
public final class AbstractAIEngine {

    private static final String ARGOS_URL = "http://127.0.0.1:5000/translate";
    private static final Pattern CYRILLIC = Pattern.compile("[\\u0400-\\u04FF]");

    private static final BaseHttpClient ARGOS_HTTP_CLIENT = new BaseHttpClient() {
        @Override
        protected String getApiUrl() {
            return ARGOS_URL;
        }

        @Override
        protected void addHeaders(HttpPost post, String apiKey) {
            // local server: no headers / no key needed
        }
    };

    private AbstractAIEngine() {
    }

    public static String translateMessageWithSourceLangUncached(String text, String sourceLang,
                                                                String targetLanguage,
                                                                String engineName)
            throws IOException {
        ValidationService.ValidationResult textValidation =
                ValidationService.validateTranslationText(text);
        if (!textValidation.isValid()) {
            throw new IllegalArgumentException(textValidation.getErrorMessage());
        }
        if (!ValidationService.isNotEmpty(targetLanguage)) {
            throw new IllegalArgumentException("Target language code cannot be empty");
        }

        // Script-based source detection is far more reliable than Lingua on short chat:
        // any Cyrillic letter -> Russian, otherwise English.
        // (the [T] button forces sourceLang="ru" even for Latin-script Russian)
        String source = (CYRILLIC.matcher(text).find() || "ru".equalsIgnoreCase(sourceLang))
                ? "ru" : "en";
        String target = targetLanguage.toLowerCase();

        if (source.equals(target)) {
            return text; // already in the requested language
        }

        JsonObject body = new JsonObject();
        body.addProperty("q", text);
        body.addProperty("source", source);
        body.addProperty("target", target);

        String response;
        try {
            response = BaseHttpClient.postJson(ARGOS_HTTP_CLIENT, body.toString(), "");
        } catch (ConnectException e) {
            throw new IOException("Argos server not running on " + ARGOS_URL
                    + " - start argos_server.py first", e);
        }

        JsonObject json = new JsonParser().parse(response).getAsJsonObject();
        if (!json.has("translatedText")) {
            throw new IOException("Argos response has no translatedText: " + response);
        }
        String translation = json.get("translatedText").getAsString();
        ModLogger.debug("[ARGOS] [{}->{}] {} => {}", source, target, text, translation);
        return translation;
    }
}

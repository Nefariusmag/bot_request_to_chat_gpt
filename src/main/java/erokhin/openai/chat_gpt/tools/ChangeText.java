package erokhin.openai.chat_gpt.tools;

public class ChangeText {

    /**
     * Escapes special symbols in the given text.
     *
     * @param text The text to escape special symbols.
     * @return The text where all special symbols are escaped.
     */
    public static String getTextWhereAllSpecialSymbolsEscaped(String text){
        if (text == null) {
            return null;
        }
        return text.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace(":", "\\\\:");
    }
}

package erokhin.openai.chat_gpt.dto;

public enum VersionModel {
    GPT_3_5_TURBO("gpt-3.5-turbo", "This is GPT 3.5 Turbo version"),
    GPT_4("gpt-4", "This is GPT 4 version");

    private final String name;
    private final String description;

    VersionModel(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static VersionModel getByName(String name) {
        for (VersionModel version : VersionModel.values()) {
            if (version.getName().equals(name)) {
                return version;
            }
        }
        throw new IllegalArgumentException("No version with provided name found!");
    }
}

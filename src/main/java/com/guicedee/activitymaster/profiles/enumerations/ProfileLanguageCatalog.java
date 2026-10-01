package com.guicedee.activitymaster.profiles.enumerations;

import com.guicedee.activitymaster.profiles.webdto.ProfileAttributeChoiceDTO;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Default ISO language lookup entries; enterprise classifications remain authoritative. */
public final class ProfileLanguageCatalog {
    private ProfileLanguageCatalog() {}
    public static final String CONCEPT = "ProfileLanguages";
    public static final int MAX_SPOKEN = 20;
    public static final List<ProfileAttributeChoiceDTO> DEFAULTS = Arrays.stream(Locale.getISOLanguages())
        .map(code -> Locale.forLanguageTag(code).getLanguage()).distinct()
        .map(code -> new ProfileAttributeChoiceDTO("ProfileLanguage_" + code,
            Locale.forLanguageTag(code).getDisplayLanguage(Locale.ENGLISH)))
        .sorted(Comparator.comparing(ProfileAttributeChoiceDTO::label, String.CASE_INSENSITIVE_ORDER)).toList();

    public static boolean isLanguage(String attribute) {
        return "HomeLanguage".equals(attribute) || "SpokenLanguages".equals(attribute);
    }

    public static List<String> parse(String attribute, String value) {
        if (value == null || value.isEmpty()) return List.of();
        List<String> selections = Arrays.stream(value.split(",", -1)).map(String::strip).toList();
        if (selections.stream().anyMatch(String::isEmpty)
            || selections.stream().distinct().count() != selections.size()
            || selections.size() > ("HomeLanguage".equals(attribute) ? 1 : MAX_SPOKEN))
            throw new IllegalArgumentException("Choose distinct published languages (at most twenty spoken languages)");
        return selections;
    }
}

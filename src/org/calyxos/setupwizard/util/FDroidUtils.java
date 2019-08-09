package org.calyxos.setupwizard.util;

import android.content.res.Resources;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

// Based on org.fdroid.fdroid.data.App
public class FDroidUtils {

    private static final String TAG = FDroidUtils.class.getSimpleName();

    /**
     * Parses the {@code localized} block in the incoming index metadata,
     * choosing the best match in terms of locale/language while filling as
     * many fields as possible.  It first sets up a locale list based on user
     * preference and the locales available for this app, then picks the texts
     * based on that list.  One thing that makes this tricky is that any given
     * locale block in the index might not have all the fields.  So when filling
     * out each value, it needs to go through the whole preference list each time,
     * rather than just taking the whole block for a specific locale.  This is to
     * ensure that there is something to show, as often as possible.
     * <p>
     * It is still possible that the fields will be loaded directly by Jackson
     * without any locale info.  This comes from the old-style, inline app metadata
     * fields that do not have locale info.  They should not be used if the
     * {@code localized} block is included in the index.  Also, null strings in
     * the {@code localized} block should not overwrite Name/Summary/Description
     * strings with empty/null if they were set directly by Jackson.
     * <p>
     * Choosing the locale to use follows two sets of rules, one for Android versions
     * older than {@code android-24} and the other for {@code android-24} or newer.
     * The system-wide language preference list was added in {@code android-24}.
     * <ul>
     * <li>{@code >= android-24}<ol>
     * <li>the country variant {@code de-AT} from the user locale list
     * <li>only the language {@code de} from the above locale
     * <li>{@code en-US} since its the most common English for software
     * <li>the first available {@code en} locale
     * </ol></li>
     * <li>{@code < android-24}<ol>
     * <li>the country variant from the user locale: {@code de-AT}
     * <li>only the language from the above locale:  {@code de}
     * <li>all available locales with the same language:  {@code de-BE}
     * <li>{@code en-US} since its the most common English for software
     * <li>all available {@code en} locales
     * </ol></li>
     * </ul>
     * On {@code >= android-24}, it is by design that this does not fallback to other
     * country-specific locales, e.g. {@code fr-CH} does not fall back on {@code fr-FR}.
     * If someone wants to fallback to {@code fr-FR}, they can add it to the system
     * language list.  There are many cases where it is inappropriate to fallback to a
     * different country-specific locale, for example {@code de-DE --> de-CH} or
     * {@code zh-CN --> zh-TW}.
     * <p>
     * On {@code < android-24}, the user can only set a single
     * locale with a country as an option, so here it makes sense to try to fallback
     * on other country-specific locales, rather than English.
     */
    public static Set<String> getlocalesToUse(JSONObject localized) { // NOPMD
        Locale defaultLocale = Locale.getDefault();
        String languageTag = defaultLocale.getLanguage();
        String countryTag = defaultLocale.getCountry();
        String localeTag;
        if (TextUtils.isEmpty(countryTag)) {
            localeTag = languageTag;
        } else {
            localeTag = languageTag + "-" + countryTag;
        }

        Set<String> availableLocales = new LinkedHashSet<>();
        Iterator<String> keys = localized.keys();
        while (keys.hasNext()) {
            availableLocales.add(keys.next());
        }
        Set<String> localesToUse = new LinkedHashSet<>();
        if (availableLocales.contains(localeTag)) {
            localesToUse.add(localeTag);
        }
        if (availableLocales.contains(languageTag)) {
            localesToUse.add(languageTag);
        }
        LocaleList localeList = Resources.getSystem().getConfiguration().getLocales();
        String[] sortedLocaleList = localeList.toLanguageTags().split(",");
        Arrays.sort(sortedLocaleList, new java.util.Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.length() - s2.length();
            }
        });
        for (String toUse : sortedLocaleList) {
            localesToUse.add(toUse);
            for (String l : availableLocales) {
                if (l.equals(toUse.split("-")[0])) {
                    localesToUse.add(l);
                    break;
                }
            }
        }
        if (availableLocales.contains("en-US")) {
            localesToUse.add("en-US");
        }
        for (String l : availableLocales) {
            if (l.startsWith("en")) {
                localesToUse.add(l);
                break;
            }
        }

        return localesToUse;
    }

    /**
     * Returns the right localized version of this entry, based on an immitation of
     * the logic that Android/Java uses.  On Android >= 24, this can get the
     * "Language Priority List", but it doesn't always seem to be properly sorted.
     * So this method has to kind of fake it by using {@link Locale#getDefault()}
     * as the first entry, then sorting the rest based on length (e.g. {@code de-AT}
     * before {@code de}).
     *
     * @see LocaleList
     * @see Locale#getDefault()
     * @see java.util.Locale.LanguageRange
     */
    public static String getLocalizedEntry(JSONObject localized,
                                     Set<String> locales, String key) {
        try {
            for (String locale: locales) {
                if (localized.has(locale)) {
                    JSONObject locale_j = localized.getJSONObject(locale);
                    if (locale_j.has(key)) {
                        return locale_j.getString(key);
                    }
                }
            }
        } catch (ClassCastException | JSONException e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * Returns the app description text with all newlines replaced by {@code <br>}
     */
    public static String formatDescription(String description) {
        return description.replace("\n", "<br>");
    }
}
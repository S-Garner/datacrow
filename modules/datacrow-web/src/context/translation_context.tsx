/**
 * Translation Context:
 * Responsible for delivering all messages and labels within the application. Theu are based directly on 
 * the standard Data Crow resources. This context also contains the useTranslation hook and the provider.
 * The default language is set to English.
 * 
 */
import { createContext, useCallback, useContext, type JSX, useState, useMemo } from 'react';

export const languages = {
    dutch: 'Dutch',
    english: 'English',
    french: 'French',
    german: 'German',
    italien: 'Italian',
    polski: 'Polski',
    portuguese: 'Portuguese',
    russian: 'Russian',
    spanish: 'Spanish',
} as const;

export const languageArray: { key: string, label: string, language: Language }[] = [
    { key: "English", label: "English", language: languages.english },
    { key: "German", label: "Deutsch", language: languages.german },
    { key: "Spanish", label: "Español", language: languages.spanish },
    { key: "French", label: "Francais", language: languages.french },
    { key: "Italian", label: "Italiano", language: languages.italien },
    { key: "Dutch", label: "Nederlands", language: languages.dutch },
    { key: "Polski", label: "Polski", language: languages.polski },
    { key: "Portuguese", label: "Português", language: languages.portuguese },
    { key: "Russian", label: "Русский", language: languages.russian }
]

export const TranslationContext = createContext<
    TranslationContextValue | undefined
>(undefined);

export type Translation = Map<String, String>

export type TFunction = (
    params: string,
    replacements?: string[]
) => string | undefined;

export type Language = (typeof languages)[keyof typeof languages];

export type TranslationContextValue = {
    translations: Translation | undefined;
    setTranslations: React.Dispatch<
        React.SetStateAction<Translation | undefined>
    >;
    language: Language;
    setLanguage: React.Dispatch<React.SetStateAction<Language>>;
};

type TranslationProviderProps = {
    children: React.ReactNode;
};

export function TranslationProvider({
    children,
}: TranslationProviderProps): JSX.Element {
    
    const [language, setLanguage] = useState<Language>(languages.english);
    const [translations, setTranslations] = useState<Translation | undefined>();

    // get the stored value from the local storage (previous selection)
    if (localStorage.getItem("language")) {
        let key = String(localStorage.getItem("language"));
        
        for (const lang of languageArray) {
            // only apply if the local storage value is different from the currently applied language
            if (lang.key === key && lang.language != language)
                setLanguage(lang.language);
        }
    }

    const value: TranslationContextValue = useMemo(
        () => ({
            translations,
            language,
            setLanguage,
            setTranslations,
        }),
        [language, translations]
    );

    return (
        <TranslationContext.Provider value={value}>
            {children}
        </TranslationContext.Provider>
    );
}

// Create a custom hook to access the context value
export function useTranslation(
): {
    t: TFunction;
    setLanguage: React.Dispatch<React.SetStateAction<Language>>;
    language: Language;
    setTranslations: React.Dispatch<React.SetStateAction<Translation | undefined>>;
} {
    const context = useContext(TranslationContext);

    if (!context)
        throw new Error('useTranslation must be used within a TranslationProvider');

    const { translations, language, setLanguage, setTranslations } = context;

    const t = useCallback<TFunction>(
        (key, replacements): string => {

            if (!translations)
                return key;

            let text = String(translations.get(key));

            if (replacements) {
                let counter = 1;
                for (const replacement of replacements)
                    text = text.replace("%" + counter++, replacement);
            }

            return text;
        },
        [translations]
    );

    return { t, setLanguage, language, setTranslations };
}
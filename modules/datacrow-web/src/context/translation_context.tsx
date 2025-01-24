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

export const TranslationContext = createContext<
    TranslationContextValue | undefined
>(undefined);

export type Translation = Map<String, String>

export type TFunction = (
    params: string
) => string | undefined;

export type Language = (typeof languages)[keyof typeof languages];

// Define the type for your context value
export type TranslationContextValue = {
    translations: Translation | undefined;
    setTranslations: React.Dispatch<
        React.SetStateAction<Translation | undefined>
    >;
    language: Language;
    setLanguage: React.Dispatch<React.SetStateAction<Language>>;
};

// Create the provider component
type TranslationProviderProps = {
    children: React.ReactNode;
};

export function TranslationProvider({
    children,
}: TranslationProviderProps): JSX.Element {
    
    const [language, setLanguage] = useState<Language>(languages.english);
    const [translations, setTranslations] = useState<Translation | undefined>();

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
        (key): string => {

            if (!translations)
                return key;

            return String(translations.get(key));
        },
        [translations]
    );

    return { t, setLanguage, language, setTranslations };
}
import { createContext, useCallback, useContext, useEffect, type JSX, useState, useMemo } from 'react';

export const languages = {
    dutch: 'Dutch',
    english: 'English',
} as const;

export const TranslationContext = createContext<
    TranslationContextValue | undefined
>(undefined);

export type Translation = {
    key: string,
    value: string
}

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
            language,
            setLanguage,
            setTranslations,
            translations,
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
} {
    const context = useContext(TranslationContext);

    if (!context) {
        throw new Error('useTranslation must be used within a TranslationProvider');
    }

    const { translations, language, setLanguage, setTranslations } = context;

    useEffect(() => {
        const prepareTranslation = async () => {
/*            if (language === languages.tr) {
                setTranslations(tr);
            }

            if (language === languages.en) {
                setTranslations(en);
            } */
        };

        prepareTranslation();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [language]);

    const t = useCallback<TFunction>(
        (key, param?: string): string | undefined => {

            if (!translations) {
                return String("missing!");
            }

            return String("");
        },
        [translations]
    );

    return { t, setLanguage, language };
}
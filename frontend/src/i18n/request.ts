// src/i18n/request.ts
import {getRequestConfig} from 'next-intl/server';

export default getRequestConfig(async ({requestLocale}) => {
  let locale = await requestLocale;

  if (!locale || !['en', 'fr'].includes(locale)) {
    locale = 'fr';  // Default to French as you prefer
  }

  const messages = (await import(`../locales/${locale}/common.json`)).default;

  return {
    locale,
    messages
  };
});
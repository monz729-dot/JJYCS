import { createI18n } from 'vue-i18n'

// Import locale messages
import ko from './locales/ko.json'
import en from './locales/en.json'

export type MessageLanguages = keyof typeof messages
export type MessageSchema = typeof messages['ko']

const messages = {
  ko,
  en
}

const i18n = createI18n<[MessageSchema], MessageLanguages>({
  legacy: false,
  locale: 'ko', // default locale
  fallbackLocale: 'en',
  messages
})

export default i18n
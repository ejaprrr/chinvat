export const USERNAME_MAX_LENGTH = 100;
export const FULL_NAME_MAX_LENGTH = 255;
export const PHONE_MAX_LENGTH = 40;
export const EMAIL_MAX_LENGTH = 255;
export const DEFAULT_LANGUAGE_MAX_LENGTH = 12;
export const ADDRESS_MAX_LENGTH = 255;
export const POSTAL_CODE_MAX_LENGTH = 20;
export const CITY_MAX_LENGTH = 100;
export const COUNTRY_MAX_LENGTH = 100;

export function isWellFormedEmail(value: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

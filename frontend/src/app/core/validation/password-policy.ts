export const PASSWORD_MIN_LENGTH = 8;
export const PASSWORD_SPECIAL_CHARACTERS = '@$!%*?&';

export function cumplePoliticaPassword(password: string): boolean {
  return password.length >= PASSWORD_MIN_LENGTH
    && /[a-z]/.test(password)
    && /[A-Z]/.test(password)
    && /\d/.test(password)
    && /[@$!%*?&]/.test(password);
}

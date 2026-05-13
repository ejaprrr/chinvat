import js from '@eslint/js';
import globals from 'globals';
import jsxA11y from 'eslint-plugin-jsx-a11y';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import tseslint from 'typescript-eslint';
import { defineConfig, globalIgnores } from 'eslint/config';

const i18nKeyPattern = /^(auth|common|error)\.(fields|actions|status|messages|errors)\./;

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      jsxA11y.flatConfigs.strict,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    settings: {
      react: {
        version: 'detect',
      },
    },
    rules: {
      'jsx-a11y/label-has-associated-control': [
        'error',
        {
          assert: 'either',
          depth: 3,
        },
      ],
      'jsx-a11y/no-autofocus': 'error',
      'no-restricted-syntax': [
        'error',
        {
          selector: "JSXAttribute[name.name='style']",
          message:
            'Avoid inline style props in the frontend. Use Tailwind utilities or global theme tokens from src/index.css instead.',
        },
        {
          selector:
            'JSXAttribute[name.name=/^(label|placeholder|aria-label|hint|message|title|buttonText|aria-description)$/] > Literal[value!=""]',
          message:
            'Hardcoded user-facing text in UI props. Use the t() function for localization. Example: label={t("auth.fields.email.label")}',
        },
        {
          selector: 'JSXText[value=/^\\s*[A-Z]/]',
          message:
            'Hardcoded JSX text content detected. Use the t() function to wrap user-facing text: {t("i18n.key")}',
        },
      ],
    },
  },
]);

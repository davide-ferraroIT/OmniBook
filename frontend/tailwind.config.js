/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          DEFAULT: 'var(--ion-color-primary)',
          dark: 'var(--ion-color-primary-shade, #1e293b)',
          light: 'var(--ion-color-primary-tint, #60a5fa)',
        }
      }
    },
  },
  plugins: [],
}

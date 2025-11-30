/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  darkMode: 'class', // Esto es crucial para v3
  theme: {
    extend: {
      animation: {
        'blob': 'blob 8s ease-in-out infinite',
      },
      keyframes: {
        blob: {
          '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
          '25%': { transform: 'translate(40px, -50px) scale(1.15)' },
          '50%': { transform: 'translate(-40px, 40px) scale(0.85)' },
          '75%': { transform: 'translate(50px, 20px) scale(1.1)' },
        },
      },
    },
  },
  plugins: [],
}
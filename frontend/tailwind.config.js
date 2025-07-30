import plugin from 'tailwindcss/plugin';

export const content = [
    "./src/**/*.{html,ts}", // Asegúrate de incluir todos los archivos Angular
    "./node_modules/primeng/**/*.{js,ts}"
];
export const theme = {
    extend: {},
};
export const plugins = [
    require('tailwindcss-primeui') // Si existe ese plugin
];

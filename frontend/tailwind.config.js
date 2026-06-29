/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './app/**/*.{js,ts,jsx,tsx,mdx}', // Note the addition of the `app` directory.
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',

    // Or if using `src` directory:
    './src/**/*.{js,ts,jsx,tsx,mdx}',
  ], theme: {
    extend: {
        colors: {
            "cats-dark-green": '#143C30',
            "cats-light-green": '#9CBBB2',
            "cats-medium-red": '#EF8B8B',
            "cats-medium-white": "#F0F9F6",
            "cats-dark-white": "#F0F0F0"
        },
    },
  },
  plugins: [],
}

